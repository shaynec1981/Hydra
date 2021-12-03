package resources;

import com.digidemic.unitof.UnitOf;

public class HelperMethods {

    public static String removeCharacter(String fullString, String remove) {
        StringBuilder newString = new StringBuilder(fullString);
        for (int i = fullString.length(); i > 0; i--) {
            if (newString.indexOf(remove) != -1) {
                newString.deleteCharAt(newString.indexOf(remove));
            }
        }
        return newString.toString();
    }

    public static Double measurementConverter(String fromAmount, String fromMeasurement, String toMeasurement) {
        UnitOf.Volume unit = new UnitOf.Volume();
        double result;
        switch (fromMeasurement) {
            case "gallon" -> unit.fromGallonsUS(Double.parseDouble(fromAmount));
            case "quart" -> unit.fromQuartsUS(Double.parseDouble(fromAmount));
            case "pint" -> unit.fromPintsUS(Double.parseDouble(fromAmount));
            case "fl oz" -> unit.fromFluidOuncesUS(Double.parseDouble(fromAmount));
            case "cup" -> unit.fromCupsUS(Double.parseDouble(fromAmount));
            case "tsp" -> unit.fromTeaspoonsUS(Double.parseDouble(fromAmount));
            case "tbsp" -> unit.fromTablespoonsUS(Double.parseDouble(fromAmount));
            case "L" -> unit.fromLiters(Double.parseDouble(fromAmount));
            case "mL" -> unit.fromMilliliters(Double.parseDouble(fromAmount));
            case "pound" -> unit.fromCupsUS(Double.parseDouble(fromAmount) * 2); // Generally, 1 pound = 2 cups.
            case "oz" -> unit.fromCupsUS(Double.parseDouble(fromAmount) / 8); // Generally, 8 oz = 1 cup.
        }
        switch (toMeasurement) {
            case "gallon" -> result = unit.toGallonsUS();
            case "quart" -> result = unit.toQuartsUS();
            case "pint" -> result = unit.toPintsUS();
            case "fl oz" -> result = unit.toFluidOuncesUS();
            case "cup" -> result = unit.toCupsUS();
            case "tsp" -> result = unit.toTeaspoonsUS();
            case "tbsp" -> result = unit.toTablespoonsUS();
            case "L" -> result = unit.toLiters();
            case "mL" -> result = unit.toMilliliters();
            case "pound" -> result = unit.toCupsUS() / 2; // Generally, 1 pound = 2 cups.
            case "oz" -> result = unit.toCupsUS() * 8; // Generally, 8 oz = 1 cup.
            default -> throw new IllegalStateException("Unexpected value: " + toMeasurement);
        }

        return result;
    }
}
