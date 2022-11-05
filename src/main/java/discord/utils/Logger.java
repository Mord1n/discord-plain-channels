package discord.utils;

import org.apache.logging.log4j.LogManager;

public class Logger {

    protected final static org.apache.logging.log4j.Logger logger = LogManager.getLogger("logger");

    public enum Level {TRACE, DEBUG, INFO, WARNING, ERROR, FATAL}

    public static void log(Level level, String msg) {
        switch (level) {
            case TRACE -> logger.trace(msg);
            case DEBUG -> logger.debug(msg);
            case INFO -> logger.info(msg);
            case WARNING -> logger.warn(msg);
            case ERROR -> logger.error(msg);
            case FATAL -> logger.fatal(msg);
        }
    }
}
