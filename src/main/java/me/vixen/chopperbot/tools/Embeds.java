package me.vixen.chopperbot.tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.RoleImpl;
import net.dv8tion.jda.internal.entities.UserImpl;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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
				" the `/config` setup", false)
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

	public static MessageEmbed getInsufficientExp() {
		return new EmbedBuilder()
			.setTitle("🚫 You do not have enough exp for that! 🚫")
			.setDescription(BUG_ERROR)
			.setColor(Colors.RED.get())
			.build();
	}

	public static MessageEmbed getInsufficientLocks() {
		return new EmbedBuilder()
			.setTitle("🚫 You do not have enough locks for that! 🚫")
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
			.setDescription("An authorized user needs to complete /config")
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
			.addField("Timestamp", TimeFormat.RELATIVE.now().toString(), true)
			.build();
	}

	public static MessageEmbed getBannedEmbed(User target, User moderator, String reason) {
		return new EmbedBuilder()
			.setTitle(target.getAsTag() + " was banned!☠")
			.setDescription("By:\n" + moderator.getAsTag())
			.addField("Ban Reason:", reason, false)
			.addField("Banned User Id:", target.getId(), false)
			.setColor(Colors.BLACK.get())
			.addField("Timestamp", TimeFormat.RELATIVE.now().toString(), true)
			.build();
	}

	public static MessageEmbed getSlashCommandLogEmbed(SlashCommandEvent event) {
		User user = event.getUser();
		return new EmbedBuilder()
			.setAuthor(user.getAsTag(), null,
				user.getAvatarUrl() == null ? "https://cdn.discordapp.com/embed/avatars/0.png" : user.getAvatarUrl())
			.setDescription(getCommandString(event))
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

	/**
	 * Gets the slash command String for this slash command.
	 * <br>This is similar to the String you see when clicking the interaction name in the client.
	 *
	 * <p>Example return for an echo command: {@code /say echo phrase: Say this}
	 *
	 * @return The command String for this slash command
	 */
	@Nonnull
	private static String getCommandString(SlashCommandEvent event)
	{
		//Get text like the text that appears when you hover over the interaction in discord
		StringBuilder builder = new StringBuilder();
		builder.append("/").append(event.getName());
		if (event.getSubcommandGroup() != null)
			builder.append(" ").append(event.getSubcommandGroup());
		if (event.getSubcommandName() != null)
			builder.append(" ").append(event.getSubcommandName());
		for (OptionMapping o : event.getOptions())
		{
			builder.append(" ").append(o.getName()).append(": ");
			switch (o.getType())
			{
				case CHANNEL:
					builder.append("#").append(o.getAsGuildChannel().getName());
					break;
				case USER:
					builder.append("@").append(o.getAsUser().getName());
					break;
				case ROLE:
					builder.append("@").append(o.getAsRole().getName());
					break;
				case MENTIONABLE: //client only allows user or role mentionable as of Aug 4, 2021
					if (o.getAsMentionable() instanceof RoleImpl)
						builder.append("@").append(((RoleImpl) o.getAsMentionable()).getName());
					else if (o.getAsMentionable() instanceof MemberImpl)
						builder.append("@").append(((MemberImpl) o.getAsMentionable()).getEffectiveName());
					else if (o.getAsMentionable() instanceof UserImpl)
						builder.append("@").append(((UserImpl) o.getAsMentionable()).getName());
					else
						builder.append("@").append(o.getAsMentionable().getIdLong());
					break;
				default:
					builder.append(o.getAsString());
					break;
			}
		}
		return builder.toString();
	}
}
