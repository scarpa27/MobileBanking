package hr.cizmic.seebanking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

// helper methods for parsing and formatting money amounts with 2 decimals
public final class MoneyUtils {

    private MoneyUtils() {}

    // parse user input like "12.5" or "12,5" into BigDecimal with 2 decimals
    public static BigDecimal parseAmount(String raw) {
        if (raw == null) return null;
        String cleaned = raw.trim().replaceAll("\\s+", "").replace(",", "."); // remove spaces, swap comma for dot
        if (cleaned.isEmpty()) return null;
        // fix cases like ".5" -> "0.5"
        if (cleaned.startsWith(".")) {
            cleaned = "0" + cleaned;
        }
        // fix cases like "5." -> "5.0"
        if (cleaned.endsWith(".")) {
            cleaned = cleaned + "0";
        }
        try {
            BigDecimal value = new BigDecimal(cleaned);
            if (value.scale() > 2) {
                return null; // reject if more than 2 decimals
            }
            return value.setScale(2, RoundingMode.HALF_UP); // always 2 decimals
        } catch (NumberFormatException ex) {
            return null; // invalid number
        }
    }

    // format amount as string with exactly 2 decimals (e.g. "12.50"
    public static String format(BigDecimal value) {
        if (value == null) return "0.00";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    // same as format but shows "--" instead of "0.00" for null
    public static String formatOrDash(BigDecimal value) {
        if (value == null) return "--";
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
