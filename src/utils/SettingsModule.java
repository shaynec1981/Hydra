package utils;

import javafx.scene.image.Image;

import java.util.prefs.Preferences;

public class SettingsModule {

    private static double salesTax;
    private static Image dashboardBackgroundImage = new Image("/images/hydraBG.png");
    public static final Preferences userPreferences = Preferences.userRoot();

    public static void setSalesTax(Double tax) { SettingsModule.salesTax = tax; }

    public static double getSalesTax() {
        salesTax = userPreferences.getDouble("SALES_TAX", 0.00);
        salesTax = Math.round(salesTax * 100);
        salesTax = salesTax / 100;
        return salesTax;
    }

   public static void setDashboardBackgroundImage(Image image) {
        SettingsModule.dashboardBackgroundImage = image;
    }

    public static Image getDashboardBackgroundImage() {
        String bgImageText = userPreferences.get("DASHBOARD_IMAGE", "/images/hydraBG.png");
        dashboardBackgroundImage = new Image(bgImageText);
        return dashboardBackgroundImage;
    }
}
