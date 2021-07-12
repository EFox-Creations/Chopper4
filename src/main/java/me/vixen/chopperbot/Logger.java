package me.vixen.chopperbot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	public static void log(String text) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true));
			final String time = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yy-MMM-dd HH:mm:ss.SSS"));
			writer.write(String.format("[%s]\s%s\n", time, text));
			writer.close();
			System.out.println(String.format("[%s]\s%s\n", time, text));
		} catch (Exception e) { }
	}

	public static void log(String text, Throwable e) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true));
			final String time = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yy-MMM-dd HH:mm:ss.SSS"));
			writer.write(String.format("[%s]\s%s: %s\n", time, text, e.getMessage()));
			writer.close();
		} catch (Exception ex) { }
	}
}