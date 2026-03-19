package hr.cizmic.seebanking.util;

import android.util.Patterns;

import java.util.regex.Pattern;

// helper methods for validating and cleaning user input
public final class ValidationUtils {

    // phone must start with optional + then 6-20 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d{6,20}$");

    private ValidationUtils() {}

    // trim whitespace, return null if empty
    public static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // clean up phone number: remove spaces
    public static String normalizePhone(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.replaceAll("\\s+", ""); // remove all whitespace
    }

    // check if phone format is valid (optional +, then 6-20 digits)
    public static boolean isValidPhone(String value) {
        return value != null && PHONE_PATTERN.matcher(value).matches();
    }

    // clean up email: remove spaces
    public static String normalizeEmail(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.replaceAll("\\s+", ""); // remove all whitespace
    }

    // check if email format is valid and not too long
    public static boolean isValidEmail(String value) {
        return value != null
                && value.length() <= 120 // max 120 chars
                && Patterns.EMAIL_ADDRESS.matcher(value).matches();
    }

    // clean up IBAN: remove spaces and uppercase it
    public static String normalizeIban(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.replaceAll("\\s+", "").toUpperCase(); // remove spaces, make uppercase
    }
}
