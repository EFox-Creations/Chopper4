package me.vixen.chopperbot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	public static void log(String text) {
		try {
			final String time = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yy-MMM-dd HH:mm:ss.SSS"));
			final String strToLog = String.format("[%s]\s%s", time, text);

			BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true));
			writer.write(strToLog + "\n");
			writer.close();
			System.out.println(strToLog);
			Entry.getChopperConsole().addToLog(strToLog);
		} catch (Exception e) {
			System.out.println("External Logging Failed");
		}
	}

	public static void log(String text, Throwable e) {
		try {
			final String time = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yy-MMM-dd HH:mm:ss.SSS"));
			final String strToLog = String.format("[%s]\s%s: %s", time, text, e.getMessage());

			BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true));
			writer.write(strToLog + "\n");
			writer.close();
			System.out.println(strToLog);
			Entry.getChopperConsole().addToLog(strToLog);
		} catch (Exception ex) {
			System.out.println("External Logging Failed");
		}
	}

	public static void debug(String text) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true));
			final String time = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yy-MMM-dd HH:mm:ss.SSS"));
			writer.write(String.format("[%s]\s%s\n", time, text));
			writer.close();
			System.out.printf("[%s] %s\n%n", time, text);
		} catch (Exception e) {
			System.out.println("External Logging Failed");
		}
	}

	public static String stacktrace() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < stackTraceElements.length; i++) {
			String clazzName = stackTraceElements[i].getClassName();
			String methodName = stackTraceElements[i].getMethodName();
			int lineNumber = stackTraceElements[i].getLineNumber();

			builder.append(i + "Class:" + clazzName + "#" + methodName + "@" + lineNumber);
			if (i != stackTraceElements.length) builder.append(" <- ");
		}

		return (builder.toString());
	}

	public static void logStacktrace() {
		log(stacktrace());
	}


}