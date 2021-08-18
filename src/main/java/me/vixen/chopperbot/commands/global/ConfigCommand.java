package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.DBMember;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.guilds.ConfigBuilder;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		event.deferReply().queue();
		@SuppressWarnings("ConstantConditions") //This cant be null as we don't accept DM SCE
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.getHook().editOriginal("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		if (!dbMember.isAuthorized()) {
			event.getHook().editOriginalEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		switch (event.getSubcommandName()) {
			case "set" -> setConfig(event);
			case "adddomain" -> {
				Config config = Database.getConfig(event.getGuild().getId());
				if (config == null) {
					event.getHook().editOriginalEmbeds(Embeds.getPleaseDoConfig()).queue();
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
				event.getHook().editOriginal(b ? "Added! " + domain : "An error occurred; aborting with Code " + Errors.CONFIG1).queue();
			}
			case "deletedomain" -> {
				Config config = Database.getConfig(event.getGuild().getId());

				if (config == null) {
					event.getHook().editOriginalEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				String domain = event.getOption("domain").getAsString();
				config.deleteDomain(domain);
				boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());
				event.getHook().editOriginal(b ? "Deleted!" : "An error occurred; aborting with Code " + Errors.CONFIG1).queue();
			}
			case "cleardomains" -> {
				Config config = Database.getConfig(event.getGuild().getId());

				if (config == null) {
					event.getHook().editOriginalEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				config.clearDomains();
				boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());
				event.getHook().editOriginal(b ? "Cleared!" : "An error occurred; aborting with Code " + Errors.CONFIG1).queue();
			}
			case "viewdomains" -> {
				Config config = Database.getConfig(event.getGuild().getId());

				if (config == null) {
					event.getHook().editOriginalEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				if (config.getDomains() == null || config.getDomains().isEmpty()){
					event.getHook().editOriginal("You have no blacklisted domains").queue();
					return;
				} else {
					StringBuilder builder = new StringBuilder();
					for (String s : config.getDomains())
						builder.append(s).append("\n");
					event.getHook().editOriginalEmbeds(
						new EmbedBuilder()
							.setColor(Color.ORANGE)
							.setTitle("Blacklisted domains for this Guild")
							.setDescription(builder.toString().trim())
							.build()
					).queue();
				}
			}

			case "addchannel" -> {
				Config config = Database.getConfig(event.getGuild().getId());
				if (config == null) {
					event.getHook().editOriginalEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				GuildChannel channel = event.getOption("channel").getAsGuildChannel();
				if (!channel.getType().equals(ChannelType.TEXT)) {
					event.getHook().editOriginal("That is not a valid text channel!").queue();
					return;
				}

				config.addChannel(channel.getId());
				boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());

				event.getHook().editOriginal(
					b ?
					"Added " + channel.getAsMention() + " to the " + config.getMode().capitalize()
					: "An error occurred; aborting with Code " + Errors.CONFIG1)
				.queue();
			}

			case "deletechannel" -> {
				Config config = Database.getConfig(event.getGuild().getId());
				if (config == null) {
					event.getHook().editOriginalEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				GuildChannel channel = event.getOption("channel").getAsGuildChannel();
				String channelId = channel.getId();
				config.removeChannel(channelId);
				boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());
				event.getHook().editOriginal(
					b ?
					"Deleted " + channel.getAsMention() + " from the " + config.getMode().capitalize()
					: "An error occurred; aborting with Code " + Errors.CONFIG1)
				.queue();
			}

			case "clearchannels" -> {
				Config config = Database.getConfig(event.getGuild().getId());
				if (config == null) {
					event.getHook().editOriginalEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				config.clearChannels();
				boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());
				event.getHook().editOriginal(b ? "Cleared!" : "An error occurred; aborting with Code " + Errors.CONFIG1).queue();
			}

			case "viewchannels" -> {
				Config config = Database.getConfig(event.getGuild().getId());
				if (config == null) {
					event.getHook().editOriginalEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}

				List<GuildChannel> listedChannels =
					event.getGuild().getChannels()
						.stream().filter(
							it -> config.getChannels().contains(it.getId()))
						.collect(Collectors.toList());

				StringBuilder builder = new StringBuilder();
				for (GuildChannel ch : listedChannels)
					builder.append(ch.getAsMention()).append("\n");

				event.getHook().editOriginalEmbeds(
					new EmbedBuilder()
						.setColor(Color.CYAN)
						.setTitle("Current mode: " + config.getMode())
						.setDescription(builder.toString())
						.build()
				).queue();
			}
		}
	}

	private void setConfig(SlashCommandEvent event) {
		OptionMapping modlog = event.getOption("modlog");
		String modlogId;
		if (modlog != null && modlog.getAsMessageChannel() != null)
			modlogId = modlog.getAsMessageChannel().getId();
		else {
			event.getHook().editOriginalEmbeds(Embeds.getInvalidArgumentEmbed("modlog", "Must be a text channel")).queue();
			return;
		}

		OptionMapping disablelvlmsgs = event.getOption("disablelvlmsgs");
		boolean msgsdisabled;
		if (disablelvlmsgs != null) {
			try {
				msgsdisabled = disablelvlmsgs.getAsBoolean();
			} catch (IllegalArgumentException e) {
				event.getHook().editOriginalEmbeds(
						Embeds.getInvalidArgumentEmbed("disablelvlmsgs", "Must be \"True\" or \"False\""))
					.queue();
				return;
			}
		} else {
			event.getHook().editOriginalEmbeds(
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
				event.getHook().editOriginalEmbeds(
						Embeds.getInvalidArgumentEmbed("onlyallowstaffpolls", "Must be \"True\" or \"False\""))
					.queue();
				return;
			}
		} else {
			event.getHook().editOriginalEmbeds(
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

		boolean enableJoinLeaveMessages = event.getOption("enablejoinleavemsgs").getAsBoolean();

		OptionMapping channelSetting = event.getOption("joinleavemsgschannel");
		String joinLeaveChannelId = channelSetting == null ? null : channelSetting.getAsString();

		OptionMapping treasureMode = event.getOption("treasuremode");
		//noinspection ConstantConditions is required, can't be null
		Config.TreasureMode mode = Config.TreasureMode.valueOf(treasureMode.getAsString());

		List<String> domains = new ArrayList<>();
		List<String> channels = new ArrayList<>();
		Config config = Database.getConfig(event.getGuild().getId());
		if (config != null) {
			domains = config.getDomains();
			channels = config.getChannels();
		}

		Config newConfig = new ConfigBuilder()
			.setLvlMsgOverride(!msgsdisabled)
			.setModLogId(modlogId)
			.setIsOnlyStaffPolls(onlyStaffPolls)
			.setEnableJoinLeaveMessges(enableJoinLeaveMessages)
			.setJoinLeaveMsgsChannelId(joinLeaveChannelId)
			.setPunishment(punishment)
			.setTreasureMode(mode)
			.setDomains(domains)
			.setTreasureChannels(channels)
			.build();

		boolean b = Database.setConfig(event.getGuild().getId(), newConfig.serialize());
		event.getHook().editOriginal(b ? "Config set!" : "An error occurred; aborting with Code " + Errors.CONFIG1).queue();
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
					new OptionData(OptionType.STRING, "treasuremode", "The mode for your treasure channel list", true)
						.addChoice("Blacklist", "BLACKLIST").addChoice("Whitelist", "WHITELIST"),
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
				new SubcommandData("viewdomains", "See your blacklisted domains"),
				new SubcommandData("addchannel", "Add a channel to the Black/White list")
					.addOption(OptionType.CHANNEL, "channel", "The channel to add", true),
				new SubcommandData("deletechannel", "Delete a channel from the Black/White list")
					.addOption(OptionType.CHANNEL, "channel", "The channel to delete", true),
				new SubcommandData("clearchannels", "Clear all channels from the Black/White list"),
				new SubcommandData("viewchannels", "View all current Black/White listed channels")
		);
	}
}
