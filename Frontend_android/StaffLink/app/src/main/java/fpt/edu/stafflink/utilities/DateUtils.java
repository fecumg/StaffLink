package fpt.edu.stafflink.utilities;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static Date parseDate(String dateString, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + dateString);
        }
    }

    public static boolean validate(String dateString, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        if (StringUtils.isEmpty(dateString)) {
            return false;
        }
        try {
            Date date = sdf.parse(dateString);
            if (date == null) {
                return false;
            }
            String formattedDateString = sdf.format(date);
            return formattedDateString.equals(dateString);
        } catch (ParseException e) {
            return false;
        }
    }

    public static String dateToString(Date date, String pattern) {
        DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return dateFormat.format(date);
    }
}
