package discord.utils;

public class Config {

    private final String token;
    private final String voiceCategoryEmoji;
    private final String createCategoryEmoji;

    public Config() {
        this.token = System.getenv("token");
        this.voiceCategoryEmoji = System.getenv("voiceEmoji");
        this.createCategoryEmoji = System.getenv("createEmoji");
    }

    public String getToken() {
        return this.token;
    }

    public String getCreateCategoryEmoji() {
        return this.createCategoryEmoji == null || this.createCategoryEmoji.isEmpty() ?
            ":white_small_square:" :
            this.createCategoryEmoji;
    }

    public String getVoiceCategoryEmoji() {
        return this.voiceCategoryEmoji == null || this.voiceCategoryEmoji.isEmpty() ?
            ":black_small_square:" :
            this.voiceCategoryEmoji;
    }
}
