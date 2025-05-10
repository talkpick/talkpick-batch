package com.likelion.backendplus4.talkpick.batch.common.configuration.logging;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import jakarta.annotation.PostConstruct;

/**
 * Logback 설정을 위한 구성 클래스.
 * application.properties의 log.rolling.* 설정에 따라
 * 콘솔 및 파일 appender를 생성하고 루트 로거에 등록한다.
 *
 * @since 2025-05-10
 */
@Configuration
public class LogbackConfig {
	@Value("${log.rolling.directory}")
	private String LOG_DIRECTORY;
	@Value("${log.rolling.file-name}")
	private String LOG_FILE_NAME;
	@Value("${log.rolling.pattern}")
	private String LOG_PATTERN;
	@Value("${log.rolling.max-history}")
	private int MAX_HISTORY;
	@Value("${log.rolling.total-size-cap}")
	private String TOTAL_SIZE_CAP;

	/**
	 * 로그 설정을 초기화하고 콘솔 및 파일 appender를 구성한다.
	 *
	 * @author 정안식
	 * @since 2025-05-10
	 */
	@PostConstruct
	public void configure() {
		LoggerContext context = initializeLoggerContext();
		createLogDirectory();

		ConsoleAppender<ILoggingEvent> consoleAppender = createConsoleAppender(context);
		FileAppender<ILoggingEvent> fileAppender = createFileAppender(context);

		configureRootLogger(context, consoleAppender, fileAppender);
	}

	/**
	 * LoggerContext를 초기화하고 리셋하여 반환한다.
	 *
	 * @return 초기화된 LoggerContext 객체
	 * @author 정안식
	 * @since 2025-05-10
	 */
	private LoggerContext initializeLoggerContext() {
		LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
		context.reset();
		return context;
	}

	/**
	 * 로그 디렉토리를 생성한다. 존재하지 않을 경우 새로 생성한다.
	 *
	 * @author 정안식
	 * @since 2025-05-10
	 */
	private void createLogDirectory() {
		Path logPath = Paths.get(LOG_DIRECTORY);
		try {
			if (!Files.exists(logPath)) {
				Files.createDirectories(logPath);
			}
		} catch (Exception e) {
			throw new RuntimeException("로그 디렉토리 생성 실패", e);
		}
	}

	/**
	 * 콘솔 appender를 생성하여 반환한다.
	 *
	 * @param context LoggerContext 객체
	 * @return 생성된 ConsoleAppender
	 * @author 정안식
	 * @since 2025-05-10
	 */
	private ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext context) {
		ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
		appender.setContext(context);
		appender.setEncoder(createEncoder(context));
		appender.start();
		return appender;
	}

	/**
	 * 파일 appender를 생성하여 반환한다.
	 *
	 * @param context LoggerContext 객체
	 * @return 생성된 FileAppender
	 * @since 2025-05-10
	 */
	private FileAppender<ILoggingEvent> createFileAppender(LoggerContext context) {
		FileAppender<ILoggingEvent> appender = new FileAppender<>();
		appender.setContext(context);
		appender.setFile(LOG_DIRECTORY + "/" + LOG_FILE_NAME);
		appender.setAppend(true);
		appender.setEncoder(createEncoder(context));

		TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = createRollingPolicy(context, appender);
		rollingPolicy.start();

		appender.start();
		return appender;
	}

	/**
	 * PatternLayoutEncoder를 생성하여 반환한다.
	 *
	 * @param context LoggerContext 객체
	 * @return 생성된 PatternLayoutEncoder
	 * @since 2025-05-10
	 */
	private PatternLayoutEncoder createEncoder(LoggerContext context) {
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern(LOG_PATTERN);
		encoder.start();
		return encoder;
	}

	/**
	 * 롤링 정책을 생성하여 반환한다.
	 *
	 * @param context LoggerContext 객체
	 * @param parent  파일 appender
	 * @return 생성된 TimeBasedRollingPolicy
	 * @since 2025-05-10
	 */
	private TimeBasedRollingPolicy<ILoggingEvent> createRollingPolicy(LoggerContext context,
		FileAppender<ILoggingEvent> parent) {
		TimeBasedRollingPolicy<ILoggingEvent> policy = new TimeBasedRollingPolicy<>();
		policy.setContext(context);
		policy.setParent(parent);
		policy.setFileNamePattern(LOG_DIRECTORY + "/" + LOG_FILE_NAME.replace(".log", ".%d{yyyy-MM-dd}.log"));
		policy.setMaxHistory(MAX_HISTORY);
		policy.setTotalSizeCap(FileSize.valueOf(TOTAL_SIZE_CAP));
		return policy;
	}

	/**
	 * 루트 로거에 레벨 설정 및 appender를 등록한다.
	 *
	 * @param context         LoggerContext 객체
	 * @param consoleAppender ConsoleAppender 객체
	 * @param fileAppender    FileAppender 객체
	 * @since 2025-05-10
	 */
	private void configureRootLogger(LoggerContext context, ConsoleAppender<ILoggingEvent> consoleAppender,
		FileAppender<ILoggingEvent> fileAppender) {
		Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if (logger instanceof ch.qos.logback.classic.Logger) {
			ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)logger;
			rootLogger.setLevel(Level.INFO);
			rootLogger.addAppender(consoleAppender);
			rootLogger.addAppender(fileAppender);
		}
	}
}