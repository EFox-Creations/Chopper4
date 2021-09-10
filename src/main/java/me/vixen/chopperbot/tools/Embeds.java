package me.vixen.chopperbot.tools;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class Embeds {

	public enum Colors {
		RED(Color.RED),
		BLACK(Color.BLACK),
		BLUE(new Color(0,55,255)),
		YELLOW(new Color(255,255,0)),
		WHITE(new Color(255,255,255)),
		FOXORANGE(new Color(204,112,0)),
		@SuppressWarnings("unused")
		ORANGE(new Color(255,127,0));

		private final Color color;

		Colors(Color clr) {
			this.color = clr;
		}

		public Color get() {
			return this.color;
		}
	}

	private static final String BUG_ERROR = "If this is an error, submit a bug report";

	public static MessageEmbed getOnJoin() {
		return new EmbedBuilder()
			.setColor(Colors.FOXORANGE.get())
			.setTitle("Hello! I'm Chopper! I'm happy to be here!")
			.addField("A couple things to note:",
				"On custom response commands, if it is marked as Staff Only, only authorized members may use them!\n"
					+ "Since I just joined, only the Owner, and my Creator, are authorized. Only authorized members may authorize" +
					"new people with /mod authorize", false)
			.addField("Additionally:", "Some commands may not function correctly until an authorized member completes" +
				" the `/config set` setup", false)
			.addField("Any other questions?", "Use [Home Base](https://discord.gg/PmmWAka) to ask!", false)
			.build();
	}

	public static MessageEmbed getLevelUpEmbed(int level) {
		return new EmbedBuilder()
			.setColor(Color.ORANGE)
			.setTitle("🔼 Level Up! " + level + "🔼")
			.build();
	}

	public static MessageEmbed getSelfVote() {
		return new EmbedBuilder()
			.setTitle("⚠ No Self Voting")
			.setDescription("You may not vote for your own idea!" + BUG_ERROR)
			.setColor(Colors.YELLOW.get())
			.build();
	}

	public static MessageEmbed getGalleryRestrict() {
		return new EmbedBuilder()
			.setTitle("⚠ Message Deleted")
			.setDescription("Your message was deleted because it was not a link, " +
				"attachment, or you have exceeded your daily limit\n" + BUG_ERROR)
			.setColor(Colors.YELLOW.get())
			.build();
	}

	public static MessageEmbed getCommandMissing() {
		return new EmbedBuilder()
			.setTitle("🚫 This command does not exist here 🚫")
			.setDescription(BUG_ERROR)
			.setColor(Colors.RED.get())
			.build();
	}

	public static MessageEmbed getPermissionMissing() {
		return new EmbedBuilder()
			.setTitle("🚫 You do not have the correct permissions for this 🚫")
			.setDescription(BUG_ERROR)
			.setColor(Colors.RED.get())
			.build();
	}

	public static MessageEmbed getAlreadyClaimed() {
		return new EmbedBuilder()
			.setTitle("🚫 You have already claimed your daily chests! 🚫")
			.setDescription(BUG_ERROR)
			.setColor(Colors.RED.get())
			.build();
	}

	public static MessageEmbed getInsufficientCoins() {
		return new EmbedBuilder()
			.setTitle("🚫 You do not have enough coins for that! 🚫")
			.setDescription(BUG_ERROR)
			.setColor(Colors.RED.get())
			.build();
	}

	public static MessageEmbed getUnknownMember() {
		return new EmbedBuilder()
			.setTitle("❓ I couldn't seem to find that person in this guild ❓")
			.setDescription(BUG_ERROR)
			.setColor(Colors.YELLOW.get())
			.build();
	}

	public static MessageEmbed getPleaseDoConfig() {
		return new EmbedBuilder()
			.setTitle("⚠ Please have an authorized user complete config! ⚠")
			.setDescription("An authorized user needs to complete /config set")
			.build();
	}

	public static MessageEmbed getWelcomeEmbed(User user) {
		return new EmbedBuilder()
			.setAuthor(user.getAsTag() + " joined", null,
				user.getAvatarUrl() == null ? "https://cdn.discordapp.com/embed/avatars/0.png" : user.getAvatarUrl())
			.setDescription("Account Age: " + timeBetween(OffsetDateTime.now(), user.getTimeCreated()))
			.setFooter("Id: " + user.getId())
			.setTimestamp(Instant.now())
			.setColor(Colors.WHITE.get())
			.build();
	}

	public static MessageEmbed getLeaveEmbed(User user) {
		return new EmbedBuilder()
			.setAuthor(user.getAsTag(), null,
				user.getAvatarUrl() == null ? "https://cdn.discordapp.com/embed/avatars/0.png" : user.getAvatarUrl())
			.setTitle("Left the guild")
			.setFooter("Id: " + user.getId())
			.setColor(Colors.WHITE.get())
			.build();
	}

	public static MessageEmbed getUserInfo(User user) {
		return new EmbedBuilder()
			.setAuthor(user.getAsTag(), null,
				user.getAvatarUrl() == null ? "https://cdn.discordapp.com/embed/avatars/0.png" : user.getAvatarUrl())
			.setDescription("***USER IS NOT IN THIS GUILD***")
			.setThumbnail(user.getAvatarUrl())
			.setColor(Colors.BLUE.get())
			.setFooter("Id: " + user.getId())
			.build();
	}

	public static MessageEmbed getMemberInfo(Member member) {
		String timeJoined = member.getTimeJoined().format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy HH:mm"));
		StringBuilder builder = new StringBuilder();
		for (Role r : member.getRoles()) {
			builder.append(r.getAsMention()).append("\n");
		}
		String roles = builder.toString().trim();
		User user = member.getUser();
		return new EmbedBuilder()
			.setAuthor(user.getAsTag(), null,
				user.getAvatarUrl() == null ? "https://cdn.discordapp.com/embed/avatars/0.png" : user.getAvatarUrl())
			.setTitle(member.getAsMention())
			.setColor(Color.CYAN)
			.addField("Joined", timeJoined, true)
			.addField("Registered", member.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy HH:mm")), true)
			.addField("Roles [" + member.getRoles().size() + "]", roles, false)
			.setFooter("Id: " + member.getUser().getId())
			.build();
	}

	public static MessageEmbed getInvalidArgumentEmbed(String argumentName, String errorText) {
		return new EmbedBuilder()
			.setColor(Colors.RED.get())
			.setTitle("❗ Invalid Argument")
			.addField(argumentName + " is incorrect!", errorText, false)
			.build();
	}

	public static MessageEmbed getAvatarEmbed(User user) {
		return new EmbedBuilder()
			.setImage(user.getAvatarUrl() == null ? "https://cdn.discordapp.com/embed/avatars/0.png" : user.getAvatarUrl())
			.setColor(Embeds.Colors.FOXORANGE.get())
			.build();
	}

	public static MessageEmbed getKickedEmbed(User target, User moderator, String reason) {
		return new EmbedBuilder()
			.setTitle(target.getAsTag() + " was kicked!")
			.setDescription("By:\n" + moderator.getAsTag())
			.addField("Kick Reason:", reason, false)
			.addField("Kicked User Id:", target.getId(), false)
			.setColor(Colors.RED.get())
			.setTimestamp(Instant.now())
			.build();
	}

	public static MessageEmbed getBLKickedEmbed(User target, User moderator, String reason, String flaggedText) {
		return new EmbedBuilder()
			.setTitle(target.getAsTag() + " was kicked!")
			.setDescription("By:\n" + moderator.getAsTag())
			.addField("Kick Reason:", reason, false)
			.addField("Kicked User Id:", target.getId(), false)
			.setColor(Colors.RED.get())
			.addField("Flagged Text", flaggedText, false)
			.setTimestamp(Instant.now())
			.build();
	}

	public static MessageEmbed getBannedEmbed(User target, User moderator, String reason) {
		return new EmbedBuilder()
			.setTitle(target.getAsTag() + " was banned!☠")
			.setDescription("By:\n" + moderator.getAsTag())
			.addField("Ban Reason:", reason, false)
			.addField("Banned User Id:", target.getId(), false)
			.setColor(Colors.BLACK.get())
			.setTimestamp(Instant.now())
			.build();
	}

	public static MessageEmbed getBLBannedEmbed(User target, User moderator, String reason, String flaggedText) {
		return new EmbedBuilder()
			.setTitle(target.getAsTag() + " was banned!☠")
			.setDescription("By:\n" + moderator.getAsTag())
			.addField("Ban Reason:", reason, false)
			.addField("Banned User Id:", target.getId(), false)
			.setColor(Colors.BLACK.get())
			.addField("Flagged Text", flaggedText, false)
			.setTimestamp(Instant.now())
			.build();
	}

	public static MessageEmbed getSlashCommandLogEmbed(SlashCommandEvent event) {
		User user = event.getUser();
		return new EmbedBuilder()
			.setAuthor(user.getAsTag(), null,
				user.getAvatarUrl() == null ? "https://cdn.discordapp.com/embed/avatars/0.png" : user.getAvatarUrl())
			.setDescription(event.getCommandString())
			.build();
	}

	public static MessageEmbed getBugEmbed(Message msg) {
		//noinspection ConstantConditions cant be null
		return new EmbedBuilder()
			.setTitle(String.format("🐛 Bug report from %s", msg.getAuthor().getAsTag()))
			.setDescription(msg.getContentRaw())
			.setColor(msg.getMember().getColor())
			.addField("Timestamp", TimeFormat.RELATIVE.now().toString(), true)
			.build();
	}

	public static MessageEmbed getCandidatesEmbed(Message msg) {
		//noinspection ConstantConditions cant be null
		return new EmbedBuilder()
			.setTitle(String.format("%s's idea", msg.getAuthor().getAsTag()))
			.setDescription(msg.getContentRaw())
			.setColor(msg.getMember().getColor())
			.build();
	}

	private static String timeBetween(OffsetDateTime time1, OffsetDateTime time2) {
		final long MINUTE = 60;
		final long HOUR = 3600;
		final long DAY = 86400;
		final long MONTH = 2629800;
		final long YEAR = 31557600;
		long totalSeconds = ChronoUnit.SECONDS.between(time1, time2)*-1;

		long years = totalSeconds/YEAR;
		totalSeconds -= (years * YEAR);
		long months = totalSeconds/MONTH;
		totalSeconds -= (months * MONTH);
		long days = totalSeconds/DAY;
		totalSeconds -= (days * DAY);
		long hours = totalSeconds/HOUR;
		totalSeconds -= (hours * HOUR);
		long mins = totalSeconds/MINUTE;
		totalSeconds -= (mins * MINUTE);
		long seconds = totalSeconds;

		if (years != 0) return String.format("%d years, %d months, %d days", years, months, days);
		if (months !=0) return String.format("%d months, %d days, %d hours", months, days, hours);
		if (days != 0) return String.format("%d days, %d hours, %d minutes", days, hours, mins);
		if (hours != 0) return String.format("%d hours, %d minutes, %d seconds", hours, mins, seconds);
		if (mins != 0) return String.format("%d minutes, %d seconds", mins, seconds);
		if (seconds != 0) return String.format("%d seconds", seconds);
		else return "0 seconds";
	}

	public static MessageEmbed getTreasureEmbed(Member member) {
		ChestRewardsEnum reward = ChestRewardsEnum.getRandom();
		int value = ChestRewardsEnum.getValue(reward);
		final UserProfile dbMember = Database.getMember(member.getGuild(), member.getUser().getId());
		if (dbMember == null) return null; //something fucky happened
		final int skill = dbMember.getSkill();
		final int rand = skill < 10 ? new Random().nextInt(10)+1 : new Random().nextInt(100)+1;
		boolean opened = skill > rand;
		dbMember.adjustSkill(opened ? 1 : 2);
		dbMember.adjustCoins(opened ? value : 0);
		dbMember.update(member);

		if (opened)
			return new EmbedBuilder()
				.setTitle(ChestRewardsEnum.getName(reward))
				.setColor(new Color(0,143,186))
				.setDescription(String.format("You found %d coins!", value))
				.setFooter("Skill increased by 2!")
				.build();

		return new EmbedBuilder()
			.setTitle("⛔ The lock broke!")
			.setColor(Color.RED)
			.setFooter("Skill increased by 1 anyway!")
			.build();
	}

	private enum ChestRewardsEnum {
		Empty,
		ForgottenTreasure,
		Spoils,
		Fortune,
		Motherload,
		FoxbeardsHorde;

		private static int getValue(ChestRewardsEnum reward) {
			return switch (reward) {
				case Empty -> 0;
				case ForgottenTreasure -> new Random().nextInt(4) + 1;
				case Spoils -> new Random().nextInt(4) + 6;
				case Fortune -> new Random().nextInt(4) + 11;
				case Motherload -> new Random().nextInt(4) + 16;
				case FoxbeardsHorde -> new Random().nextInt(4) + 21;
			};
		}

		private static ChestRewardsEnum getRandom() {
			int rand = new Random().nextInt(100)+1;
			if (isBetween(rand, 0, 10)) return Empty; //10%
			if (isBetween(rand, 11, 30)) return ForgottenTreasure; //20%
			if (isBetween(rand, 31, 60)) return Spoils; //30%
			if (isBetween(rand, 61, 85)) return Fortune; //25%
			if (isBetween(rand, 86, 95)) return Motherload; //10%
			if (isBetween(rand, 95, 100)) return FoxbeardsHorde; //5%
			else return getRandom();
		}

		private static boolean isBetween(int x, int lower, int upper) {
			return lower <= x && x <= upper;
		}

		private static String getName(ChestRewardsEnum reward) {
			return switch (reward) {
				case Empty -> "Empty";
				case ForgottenTreasure -> "Forgotten Treasure";
				case Spoils -> "Spoils";
				case Fortune -> "Fortune";
				case Motherload -> "Motherload";
				case FoxbeardsHorde -> "Foxbeard's Horde";
			};
		}
	} //End Enum
}
