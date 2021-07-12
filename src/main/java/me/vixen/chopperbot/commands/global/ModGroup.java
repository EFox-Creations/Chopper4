package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.Logger;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ConstantConditions")
public class ModGroup implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User moderator = event.getUser();
		String moderatorId = moderator.getId();
		DBMember moderatorDB = Database.getMember(guild, moderatorId);
		if (!moderatorDB.isAuthorized()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}
		event.deferReply().queue();
		final String name = event.getSubcommandName();

		switch (name) {
			case "authorize" -> {
				try {
					final Member targetMem = event.getOption("user").getAsMember();
					DBMember targetDB = Database.getMember(guild, targetMem.getId());
					if (targetDB == null) {
						event.reply("I couldn't load that person :/").queue();
						return;
					}
					final boolean authorize = event.getOption("authorize").getAsBoolean();
					targetDB.setAuthorized(authorize);
					targetDB.update();
					event.getHook().editOriginal("Set " + targetMem.getAsMention() + " authorization as " + authorize).queue();
				} catch (NullPointerException e) {
					Logger.log("NPE caught:", e);
					event.getHook().editOriginal("Couldn't figure out who you're talking about").queue();
				}
			}
			case "softban" -> {
				try {
					final Member member = event.getOption("user").getAsMember();
					final String reason = event.getOption("reason").getAsString() + "\nThis was a softban";
					try {
						member.ban(7, reason).queue(
							success -> guild.unban(member.getUser()).queue(
								success1 -> {
									event.getHook().editOriginal("SoftBanned " + member.getEffectiveName() + " for " + reason).queue();
									Config config = Database.getConfig(guild.getId());
									if (config != null)
										event.getGuild().getTextChannelById(config.getModlogId()).sendMessage(
											event.getMember().getAsMention() + " SoftBanned " + member.getEffectiveName() + " for " + reason
										).queue();
									else event.getTextChannel().sendMessageEmbeds(Embeds.getPleaseDoConfig()).queue();
								},
								failed2 -> event.getHook().editOriginal("Failed to unban member").queue()),
							failed -> event.getHook().editOriginal("Failed to ban member").queue());
					} catch (InsufficientPermissionException e) {
						event.getHook().editOriginal("I do not have permission to ban members here").queue();
					} catch (HierarchyException ex) {
						event.getHook().editOriginal("This member is more powerful than I am").queue();
					} catch (IllegalArgumentException e) {
						event.getHook().editOriginal("This is not a valid member").queue();
					}
				} catch (NullPointerException e) {
					event.getHook().editOriginal("Couldn't figure out who you're talking about").queue();
				}
			}
			case "ban" -> {
				try {
					final Member member = event.getOption("user").getAsMember();
					final String reason = event.getOption("reason").getAsString();
					try {
						member.ban(7, reason).queue();
						event.getHook().editOriginalEmbeds(
							Embeds.getBannedEmbed(member.getUser(), moderator, reason)
						).queue();
						Config config = Database.getConfig(guild.getId());
						if (config != null)
							event.getGuild().getTextChannelById(config.getModlogId()).sendMessageEmbeds(
								Embeds.getBannedEmbed(member.getUser(), moderator, reason)
							).queue();
						else event.getTextChannel().sendMessageEmbeds(Embeds.getPleaseDoConfig()).queue();
					} catch (InsufficientPermissionException e) {
						event.getHook().editOriginal("I do not have permission to ban members here").queue();
					} catch (HierarchyException ex) {
						event.getHook().editOriginal("This member is more powerful than I am").queue();
					} catch (IllegalArgumentException e) {
						event.getHook().editOriginal("This is not a valid member").queue();
					}
				} catch (NullPointerException e) {
					event.getHook().editOriginal("Couldn't figure out who you're talking about").queue();
				}
			}
			case "kick" -> {
				OptionMapping userOpt = event.getOption("user");
				Member target;
				if (userOpt != null) target = userOpt.getAsMember();
				else {
					event.getHook().editOriginalEmbeds(
						Embeds.getInvalidArgumentEmbed("user", "Is not provided")
					).queue();
					return;
				}
				if (target == null) {
					event.getHook().editOriginalEmbeds(Embeds.getUnknownMember()).queue();
					return;
				}

				final String reason = event.getOption("reason").getAsString();
				guild.kick(target, reason).queue();
				event.getHook().editOriginalEmbeds(Embeds.getKickedEmbed(target.getUser(), moderator, reason)).queue();

				Config config = Database.getConfig(guild.getId());
				if (config != null)
					event.getGuild().getTextChannelById(config.getModlogId()).sendMessageEmbeds(
						Embeds.getKickedEmbed(target.getUser(), moderator, reason)
					).queue();
				else event.getTextChannel().sendMessageEmbeds(Embeds.getPleaseDoConfig()).queue();
			}
			case "clear" -> {
				int number = (int) event.getOption("nummsgs").getAsLong();

				event.getTextChannel().getHistory().retrievePast(number).queue(messages ->
					event.getTextChannel().deleteMessages(messages).queue((unused) ->
						event.getHook().editOriginal(number + " Messages Cleared").queue())
				);
			}
			case "mute" -> {
				OptionMapping userOpt = event.getOption("user");
				Member target;
				if (userOpt != null) target = userOpt.getAsMember();
				else {
					event.getHook().editOriginalEmbeds(
						Embeds.getInvalidArgumentEmbed("user", "Is not provided")
					).queue();
					return;
				}
				if (target == null) {
					event.getHook().editOriginalEmbeds(Embeds.getUnknownMember()).queue();
					return;
				}

				OptionMapping unmuteTimeOpt = event.getOption("unmutetime");
				OffsetDateTime unmutetime;
				if (unmuteTimeOpt != null) {
					try {
						unmutetime = resolveUnmuteTime(unmuteTimeOpt.getAsString());
					} catch (IllegalArgumentException e) {
						event.getHook().editOriginalEmbeds(
							Embeds.getInvalidArgumentEmbed("unmutetime", "Not a valid input\nShould be in format of \"1d2h3m4s\"")
						).queue();
						return;
					}
				}
				else {
					event.getHook().editOriginalEmbeds(
						Embeds.getInvalidArgumentEmbed("unmuteTime", "Is not provided")
					).queue();
					return;
				}

				final DBMember targetDB = Database.getMember(guild, target.getId());
				targetDB.setMuted(unmutetime);
				targetDB.update();

				Config config = Database.getConfig(guild.getId());
				if (config != null)
					event.getGuild().getTextChannelById(config.getModlogId()).sendMessage(
						moderator.getAsMention() + " Muted " + target.getEffectiveName() + " for " + unmuteTimeOpt.getAsString()
					).queue();
				else event.getTextChannel().sendMessageEmbeds(Embeds.getPleaseDoConfig()).queue();
			}
			case "unmute" -> {
				OptionMapping userOpt = event.getOption("user");
				Member target;
				if (userOpt != null) target = userOpt.getAsMember();
				else {
					event.getHook().editOriginalEmbeds(
						Embeds.getInvalidArgumentEmbed("user", "Is not provided")
					).queue();
					return;
				}
				if (target == null) {
					event.getHook().editOriginalEmbeds(Embeds.getUnknownMember()).queue();
					return;
				}
				DBMember targetDB = Database.getMember(guild, target.getUser().getId());
				targetDB.unmute();
				targetDB.update();
			}
		}
	}
	private static final Pattern DAY_CAPTURE = Pattern.compile("(\\d[dD])");
	private static final Pattern HOUR_CAPTURE = Pattern.compile("(\\d[hH])");
	private static final Pattern MIN_CAPTURE = Pattern.compile("(\\d[mM])");
	private static final Pattern SEC_CAPTURE = Pattern.compile("(\\d[sS])");

	//Time looks like 1d2h3m4s
	private OffsetDateTime resolveUnmuteTime(String input) throws IllegalArgumentException {

		Matcher matcher = DAY_CAPTURE.matcher(input);
		int days = 0, hours = 0, min = 0, sec = 0;
		if (matcher.find()) {
			days = Integer.parseInt(matcher.group(0).toLowerCase().replaceAll("d", ""));
		}
		Matcher matcher1 = HOUR_CAPTURE.matcher(input);
		if (matcher1.find()) {
			hours = Integer.parseInt(matcher.group(0).toLowerCase().replaceAll("h", ""));
		}
		Matcher matcher2 = MIN_CAPTURE.matcher(input);
		if (matcher2.find()) {
			min = Integer.parseInt(matcher.group(0).toLowerCase().replaceAll("m", ""));
		}
		Matcher matcher3 = SEC_CAPTURE.matcher(input);
		if (matcher3.find()) {
			sec = Integer.parseInt(matcher.group(0).toLowerCase().replaceAll("s", ""));
		}

		OffsetDateTime unmuteTime = OffsetDateTime.now()
			.plus(days, ChronoUnit.DAYS)
			.plus(hours, ChronoUnit.HOURS)
			.plus(min, ChronoUnit.MINUTES)
			.plus(sec, ChronoUnit.SECONDS);

		if (OffsetDateTime.now().isEqual(unmuteTime) || OffsetDateTime.now().isAfter(unmuteTime)) {
			throw new IllegalArgumentException("Invalid time provided");
		} else return unmuteTime;
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("mod", "Moderation Commands").addSubcommands(
			new SubcommandData("authorize", "Authorize a user to use staff only commands").addOptions(
				new OptionData(OptionType.USER, "user", "The user in question",true),
				new OptionData(OptionType.BOOLEAN, "setauthorized", "(T/F) Set authorized?", true)
			),
			new SubcommandData("softban", "Ban and immediately unban a user. Useful for deleting all messages from a user for the past 7 days")
				.addOptions(
					new OptionData(OptionType.USER, "user", "The user to ban",true),
					new OptionData(OptionType.STRING, "reason", "The reason for ban", true)
				),
			new SubcommandData("ban", "Ban a user (Requires ban permissions and authorization").addOptions(
				new OptionData(OptionType.USER, "user", "The user to ban",true),
				new OptionData(OptionType.STRING, "reason", "The reason for ban", true)
			),
			new SubcommandData("kick", "Kick a user").addOptions(
				new OptionData(OptionType.USER, "user", "The user to kick",true),
				new OptionData(OptionType.STRING, "reason", "The reason to kick",true)
			),
			new SubcommandData("clear", "Clear x number of messages").addOptions(
				new OptionData(OptionType.INTEGER, "nummsgs", "The number of messages to clear",true)
			),
			new SubcommandData("mute", "Mute a user").addOptions(
				new OptionData(OptionType.USER, "user", "The user to mute",true),
				new OptionData(OptionType.STRING, "muteduration", "Should be in format of \"1d2h3m4s\"",true)
			),
			new SubcommandData("unmute", "Unmute a user early").addOptions(
				new OptionData(OptionType.USER, "user", "The user to unmute",true)
			)
		);
	}
}
