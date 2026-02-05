package pl.holowko.intellij.tapestry.util;

import com.google.common.base.Ascii;

public final class Utils {

    public static String firstCharToUpper(String word) {
        return (word.isEmpty())
            ? word
            : new StringBuilder(word.length())
                .append(Ascii.toUpperCase(word.charAt(0)))
                .append(word.substring(1))
                .toString();
    }

    public static String firstCharToLower(String word) {
        return (word.isEmpty())
            ? word
            : new StringBuilder(word.length())
                .append(Ascii.toLowerCase(word.charAt(0)))
                .append(word.substring(1))
                .toString();
    }
    
    private Utils() {
    }
}
