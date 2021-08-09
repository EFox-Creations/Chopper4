package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.DBMember;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.guilds.ConfigBuilder;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		@SuppressWarnings("ConstantConditions") //This cant be null as we don't accept DM SCE
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		if (!dbMember.isAuthorized()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		switch (event.getSubcommandName()) {
			case "set" -> setConfig(event);
			case "adddomain" -> {
				Config config = Database.getConfig(event.getGuild().getId());

				if (config == null) {
					event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				String domain = event.getOption("domain").getAsString();
				// Sanitize
				if (domain.startsWith("https://")) domain.replaceFirst("https://", "");
				if (domain.startsWith("http://")) domain.replaceFirst("http://", "");
				if (domain.startsWith("www.")) domain.replaceFirst("www\\.", "");
				//leave anything after TLD off
				Matcher matcher = Pattern.compile("(.+\\.[^\\s\\/]{2,63})", Pattern.CASE_INSENSITIVE).matcher(domain);
				if (matcher.find()) domain = matcher.group(0);
				config.addDomain(domain);
				boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());
				event.reply(b ? "Added!" + domain : "An error occurred; aborting with Code " + Errors.CONFIG1).queue();
			}
			case "deletedomain" -> {
				Config config = Database.getConfig(event.getGuild().getId());

				if (config == null) {
					event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				String domain = event.getOption("domain").getAsString();
				boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());
				event.reply(b ? "Deleted!" : "An error occurred; aborting with Code " + Errors.CONFIG1).queue();
			}
			case "cleardomains" -> {
				Config config = Database.getConfig(event.getGuild().getId());

				if (config == null) {
					event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				config.clearDomains();
				boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());
				event.reply(b ? "Cleared!" : "An error occurred; aborting with Code " + Errors.CONFIG1).queue();
			}
			case "viewdomains" -> {
				Config config = Database.getConfig(event.getGuild().getId());

				if (config == null) {
					event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				if (config.getDomains() == null || config.getDomains().isEmpty()){
					event.reply("You have no blacklisted domains").queue();
					return;
				} else {
					StringBuilder builder = new StringBuilder();
					for (String s : config.getDomains())
						builder.append(s).append("\n");
					event.replyEmbeds(
						new EmbedBuilder()
							.setColor(Color.ORANGE)
							.setTitle("Blacklisted domains for this Guild")
							.setDescription(builder.toString().trim())
							.build()
					).queue();
				}
			}
		}
	}

	private void setConfig(SlashCommandEvent event) {
		OptionMapping modlog = event.getOption("modlog");
		String modlogId;
		if (modlog != null && modlog.getAsMessageChannel() != null)
			modlogId = modlog.getAsMessageChannel().getId();
		else {
			event.replyEmbeds(Embeds.getInvalidArgumentEmbed("modlog", "Must be a text channel")).queue();
			return;
		}

		OptionMapping disablelvlmsgs = event.getOption("disablelvlmsgs");
		boolean msgsdisabled;
		if (disablelvlmsgs != null) {
			try {
				msgsdisabled = disablelvlmsgs.getAsBoolean();
			} catch (IllegalArgumentException e) {
				event.replyEmbeds(
						Embeds.getInvalidArgumentEmbed("disablelvlmsgs", "Must be \"True\" or \"False\""))
					.queue();
				return;
			}
		} else {
			event.replyEmbeds(
					Embeds.getInvalidArgumentEmbed("disablelvlmsgs", "Must be \"True\" or \"False\""))
				.queue();
			return;
		}

		OptionMapping onlyStaffPollsOpt = event.getOption("onlyallowstaffpolls");
		boolean onlyStaffPolls;
		if (onlyStaffPollsOpt != null) {
			try {
				onlyStaffPolls = onlyStaffPollsOpt.getAsBoolean();
			} catch (IllegalArgumentException e) {
				event.replyEmbeds(
						Embeds.getInvalidArgumentEmbed("onlyallowstaffpolls", "Must be \"True\" or \"False\""))
					.queue();
				return;
			}
		} else {
			event.replyEmbeds(
					Embeds.getInvalidArgumentEmbed("onlyallowstaffpolls", "Must be \"True\" or \"False\""))
				.queue();
			return;
		}

		Config.Punishment punishment;
		try {
			punishment = Config.Punishment.valueOf(event.getOption("black-domain-punish").getAsString());
		} catch (IllegalArgumentException e) {
			punishment = Config.Punishment.WARN;
		}

		boolean enableJoinLeaveMessages = event.getOption("enablejoinlavemsgs").getAsBoolean();

		OptionMapping optMap = event.getOption("joinleavemsgschannel");
		String joinLeaveChannelId = optMap == null ? null : optMap.getAsString();

		Config config = new ConfigBuilder()
			.setLvlMsgOverride(!msgsdisabled)
			.setModLogId(modlogId)
			.setIsOnlyStaffPolls(onlyStaffPolls)
			.setEnableJoinLeaveMessges(enableJoinLeaveMessages)
			.setJoinLeaveMsgsChannelId(joinLeaveChannelId)
			.setPunishment(punishment)
			.build();

		boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());
		event.reply(b ? "Config set!" : "An error occurred; aborting with Code " + Errors.CONFIG1).queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("config", "Set up your config for this guild").addSubcommands(
			new SubcommandData("set", "Set your initial config")
				.addOptions(
					new OptionData(OptionType.CHANNEL, "modlog",
						"This is where all system messages and mod actions are sent", true),
					new OptionData(OptionType.BOOLEAN, "disablelvlmsgs",
						"Turn off everyone's level up messages?", true),
					new OptionData(OptionType.BOOLEAN, "onlyallowstaffpolls",
						"Only allow staff to make polls?", true),
					new OptionData(OptionType.STRING, "black-domain-punish", "How should auto-mod " +
						"punish blacklisted domains?", true)
						.addChoices(
							new Command.Choice("None", "NONE"),
							new Command.Choice("Warn", "WARN"),
							new Command.Choice("Kick", "KICK"),
							new Command.Choice("Ban", "BAN")
						),
					new OptionData(OptionType.BOOLEAN, "enablejoinleavemsgs",
						"Should Chop show Join and leave messages?", true),
					new OptionData(OptionType.CHANNEL, "joinleavemsgschannel",
						"What channel should they be shown in?\nPlease note this MUST be provided to recieve them")
				),
				new SubcommandData("adddomain", "Add a new doamin to the blacklist")
					.addOption(OptionType.STRING, "domain",
						"This should only be the website name and suffix" +
							" ex. youtube.com", true),
				new SubcommandData("deletedomain", "Delete a domain from the blacklist")
					.addOption(OptionType.STRING, "domain",
						"This should only be the website name and suffix" +
							" ex. youtube.com", true),
				new SubcommandData("cleardomains", "Clear all domains from the blacklist"),
				new SubcommandData("viewdomains", "See your blacklisted domains")
		);
	}
}
