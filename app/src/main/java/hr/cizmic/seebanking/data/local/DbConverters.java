package hr.cizmic.seebanking.data.local;

import androidx.room.TypeConverter;

import java.math.BigDecimal;

// type converters (lets Room store BigDecimal as strings in db)
public final class DbConverters {

    private DbConverters() {}

    // convert BigDecimal to string for storing in db
    @TypeConverter
    public static String bigDecimalToString(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    // convert string from db back to BigDecimal
    @TypeConverter
    public static BigDecimal stringToBigDecimal(String value) {
        if (value == null) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
