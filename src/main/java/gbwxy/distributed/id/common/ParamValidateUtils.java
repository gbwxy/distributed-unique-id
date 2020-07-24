package gbwxy.distributed.id.common;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * 描述：
 *
 * @Author wangjun
 * @Date 2020/7/22
 */
public class ParamValidateUtils {
    private static final String LETTER_EXPRESSION = "[a-z]";
    private static final String DIGITAL_EXPRESSION = "[0-9]";
    private static final int MIN_LEN = 1;
    private static final int MAX_LEN = 5;
    private static final char[] SPECIAL_CHARACTERS = new char[]{'-', '_'};

    public ParamValidateUtils() {
    }

    public static boolean validateProdName(String productionName) {
        return validNotNull(productionName) && validLen(productionName) && (validateDigital(productionName) || validateLetters(productionName) || validateCharacters(productionName)) && (validateDigitalFirst(productionName) || validateLettersFirst(productionName));
    }

    private static boolean validNotNull(String param) {
        return StringUtils.isNotBlank(param);
    }

    private static boolean validLen(String param) {
        return param.length() >= 1 && param.length() <= 5;
    }

    private static boolean validateDigital(String param) {
        return Pattern.compile("[0-9]").matcher(param).find();
    }

    private static boolean validateDigitalFirst(String param) {
        return Pattern.compile("[0-9]").matcher(param.substring(0, 1)).find();
    }

    private static boolean validateLetters(String param) {
        return Pattern.compile("[a-z]").matcher(param).find();
    }

    private static boolean validateLettersFirst(String param) {
        return Pattern.compile("[a-z]").matcher(param.substring(0, 1)).find();
    }

    private static boolean validateCharacters(String param) {
        for(int i = 0; i < SPECIAL_CHARACTERS.length; ++i) {
            if (param.indexOf(SPECIAL_CHARACTERS[i]) != -1) {
                return true;
            }
        }

        return false;
    }

    public static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }
}
