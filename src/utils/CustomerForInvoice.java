package utils;

import models.Customer;

public class CustomerForInvoice {

    private static Customer customer;

   public static void setCustomer(Customer customer) {
        CustomerForInvoice.customer = customer;
    }

    public static Customer getCustomer() {
        return customer;
    }
}
