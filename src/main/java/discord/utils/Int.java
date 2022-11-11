package discord.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Int {

    private Integer value;

    public Int() {
        this.value = null;
    }

    public Integer getValue() {
        return value;
    }

    public Int withFallback(Integer fallback) {
        if (this.value == null) {
            this.value = fallback;
        }
        return this;
    }

    public Int findAllIntegersInString(String stringToSearch) {
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
    private Int parseAllowed(String str) {
        try {
            this.value = Integer.parseInt(str);
            if (this.value < 0 || this.value >= 100) {
                this.value = null;
            }
        } catch (Exception e) {
            this.value = null;
        }
        return this;
    }
}
