package hr.cizmic.seebanking.util;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

// input filter that limits decimal places (e.g. only allow 2 decimals in money fields
public final class DecimalDigitsInputFilter implements InputFilter {

    private final Pattern pattern;

    // e.g. DecimalDigitsInputFilter(2) allows "12.34" but blocks "12.345"
    public DecimalDigitsInputFilter(int decimalDigits) {
        // pattern allows: digits, optional decimal point or comma, then up to n decimals
        this.pattern = Pattern.compile("\\d*(?:[\\.,]\\d{0," + decimalDigits + "})?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // build what text would look like after this edit
        String prefix = dest.subSequence(0, dstart).toString(); // text before cursor
        String insert = source.subSequence(start, end).toString(); // what user is typing
        String suffix = dest.subSequence(dend, dest.length()).toString(); // text after cursor
        String candidate = prefix + insert + suffix;
        // if it matches pattern, allow it (return null). otherwise block it (return ""
        return pattern.matcher(candidate).matches() ? null : "";
    }
}
