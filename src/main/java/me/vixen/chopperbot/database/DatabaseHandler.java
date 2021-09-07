package me.vixen.chopperbot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.vixen.chopperbot.Logger;
import net.dv8tion.jda.api.JDA;

import java.sql.Connection;

public class DatabaseHandler {
	private final HikariDataSource pool;

	public DatabaseHandler(JDA jda) {
		this.pool = initHikari();
		Database.initDatabase(this, jda.getGuilds());

		Logger.log("DatabaseLoaded");
	}

	private HikariDataSource initHikari() {
		HikariConfig config = new HikariConfig();
		config.setMaximumPoolSize(10);
		config.setJdbcUrl("jdbc:sqlite:database/data.db");
		config.setMinimumIdle(5);
		config.setConnectionTimeout(15000);

		try {
			return new HikariDataSource(config);
		} catch (Exception e) {
			Logger.log("DB Connection Failure", e);
			System.exit(1);
			return null;
		}
	}

	public Connection getConnection() {
		try {
			return pool.getConnection();
		} catch (Exception e) {
			Logger.log("Connection get failed...", e);
			return getConnection();
		}
	}

	@SuppressWarnings("unused")
	public void close() {
		try {
			pool.close();
			Logger.log("Database Closed");
		} catch (Exception e) {
			Logger.log("Database FAILED to close", e);
		}
	}
}