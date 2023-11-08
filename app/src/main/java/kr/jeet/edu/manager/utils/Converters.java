package kr.jeet.edu.manager.utils;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;


@ProvidedTypeConverter
public class Converters {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @TypeConverter
    public static LocalDateTime fromString(String value) {
        try {
            return value == null ? null : LocalDateTime.parse(value, formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String fromDate(LocalDateTime date) {
        return date == null ? null : date.format(formatter);
    }
}
