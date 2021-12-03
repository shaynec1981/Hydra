package models;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import utils.DBQuery;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static models.Main.conn;

public class Invoice {

    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    public static void print(String invoiceID) throws IOException, SQLException {
        String customerName = null, customerEmail = null, customerPhone = null, invoiceNumber;
        LocalDate transactionDate = null;
        BigDecimal subtotal = null, discount = null, grandTotal = null;
        double salesTaxRate = 0.00;
        ArrayList<String> allSkus = new ArrayList<>();
        ArrayList<java.util.List<String>> lineItem = new ArrayList<>();
        ArrayList<java.util.List<String>> invoiceLineItems = new ArrayList<>();

        invoiceNumber = invoiceID;

        String fistStatement = "SELECT Customer_Name, Customer_Email, Customer_Phone FROM hydra_customers WHERE hydra_customers.Customer_ID = (SELECT hydra_invoices.Customer_ID FROM hydra_invoices WHERE hydra_invoices.Invoice_ID = '" + invoiceID + "')";
        DBQuery.setPreparedStatement(conn, fistStatement);
        PreparedStatement firstStatementPS = DBQuery.getPreparedStatement();
        firstStatementPS.execute();
        ResultSet firstStatementRS = firstStatementPS.getResultSet();
        if (!firstStatementRS.next()) {
            customerName = "DOESN'T EXIST";
            customerEmail = "DOESN'T EXIST";
            customerPhone = "DOESN'T EXIST";
        } else {
            do {
                customerName = firstStatementRS.getString("Customer_Name");
                customerEmail = firstStatementRS.getString("Customer_Email");
                customerPhone = firstStatementRS.getString("Customer_Phone");
            } while (firstStatementRS.next());
        }
        String secondStatement = "SELECT Date, Items_Total, Discounts_Total, Grand_Total, Tax_Rate FROM hydra_invoices WHERE Invoice_ID = '" + invoiceID + "'";
        DBQuery.setPreparedStatement(conn, secondStatement);
        PreparedStatement secondStatementPS = DBQuery.getPreparedStatement();
        secondStatementPS.execute();
        ResultSet secondStatementRS = secondStatementPS.getResultSet();
        while (secondStatementRS.next()) {
            transactionDate = LocalDate.parse(secondStatementRS.getString("Date").substring(0, 10));
            subtotal = secondStatementRS.getBigDecimal("Items_Total");
            discount = secondStatementRS.getBigDecimal("Discounts_Total");
            grandTotal = secondStatementRS.getBigDecimal("Grand_Total");
            salesTaxRate = secondStatementRS.getDouble("Tax_Rate") / 100;
        }
        String allSKUsStatement = "SELECT DISTINCT SKU FROM hydra_invoice_lineitems WHERE Invoice_ID = '" + invoiceID + "'";
        DBQuery.setPreparedStatement(conn, allSKUsStatement);
        PreparedStatement allSKUsPS = DBQuery.getPreparedStatement();
        allSKUsPS.execute();
        ResultSet allSKUsRS = allSKUsPS.getResultSet();
        while (allSKUsRS.next()) {
            allSkus.add(allSKUsRS.getString("SKU"));
        }

        assert false;
        for (String aSKU : allSkus) {
            String thirdStatement = "SELECT allinvoices.Name, allinvoices.SKU, allinvoices.Cost, allinvoices.Markup FROM (SELECT hydra_invoices.*, ip.* FROM hydra_invoices INNER JOIN (SELECT Invoice_ID FROM hydra_invoice_lineitems WHERE SKU = '" + aSKU + "') i ON hydra_invoices.Invoice_ID = i.Invoice_ID INNER JOIN (SELECT SKU, Name, Cost, Markup FROM hydra_items UNION SELECT SKU, Name, Cost, Markup FROM hydra_products) ip ON ip.SKU = '" + aSKU + "') allinvoices WHERE allinvoices.Invoice_ID = '" + invoiceID + "'";
            DBQuery.setPreparedStatement(conn, thirdStatement);
            PreparedStatement thirdStatementPS = DBQuery.getPreparedStatement();
            thirdStatementPS.execute();
            ResultSet thirdStatementRS = thirdStatementPS.getResultSet();
            while (thirdStatementRS.next()) {
                lineItem.add(Arrays.asList(thirdStatementRS.getString("Name"), thirdStatementRS.getString("SKU"), thirdStatementRS.getString("Cost"), thirdStatementRS.getString("Markup"), "1"));
            }
        }
        File sourceFile = new File("src/resources/invoiceTemplate.pdf");
        File file = new File("src/resources/invoice" + invoiceNumber + ".pdf");
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        Files.copy(sourceFile.toPath(), file.toPath());
        PDDocument doc = PDDocument.load(file);
        PDPage page = doc.getPage(0);
        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
        PDImageXObject pdImage = PDImageXObject.createFromFile("src/images/hydraIcon100x100.png", doc);
        contentStream.drawImage(pdImage, 475, 675);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.TIMES_BOLD, 16);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(50, 750);
        contentStream.showText("<COMPANY NAME>");
        contentStream.newLine();
        contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
        contentStream.showText("<Company Address>");
        contentStream.newLine();
        contentStream.showText("<Website, Email Address>");
        contentStream.newLine();
        contentStream.showText("<Phone Number>");
        contentStream.newLineAtOffset(65,-175);
        assert customerName != null;
        contentStream.showText(customerName);
        contentStream.newLine();
        assert customerEmail != null;
        contentStream.showText(customerEmail);
        contentStream.newLine();
        assert customerPhone != null;
        contentStream.showText(customerPhone);
        contentStream.newLineAtOffset(390, 31);
        assert transactionDate != null;
        contentStream.showText(transactionDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        Matrix matrix = Matrix.getRotateInstance(Math.toRadians(90), 0, 0);
        matrix.translate(0, -page.getMediaBox().getWidth());
        contentStream.setTextMatrix(matrix);
        contentStream.newLineAtOffset(450, 535);
        contentStream.setFont(PDType1Font.TIMES_BOLD, 16);
        contentStream.showText(invoiceNumber);
        contentStream.endText();

        for (List<String> everyItem : lineItem) {
            outer:
            if (!invoiceLineItems.equals(null)) {
                for (List<String> nonduplicatedItem : invoiceLineItems) {
                    if (everyItem.get(1).equals(nonduplicatedItem.get(1))) {
                        // add 1 to invoiceLineItems quantity
                        invoiceLineItems.get(invoiceLineItems.indexOf(nonduplicatedItem)).set(4, String.valueOf(Integer.parseInt(nonduplicatedItem.get(4)) + 1));
                        break outer;
                    }
                }
                invoiceLineItems.add(everyItem);
            } else {
                invoiceLineItems.add(everyItem);
            }
        }

        if (invoiceLineItems.size() > 11) {
            contentStream.close();
            // Create correct number of pages, 11 line items to a page.
            int pagesToAdd = (int) Math.ceil((float) invoiceLineItems.size() / 11) - 1;
            int totalPages = pagesToAdd + 1;
            for (int i = 0; i < pagesToAdd; i++) {
                page = doc.getPage(0);
                doc.importPage(page);
            }
            for (int i = 0; i < totalPages; i++) { // Iterate through each page
                page = doc.getPage(i);
                contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
                for (int x = 0; x < 11; x++) { // Iterate through 11 items per page
                    if ((i + (i * 10)) + x == invoiceLineItems.size()) {
                        break;
                    }
                    List<String> item = invoiceLineItems.get( (i + (i * 10)) + x );
                    BigDecimal cost = BigDecimal.valueOf(Double.parseDouble(item.get(2))).add(BigDecimal.valueOf(Double.parseDouble(item.get(2))).multiply(BigDecimal.valueOf(Double.parseDouble(item.get(3))).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)) );
                    int quantity = Integer.parseInt(item.get(4));
                    BigDecimal totalCost = cost.multiply(BigDecimal.valueOf(quantity));
                    contentStream.beginText();
                    contentStream.setLeading(15.85f);
                    contentStream.setFont(PDType1Font.TIMES_ROMAN, 8);
                    contentStream.newLineAtOffset(120, 440 - (x * 16));
                    contentStream.showText(item.get(0) + " | " + item.get(1));
                    contentStream.newLineAtOffset(200, 0);
                    contentStream.showText(item.get(4));
                    contentStream.newLineAtOffset(55, 0);
                    contentStream.showText(currencyFormat.format(cost));
                    contentStream.newLineAtOffset(125, 0);
                    contentStream.showText(currencyFormat.format(totalCost));
                    contentStream.endText();
                }
                contentStream.beginText();
                contentStream.setLeading(17f);
                contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
                contentStream.newLineAtOffset(500, 265);
                if (i == totalPages - 1) {
                    // Totals
                    contentStream.showText(currencyFormat.format(subtotal));
                    contentStream.newLine();
                    contentStream.showText(currencyFormat.format(discount));
                    contentStream.newLine();
                    contentStream.showText(currencyFormat.format(subtotal.subtract(discount)));
                    contentStream.newLine();
                    contentStream.showText(salesTaxRate * 100 + "%");
                    contentStream.newLine();
                    contentStream.showText(currencyFormat.format(BigDecimal.valueOf(salesTaxRate).multiply(subtotal.subtract(discount))));
                    contentStream.setLeading(28f);
                    contentStream.setFont(PDType1Font.TIMES_BOLD, 14);
                    contentStream.newLine();
                    contentStream.showText(currencyFormat.format(grandTotal));
                } else {
                    // Totals
                    contentStream.showText("next page");
                    contentStream.newLine();
                    contentStream.showText("next page");
                    contentStream.newLine();
                    contentStream.showText("next page");
                    contentStream.newLine();
                    contentStream.showText(salesTaxRate * 100 + "%");
                    contentStream.newLine();
                    contentStream.showText("next page");
                    contentStream.setLeading(28f);
                    contentStream.setFont(PDType1Font.TIMES_BOLD, 14);
                    contentStream.newLine();
                    contentStream.showText("next page");
                }
                contentStream.endText();
                contentStream.close();
            }

        } else {
            for (int i = 0; i < invoiceLineItems.size(); i++) {
                List<String> item = invoiceLineItems.get(i);
                BigDecimal cost = BigDecimal.valueOf(Double.parseDouble(item.get(2))).add( BigDecimal.valueOf(Double.parseDouble(item.get(2))).multiply(BigDecimal.valueOf(Double.parseDouble(item.get(3))).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)) );
                int quantity = Integer.parseInt(item.get(4));
                BigDecimal totalCost = cost.multiply(BigDecimal.valueOf(quantity));
                contentStream.beginText();
                contentStream.setLeading(15.85f);
                contentStream.setFont(PDType1Font.TIMES_ROMAN, 8);
                contentStream.newLineAtOffset(120, 440 - (i * 16));
                contentStream.showText(item.get(0) + " | " + item.get(1));
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(item.get(4));
                contentStream.newLineAtOffset(55, 0);
                contentStream.showText(currencyFormat.format(cost));
                contentStream.newLineAtOffset(125, 0);
                contentStream.showText(currencyFormat.format(totalCost));
                contentStream.endText();
            }
            // Totals
            contentStream.beginText();
            contentStream.setLeading(17f);
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
            contentStream.newLineAtOffset(500, 265);
            contentStream.showText(currencyFormat.format(subtotal));
            contentStream.newLine();
            contentStream.showText(currencyFormat.format(discount));
            contentStream.newLine();
            contentStream.showText(currencyFormat.format(subtotal.subtract(discount)));
            contentStream.newLine();
            contentStream.showText(salesTaxRate * 100 + "%");
            contentStream.newLine();
            contentStream.showText(currencyFormat.format(BigDecimal.valueOf(salesTaxRate).multiply(subtotal.subtract(discount))));
            contentStream.setLeading(28f);
            contentStream.setFont(PDType1Font.TIMES_BOLD, 14);
            contentStream.newLine();
            contentStream.showText(currencyFormat.format(grandTotal));
            contentStream.endText();
            contentStream.close();
        }
        contentStream.close();
        doc.save("src/resources/invoice" + invoiceNumber + ".pdf");
        doc.close();
        Desktop.getDesktop().open(new File("src/resources/invoice" + invoiceNumber + ".pdf"));
    }
}
