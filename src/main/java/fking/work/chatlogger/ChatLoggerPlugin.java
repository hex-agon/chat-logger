package fking.work.chatlogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name = "Clan Chat Logger")
public class ChatLoggerPlugin extends Plugin {

    private Logger logger;

    @Override
    protected void startUp() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss} %msg%n");
        encoder.start();

        String basePath = System.getProperty("user.home") + "/.runelite/clanchatlogs/";

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setFile(basePath + "latest.log");
        appender.setAppend(true);
        appender.setEncoder(encoder);
        appender.setContext(context);

        TimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new TimeBasedRollingPolicy<>();
        logFilePolicy.setContext(context);
        logFilePolicy.setParent(appender);
        logFilePolicy.setFileNamePattern(basePath + "chatlog_%d{yyyy-MM-dd}.log");
        logFilePolicy.setMaxHistory(30);
        logFilePolicy.start();

        appender.setRollingPolicy(logFilePolicy);
        appender.start();

        logger = context.getLogger(ChatLoggerPlugin.class);
        logger.detachAndStopAllAppenders();
        logger.setAdditive(false);
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {

        if (event.getType() == ChatMessageType.FRIENDSCHAT) {
            logger.info("[{}] {}: {}", event.getSender(), event.getName(), event.getMessage());
        }
    }
}
