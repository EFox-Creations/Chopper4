package me.vixen.chopperbot.Database;

import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.Logger;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.guilds.bejoijoplugins.Card;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.steppschuh.markdowngenerator.table.Table;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Database {

	private static Connection getConnection() {
		return Entry.dbHandler.getConnection();
	}

	public static void initDatabase(List<Guild> guilds) {
		createMemberTables(guilds);
		createWarningTables(guilds);
		createStickyTable();
		createCommandTable();
		createConfigTable();
	}

	public static String getGuildMemberTable(String guildId) {
		return String.format("'%s members'", guildId).replaceAll(" ", "");
	}

	public static String getGuildWarningTable(String guildId) {
		return String.format("'%s warnings'", guildId).replaceAll(" ", "");
	}

	// ************************************************************
	// *                      Table Creation                      *
	// ************************************************************

	public static void createMemberTables(List<Guild> guilds) {
		for (Guild g : guilds) {
			String guildMemberTable = getGuildMemberTable(g.getId());
			String SQL = " CREATE TABLE IF NOT EXISTS " + guildMemberTable + "(" +
				"user_id TEXT NOT NULL UNIQUE,\s" +
				"nickname TEXT NOT NULL,\s" +
				"authorized BOOL NOT NULL DEFAULT FALSE,\s" +
				"level_up_messages BOOL NOT NULL DEFAULT TRUE,\s" +
				"muted BOOL NOT NULL DEFAULT FALSE,\s" +
				"lst_msg_time TEXT,\s" +
				"unmute_time TEXT DEFAULT NULL,\s" +
				"gallery_remaining INTEGER NOT NULL DEFAULT 10,\s" +
				"chest_count INTEGER NOT NULL DEFAULT 1,\s" +
				"lockpick_skill INTEGER NOT NULL DEFAULT 1,\s" +
				"lock_count INTEGER NOT NULL DEFAULT 0,\s" +
				"exp INTEGER NOT NULL DEFAULT 0,\s" +
				"level INTEGER NOT NULL DEFAULT 0,\s" +
				"currency INTEGER NOT NULL DEFAULT 0,\s" +
				"lottery_plays INTEGER NOT NULL DEFAULT 0,\s" +
				"robbed_today BOOLEAN NOT NULL DEFAULT 0" +
				");";
			try (Connection con = getConnection(); Statement statement = con.createStatement()) {
				statement.execute(SQL);
			} catch (SQLException e) {
				Logger.log("Error creating Database Member Table", e);
				System.exit(1);
			}
		}
	}

	private static void createWarningTables(List<Guild> guilds) {
		for (Guild g : guilds) {
			String guildWarningTable = getGuildWarningTable(g.getId());
			String SQL = " CREATE TABLE IF NOT EXISTS " + guildWarningTable + "(" +
				"user_id TEXT NOT NULL UNIQUE,\s" +
				"warn_json TEXT NOT NULL DEFAULT \"[]\"" +
				");";
			try (Connection con = getConnection(); Statement statement = con.createStatement()) {
				statement.execute(SQL);
			} catch (SQLException e) {
				Logger.log("Error creating Database Table", e);
				System.exit(1);
			}
		}
	}

	private static void createStickyTable() {
		String SQL = """
				CREATE TABLE IF NOT EXISTS sticky (
				 channel_id text NOT NULL PRIMARY KEY UNIQUE,\s
				 message_id text NOT NULL\s
				);""";
		//Build and execute
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			stmt.execute(SQL);
		} catch (SQLException e) {
			Logger.log("Error creating Database Table", e);
			System.exit(1);
		}
	}

	private static void createCommandTable() {
		String SQL = """
				CREATE TABLE IF NOT EXISTS custom_commands (
				 guild_id text NOT NULL,\s
				 command_name text NOT NULL,\s
				 staff_only boolean NOT NULL DEFAULT true,\s
				 response text NOT NULL\s
				);""";
		//Build and execute
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			stmt.execute(SQL);
		} catch (SQLException e) {
			Logger.log("Error creating Database Table", e);
			System.exit(1);
		}
	}

	private static void createConfigTable() {
		String SQL = """
			CREATE TABLE IF NOT EXISTS configs (
			guild_id TEXT UNIQUE NOT NULL,\s
			config TEXT\s
			);""";
		//Build and execute
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			stmt.execute(SQL);
		} catch (SQLException e) {
			Logger.log("Error creating Database Table", e);
			System.exit(1);
		}
	}


	// ************************************************************
	// *                     Member Methods                      *
	// ************************************************************

	public static DBMember getMember(Guild g, String userId) {
		final String guildId = g.getId();
		String guildMemberTable = getGuildMemberTable(g.getId());
		String SQL = "SELECT * FROM " + guildMemberTable + " WHERE user_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				final String nickname = rs.getString("nickname");
				final boolean authorized = rs.getBoolean("authorized");
				final boolean lvlMessages = rs.getBoolean("level_up_messages");
				final String lstMsgTime = rs.getString("lst_msg_time");
				final String unmuteTime = rs.getString("unmute_time");
				final int galleryRemaining = rs.getInt("gallery_remaining");
				final int chestCount = rs.getInt("chest_count");
				final int lockpickSkill = rs.getInt("lockpick_skill");
				final int lockCount = rs.getInt("lock_count");
				final int exp = rs.getInt("exp");
				final int level = rs.getInt("level");
				final int currency = rs.getInt("currency");
				final int lotteryPlays = rs.getInt("lottery_plays");
				final boolean robbedToday = rs.getBoolean("robbed_today");
				ps.close();
				con.close();
				return new DBMember(userId, guildId, nickname, authorized, lvlMessages,
					OffsetDateTime.parse(lstMsgTime), resolveUnmuteTime(unmuteTime), galleryRemaining,
					chestCount, lockpickSkill, lockCount, exp, level, currency, lotteryPlays, robbedToday);
			}
			ps.close();
			con.close();
			return null;
		} catch (SQLException e) {
			Logger.log("Couldn't retrieve member", e);
			return null;
		}
	}

	private static OffsetDateTime resolveUnmuteTime(String unmuteString) {
		if (unmuteString == null || unmuteString.equalsIgnoreCase("NULL")) return null;
		else return OffsetDateTime.parse(unmuteString);
	}

	/**
	 * Inserts a new or Updates an existing {@link DBMember} profile
	 *
	 * @param dbmember The {@link DBMember} object
	 */
	public static void upsertMember(Guild guild, DBMember dbmember) {
		String guildMemberTable = getGuildMemberTable(guild.getId());
		String SQL = "INSERT INTO " + guildMemberTable + "(user_id,nickname,authorized,level_up_messages,muted,lst_msg_time," +
			"unmute_time,gallery_remaining,chest_count,lockpick_skill,lock_count,exp," +
			"level,currency,lottery_plays, robbed_today) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" +
			"ON CONFLICT (user_id) DO " +
			"UPDATE SET nickname = ?, muted = ?, level_up_messages = ?, lst_msg_time = ?, unmute_time = ?, " +
			"gallery_remaining = ?, authorized = ?, chest_count = ?, lockpick_skill = ?, lock_count = ?, exp = ?, " +
			"level = ?, currency = ?, lottery_plays = ?, robbed_today = ? WHERE user_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, dbmember.getUserId());
			ps.setString(2, dbmember.getNickname());
			ps.setBoolean(3, dbmember.isAuthorized());
			ps.setBoolean(4, dbmember.areLvlMsgsEnabled());
			ps.setBoolean(5, dbmember.isMuted());
			ps.setString(6, dbmember.getLstMsgTime().toString());
			ps.setString(7, dbmember.getUnmuteTime());
			ps.setInt(8, dbmember.getGalleryImgsLeft());
			ps.setInt(9, dbmember.getDailyChests());
			ps.setInt(10, dbmember.getSkill());
			ps.setInt(11, dbmember.getLockCount());
			ps.setInt(12, dbmember.getExp());
			ps.setInt(13, dbmember.getLevel());
			ps.setInt(14, dbmember.getCoins());
			ps.setInt(15, dbmember.getLottoPlaysLeft());
			ps.setBoolean(16, dbmember.hasRobbed());
			ps.setString(17, dbmember.getNickname());
			ps.setBoolean(18, dbmember.isMuted());
			ps.setBoolean(19, dbmember.areLvlMsgsEnabled());
			ps.setString(20, dbmember.getLstMsgTime().toString());
			ps.setString(21, dbmember.getUnmuteTime());
			ps.setInt(22, dbmember.getGalleryImgsLeft());
			ps.setBoolean(23, dbmember.isAuthorized());
			ps.setInt(24, dbmember.getDailyChests());
			ps.setInt(25, dbmember.getSkill());
			ps.setInt(26, dbmember.getLockCount());
			ps.setInt(27, dbmember.getExp());
			ps.setInt(28, dbmember.getLevel());
			ps.setInt(29, dbmember.getCoins());
			ps.setInt(30, dbmember.getLottoPlaysLeft());
			ps.setBoolean(31, dbmember.hasRobbed());
			ps.setString(32, dbmember.getUserId());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log("Couldn't Upsert Member", e);
		}
	}

	/**
	 *
	 * @param g The guild to search in
	 * @param minimumCoins The minimum number of coins a profile must have to be included
	 * @return a {@link List} with the {@link DBMember} that have more than the provided minimum coins
	 */
	public static List<DBMember> getDBMembersWithCoins(Guild g, int minimumCoins, String userIdToExclude) {
		String SQL = "SELECT user_id FROM ? WHERE currency >= ? AND user_id IS NOT ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, getGuildMemberTable(g.getId()));
			ps.setInt(2, minimumCoins);
			ps.setString(3, userIdToExclude);
			final ResultSet resultSet = ps.executeQuery();
			final Guild guild = Entry.jda.getGuildById(g.getId());
			List<DBMember> dbMembers = new ArrayList<>();
			while (resultSet.next())
				//noinspection ConstantConditions 99.99% likely to be non-null
				dbMembers.add(getMember(guild, resultSet.getString("user_id")));
			ps.close();
			con.close();
			return dbMembers;
		} catch (SQLException e) {
			return new ArrayList<>();
		}
	}

	/**
	 *
	 * @param numOfEntries The number of entries to return
	 * @return A String[] of {@link DBMember} names, exp, and coins ordered by exp DESC
	 */
	public static String[] getLeaderboard(Guild g, int numOfEntries) {
		String SQL = "SELECT nickname, exp, currency FROM " + getGuildMemberTable(g.getId()) +
			" ORDER BY exp DESC LIMIT " + numOfEntries;
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(SQL);
			List<String> entries = new ArrayList<>();
			while (rs.next()) {
				String nickname = (rs.getString("nickname") + (" ".repeat(15))).substring(0, 9) + "    ";
				String exp = (rs.getString("exp_value") + (" ".repeat(15))).substring(0,14);
				String currency = (rs.getString("currency_value") +  (" ".repeat(15))).substring(0,14);
				entries.add("`" + nickname + exp + currency + "`");
			}
			stmt.close();
			con.close();
			if (entries.isEmpty())
				return null;
			return (String[]) entries.toArray();
		} catch (SQLException e) {
			return null;
		}
	}

	// ************************************************************
	// *                     Command Methods                      *
	// ************************************************************

	/**
	 * @return {@link List} of all {@link Command} for the provided guild
	 */
	public static List<Command> getCommands(Guild g) {
		String SQL = "SELECT command_name, staff_only, response FROM custom_commands WHERE guild_id = " + g.getId();
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			List<Command> commands = new ArrayList<>();
			ResultSet rs = stmt.executeQuery(SQL);
			while (rs.next()) {
				String name = rs.getString("command_name");
				boolean staffOnly = rs.getBoolean("staff_only");
				String response = rs.getString("response");
				Command cmd = new Command.Builder()
					.setName(name)
					.setStaffOnly(staffOnly)
					.setResponse(response)
					.build();
				commands.add(cmd);
			}
			stmt.close();
			con.close();
			return commands;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 *
	 * @param commandName The name of the command to search
	 * @return {@link Command} with the provided name, or null, if it does not exist
	 */
	@Nullable
	@CheckReturnValue
	public static Command getCommandByName(Guild g, String commandName) {
		String SQL = "SELECT * FROM custom_commands WHERE command_name = ? AND guild_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, commandName);
			ps.setString(2, g.getId());
			final ResultSet resultSet = ps.executeQuery();
			if (resultSet.next()) {
				String name = resultSet.getString("command_name");
				boolean staffOnly = resultSet.getBoolean("staff_only");
				String response = resultSet.getString("response");
				ps.close();
				con.close();
				return new Command.Builder()
					.setName(name)
					.setStaffOnly(staffOnly)
					.setResponse(response)
					.build();
			} else {
				ps.close();
				con.close();
				return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}

	/**
	 *
	 * @param cmd The {@link Command} to add
	 * @return True if write to database successful
	 */
	@CheckReturnValue
	public static boolean addCommand(Guild g, Command cmd) {
		String SQL = "INSERT INTO custom_commands(guild_id,command_name,staff_only,response) VALUES(?,?,?,?)";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, g.getId());
			ps.setString(2, cmd.getName());
			ps.setBoolean(3, cmd.isStaffOnly());
			ps.setString(4, cmd.getResponse());
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
			return false;
		}
	}

	/**
	 *
	 * @param commandName The name of the command to delete
	 * @return True if the command was successfully deleted
	 */
	@CheckReturnValue
	public static boolean deleteCommand(Guild g, @Nonnull String commandName) {
		String SQL = "DELETE FROM custom_commands WHERE command_name = ? AND guild_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, commandName);
			ps.setString(2, g.getId());
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}


	/**
	 * @param oldname The current name for the command
	 * @param newname The new name for the command
	 * @return True if successful
	 */
	@CheckReturnValue
	public static boolean changeCommandName(Guild g, String oldname, String newname) {
		String SQL = "UPDATE custom_commands SET command_name = ? WHERE command_name = ? AND guild_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, newname);
			ps.setString(2, oldname);
			ps.setString(3, g.getId());
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			Logger.log("Command name couldn't be changed", e);
			return false;
		}
	}

	/**
	 *
	 * @param commandName The name of the command
	 * @param staffOnly boolean - Should the command be staff only?
	 * @return True if successfully changed
	 */
	@CheckReturnValue
	public static boolean changeStaffOnly(Guild g, String commandName, boolean staffOnly) {
		String SQL = "UPDATE custom_commands SET staff_only = ? WHERE command_name = ? AND guild_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setBoolean(1, staffOnly);
			ps.setString(2, commandName);
			ps.setString(3, g.getId());
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 *
	 * @param commandName The name of the command to alter
	 * @param response The new response of the command
	 * @return True if successful
	 */
	public static boolean changeResponse(Guild g, String commandName, String response) {
		String SQL = "UPDATE custom_commands SET response = ? WHERE command_name = ? AND guild_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, response);
			ps.setString(2, commandName);
			ps.setString(3, g.getId());
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	// ************************************************************
	// *                      Sticky Methods                      *
	// ************************************************************

	/**
	 *
	 * @param channel The channel id
	 * @param message The new message id
	 * @return True, if successfully updated
	 */
	public static boolean upsertSticky(TextChannel channel, Message message) {
		String SQL = "INSERT INTO sticky(channel_id, message_id) VALUES(?,?)" +
			"ON CONFLICT (channel_id) DO" +
			"UPDATE SET message_id = ? WHERE channel_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, channel.getId());
			ps.setString(2, message.getId());
			ps.setString(3, message.getId());
			ps.setString(4, channel.getId());
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			Logger.log("Error updating sticky", e);
			return false;
		}
	}

	@Nullable
	public static String getStickyId(TextChannel channel) {
		String SQL = "SELECT message_id FROM sticky WHERE channel_id = " + channel.getId();
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			ResultSet rs = stmt.executeQuery(SQL);
			if (rs.next()) {
				String msgId = rs.getString("message_id");
				stmt.close();
				con.close();
				return msgId;
			}
			stmt.close();
			con.close();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
			return null;
		}
	}

	/**
	 * @param channel The channel to delete the sticky message in
	 * @return True, if successful
	 */
	public static boolean deleteSticky(TextChannel channel) {
		String SQL = "DELETE FROM sticky WHERE channel_id = " + channel.getId();
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			stmt.executeUpdate(SQL);
			stmt.close();
			con.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	// ************************************************************
	// *                     Mass Edit Methods                    *
	// ************************************************************

	/**
	 *
	 * @param basicPatreonIds A list of users entitled to basic patreon benefits
	 * @param premiumPatreonIds A list of users entitled to premium patreon benefits
	 * @param chopAndBasic A list of users entitled to basic patreon benefits and are supporting chopper
	 * @param chopAndPremium A list of users entitled to premium patreon benefits and are supporting chopper
	 */
	public static void resetDailyCounts(Guild g, List<String> basicPatreonIds, List<String> premiumPatreonIds,
										   List<String> chopAndBasic, List<String> chopAndPremium) {
		try (Connection con = getConnection()) {
			con.setAutoCommit(false);
			String SQL = "UPDATE ? SET gallery_pics = 0, daily_chest_count = 1, claimed_card = 0, robbed_today = 0, lottery_plays = 0, lock_count = 1";
			PreparedStatement ps = con.prepareStatement(SQL);
			ps.setString(1, getGuildMemberTable(g.getId()));
			ps.executeUpdate();

			SQL = "UPDATE ? SET daily_chest_count = 2 WHERE user_id = ?";
			ps = con.prepareStatement(SQL);
			for (String id : basicPatreonIds) {
				ps.setString(1, getGuildMemberTable(g.getId()));
				ps.setString(2, id);
				ps.addBatch();
			}
			ps.executeBatch();

			SQL = "UPDATE ? SET daily_chest_count = 3 WHERE user_id = ?";
			ps = con.prepareStatement(SQL);
			for (String id : premiumPatreonIds) {
				ps.setString(1, getGuildMemberTable(g.getId()));
				ps.setString(2, id);
				ps.addBatch();
			}
			ps.executeBatch();

			SQL = "UPDATE ? SET daily_chest_count = 3 WHERE user_id = ?";
			ps = con.prepareStatement(SQL);
			for (String id : chopAndBasic) {
				ps.setString(1, getGuildMemberTable(g.getId()));
				ps.setString(2, id);
				ps.addBatch();
			}
			ps.executeBatch();

			SQL = "UPDATE ? SET daily_chest_count = 4 WHERE user_id = ?";
			ps = con.prepareStatement(SQL);
			for (String id : chopAndPremium) {
				ps.setString(1, getGuildMemberTable(g.getId()));
				ps.setString(2, id);
				ps.addBatch();
			}
			ps.executeBatch();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
		}
	}

	// ************************************************************
	// *                     Warning Methods                      *
	// ************************************************************

	public static List<Warning> getWarnings(String guildId, String userId) {
		String SQL = "SELECT warn_json FROM " + getGuildWarningTable(guildId) + " WHERE user_id = " + userId;
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(SQL);
			List<Warning> warnings = new ArrayList<>();
			if (rs.next())
				 warnings = Warning.deserializeList(rs.getString("warn_json"));
			stmt.close();
			con.close();
			return warnings;
		} catch (SQLException e) {
			Logger.log("Couldn't load warnings", e);
			return null;
		}
	}

	public static void setWarnings(DBMember member) {
		String tableName = getGuildWarningTable(member.getGuildId());
		String userId = member.getUserId();
		String json = member.warningsAsJSON();
		String SQL =
			"INSERT INTO " + tableName + "(user_id,warn_json)\s" +
			"VALUES(" + userId + ",'" + json + "')\s" +
			"ON CONFLICT (user_id) DO\s" +
			"UPDATE SET warn_json = '" + json + "' WHERE user_id = " + userId;
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			stmt.executeUpdate(SQL);
		} catch (SQLException e) {
			Logger.log("Couldn't set warnings", e);
		}
	}

	// ************************************************************
	// *                       Card Methods                       *
	// ************************************************************

	/**
	 *
	 * @return The next card number (-1 if error occurs)
	 */
	@CheckReturnValue
	public static int getNextCardId() {
		String SQL = "SELECT card_id FROM cards ORDER BY card_id DESC LIMIT 1";
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			final ResultSet resultSet = stmt.executeQuery(SQL);
			int returnInt = 1;
			if (resultSet.next()) returnInt = (resultSet.getInt("card_id") + 1);
			stmt.close();
			con.close();
			return returnInt;
		} catch (SQLException e ) {
			e.printStackTrace();
			Logger.log(e.getMessage());
			return -1;
		}
	}

	/**
	 *
	 * @param userId The id of the owner of the card
	 * @param card The {@link Card} object
	 * @return True, if successfully added
	 */
	public static boolean addCard(String userId, Card card) {
		String SQL = "INSERT INTO cards(card_id, user_id, card_face, card_rarity) VALUES(?,?,?,?)";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setInt(1, getNextCardId());
			ps.setString(2, userId);
			ps.setString(3, card.getFaceAsString());
			ps.setString(4, card.getRarityAsString());
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e ) {
			e.printStackTrace();
			Logger.log(e.getMessage());
			return false;
		}
	}

	/**
	 *
	 * @param userId The id of the user to search for
	 * @return A {@link List} of {@link Card}s that the specified user owns
	 */
	public static List<Card> getOwnedCards(String userId) {
		String SQL = "SELECT card_id, card_face, card_rarity FROM cards WHERE user_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, userId);
			final ResultSet rs = ps.executeQuery();
			List<Card> ownedCards = new ArrayList<>();
			while (rs.next()) {
				final int id = rs.getInt("card_id");
				final String cardFace = rs.getString("card_face");
				final String cardRarity = rs.getString("card_rarity");
				ownedCards.add(new Card.Builder()
					.setId(id)
					.setFace(cardFace)
					.setRarity(cardRarity)
					.build()
				);
			}
			ps.close();
			con.close();
			return ownedCards;
		} catch (SQLException e ) {
			e.printStackTrace();
			Logger.log(e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 *
	 * @param userId The user id to check for
	 * @param cardId The id of the {@link Card}
	 * @return True, if the specified user owns the card
	 */
	public static boolean doesUserOwnCard(String userId, int cardId) {
		String SQL = "SELECT user_id FROM cards WHERE card_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setInt(1, cardId);
			ResultSet rs = ps.executeQuery();
			boolean owns = false;
			if (rs.next())
				owns = rs.getString("user_id").equalsIgnoreCase(userId);
			ps.close();
			con.close();
			return owns;
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * <br>Gets information from the database and builds a {@link Card}<br>
	 *
	 * @param cardId The id number of the card to get
	 * @return {@link Card} with the provided Id. Null if the id does not exist or an error occurs
	 */
	@Nullable @CheckReturnValue
	public static Card getCardById(int cardId) {
		String SQL = "SELECT card_id, card_face, card_rarity FROM cards WHERE card_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setInt(1, cardId);
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				final int id = rs.getInt("card_id");
				final String cardFace = rs.getString("card_face");
				final String cardRarity = rs.getString("card_rarity");
				ps.close();
				con.close();
				return new Card.Builder()
					.setFace(cardFace)
					.setId(id)
					.setRarity(cardRarity)
					.build();
			} else {
				ps.close();
				con.close();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
			return null;
		}
	}

//	/**
//	 *
//	 * @param cardId The id of the {@link Card}
//	 * @param newOwnerId The user id of the new owner
//	 * @return True, if changed successfully
//	 */
//	public static boolean setNewCardOwner(int cardId, String newOwnerId) {
//		String SQL = "UPDATE cards SET user_id = ? WHERE card_id = ?";
//		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
//			ps.setString(1, newOwnerId);
//			ps.setInt(2, cardId);
//			ps.executeUpdate();
//			ps.close();
//			con.close();
//			return true;
//		} catch (SQLException e) {
//			return false;
//		}
//	}

	/**
	 *
	 * @param cardId The id of the {@link Card} to delete
	 * @return True, if deleted successfully
	 */
	public static boolean deleteCard(int cardId) {
		String SQL = "DELETE FROM cards WHERE card_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setInt(1, cardId);
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 *
	 * @param cardIds An Integer {@link Collection} of {@link Card} ids
	 */
	public static void deleteCards(Collection<Integer> cardIds) {
		String SQL = "DELETE FROM cards WHERE card_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			for (int i : cardIds) {
				ps.setInt(1, i);
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
		}
	}

	/**
	 *
	 * Returns a list of cards from a specified guild that have been filtered both by owner Id and CardFace.
	 *
	 * @param cardFace the {@link Card.CardFace Card Face} to filter by
	 * @param userId The id of the user to filter by
	 * @return A possibly empty {@link List} of {@link Card}s that the specified user owns with the specified {@link Card.CardFace Card Face}.
	 * Null, if an error occurs.
	 */
	@Nullable
	@CheckReturnValue
	public static List<Card> getCardsByFaceWithOwner(Card.CardFace cardFace, String userId) {
		String SQL = "SELECT * FROM cards WHERE user_id = ? AND card_face = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, userId);
			ps.setString(2, cardFace.toString());
			final ResultSet rs = ps.executeQuery();
			List<Card> cards = new ArrayList<>();
			while (rs.next()) {
				cards.add(new Card.Builder()
					.setFace(rs.getString("card_face"))
					.setRarity(rs.getString("card_rarity"))
					.setId(rs.getInt("card_id"))
					.build()
				);
			}
			ps.close();
			con.close();
			return cards;
		} catch (SQLException e) {
			return null;
		}
	}

	// ************************************************************
	// *                      Lotto Methods                       *
	// ************************************************************

	public static int getPot() {
		String SQL = "SELECT value FROM lotto WHERE key = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, "pot");
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				final int pot = rs.getInt("value");
				ps.close();
				con.close();
				return pot;
			} else {
				ps.close();
				con.close();
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
			return -1;
		}
	}

	/*
	public static boolean setPot(int amount) {
		String SQL = String.format("UPDATE lotto SET value = %s WHERE key = \"pot\"", amount);
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			stmt.executeUpdate(SQL);
			stmt.close();
			con.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	*/

	public static boolean addToPot(int amount) {
		final int pot = getPot();
		amount = Math.min(pot + amount, Integer.MAX_VALUE);
		String SQL = "UPDATE lotto SET value = ? WHERE key = 'pot'";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, String.valueOf(amount));
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(e.getMessage());
			return false;
		}
	}

	public static boolean doesBetExist(String userId) {
		String SQL = "SELECT user_id FROM bets WHERE user_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, userId);
			final ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ps.close();
				con.close();
				return true;
			} else {
				ps.close();
				con.close();
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
	}

	/*
	 *   Bet String:  "84,32,63,87,90"
	 */
	public static boolean addBet(String userId, String betString) {
		String SQL = "INSERT INTO bets(user_id,bet) VALUES(?,?)";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, userId);
			ps.setString(2, betString);
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

//	/**
//	 *
//	 * @param winBetString The winning bet
//	 * @return Possibly null Id String of user that made the first winning bet
//	 */
//	public static String getWinningBet(String winBetString) {
//		String SQL = "SELECT user_id FROM bets WHERE bet = " + winBetString;
//		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
//			final ResultSet rs = stmt.executeQuery(SQL);
//			String winnerId = null;
//			while (rs.next()) winnerId = rs.getString("user_id");
//			stmt.close();
//			con.close();
//			return winnerId;
//		} catch (SQLException e) {
//			return null;
//		}
//	}

	/*
	public static void deleteAllBets() {
		String SQL = "DELETE FROM bets";
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			stmt.executeUpdate(SQL);
		} catch (SQLException e) {
			Logger.log("Failed to delete lotto bets", e);
		}
	}*/

	// ************************************************************
	// *                      Config Methods                      *
	// ************************************************************

	public static Config getConfig(String guildId) {
		String SQL = "SELECT config FROM configs WHERE guild_id = " + guildId;
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(SQL);
			if (rs.next()) {
				String result = rs.getString("config");
				stmt.close();
				con.close();
				return Config.deserializeConfig(result);
			} else {
				stmt.close();
				con.close();
				return null;
			}
		} catch (SQLException e) {
			Logger.log("Failed to Upsert config:", e);
			return null;
		}
	}

	public static boolean setConfig(String guildId, String jsonOfConfig) {
		String SQL = """
				INSERT INTO configs(guild_id, config) VALUES(?,?)
				ON CONFLICT (guild_id) DO
				UPDATE SET config = ? WHERE guild_id = ?
				""";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(SQL)) {
			ps.setString(1, guildId);
			ps.setString(2, jsonOfConfig);
			ps.setString(3, jsonOfConfig);
			ps.setString(4, guildId);
			ps.executeUpdate();
			ps.close();
			con.close();
			return true;
		} catch (SQLException e) {
			Logger.log("Failed to Upsert config:", e);
			return false;
		}
	}

	// ************************************************************
	// *                   Remote Access Methods                  *
	// ************************************************************

	public static boolean execute(String sql) {
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			stmt.executeUpdate(sql);
			stmt.close();
			con.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public static Table query(String sql) {
		try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
			//Get results
			ResultSet rs = stmt.executeQuery(sql);
			//Get meta
			ResultSetMetaData meta = rs.getMetaData();
			//get column names
			int columnCount = meta.getColumnCount();
			List<String> columns = new ArrayList<>();
			for (int i=1; i<=columnCount; i++)
				columns.add(meta.getColumnName(i));
			//fetch rows
			List<List<String>> table = new ArrayList<>();

			while (rs.next()) {
				List<String> row = new ArrayList<>();
				for (String colName: columns) {
					String val = rs.getString(colName);
					row.add(val);
				}
				table.add(row);
			}
			//close statement
			stmt.close();
			con.close();

			//Construct table
			Table.Builder tblBuilder = new Table.Builder()
				.withAlignment(Table.ALIGN_CENTER)
				.addRow(columns);
			for (List<String> row : table)
				tblBuilder.addRow(row);
			return tblBuilder.build();
		} catch (SQLException e) {
			return null;
		}
	}
}
