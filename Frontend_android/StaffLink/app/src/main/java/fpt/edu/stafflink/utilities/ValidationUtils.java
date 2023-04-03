package fpt.edu.stafflink.utilities;

import org.apache.commons.lang3.StringUtils;

public class ValidationUtils {
    private static final String EMAIL_PATTERN = "[a-zA-Z\\d._-]+@[a-z]+\\.[a-z]+\\.?[a-z]{0,3}";

    public static boolean isValidEmail(String email) {
        return StringUtils.isNotEmpty(email) && email.matches(EMAIL_PATTERN);
    }
}
