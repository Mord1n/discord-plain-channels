package discord.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Integers {

    public static Integer findAllIntegersInString(String stringToSearch) {
        Matcher matcher = Pattern.compile("-?\\d+").matcher(stringToSearch);
        StringBuilder integersStr = new StringBuilder();
        while (matcher.find()) {
            integersStr.append(matcher.group());
        }
        return parseAllowed(integersStr.toString());
    }

    /**
     * Parse string to integer
     *
     * @param str of numbers
     * @return integer between 1 - 99 or Null
     */
    private static Integer parseAllowed(String str) {
        try {
            Integer i = Integer.parseInt(str);
            return (i > 0 && i < 100 ? i : null);
        } catch (Exception e) {
            return null;
        }
    }
}
