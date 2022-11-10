package discord.utils;

import com.vdurmont.emoji.EmojiParser;

public class Emoji {
    public static boolean endsWithEmoji(String searchString, String emojiStr) {
        String[] s = searchString.split(" ");
        return EmojiParser.parseToAliases(s[s.length - 1])
            .equalsIgnoreCase(emojiStr);
    }
}
