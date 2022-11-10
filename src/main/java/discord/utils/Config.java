package discord.utils;


import java.io.InputStream;
import java.util.Properties;

public class Config {
    public Properties properties;

    public Config() {
        this.properties = new Properties();

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(is);
        } catch (Exception e) {
            Logger.log(Logger.Level.FATAL, "Could not read from config.properties");
        }
    }

    public String getDiscordToken() {
        if (!properties.containsKey("discord.token") || properties.getProperty("discord.token").equals("")) {
            Logger.log(Logger.Level.FATAL, "Missing required field in application.properties");
        }
        return properties.getProperty("discord.token");
    }

    public String getCreateCategoryEmoji() {
        if (!properties.containsKey("discord.category.create_emoji") || properties.getProperty("discord.category.create_emoji").equals("")) {
            Logger.log(Logger.Level.FATAL, "Missing required field in application.properties");
        }
        return properties.getProperty("discord.category.create_emoji");
    }

    public String getVoiceCategoryEmoji() {
        if (!properties.containsKey("discord.category.voice_emoji") || properties.getProperty("discord.category.voice_emoji").equals("")) {
            Logger.log(Logger.Level.FATAL, "Missing required field in application.properties");
        }
        return properties.getProperty("discord.category.voice_emoji");
    }
}
