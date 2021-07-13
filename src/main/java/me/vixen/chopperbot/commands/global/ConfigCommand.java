package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.guilds.ConfigBuilder;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ConfigCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		@SuppressWarnings("ConstantConditions") //This cant be null as we don't accept DM SCE
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An unknown error occurred; aborting with Error Code C02").queue();
			return;
		}
		if (!dbMember.isAuthorized()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

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

		Config config = new ConfigBuilder()
			.setLvlMsgOverride(!msgsdisabled)
			.setModLogId(modlogId)
			.setIsOnlyStaffPolls(onlyStaffPolls)
			.build();

		boolean b = Database.setConfig(event.getGuild().getId(), config.serialize());
		event.reply(b ? "Config set!" : "An unknown error occurred; aborting with Error code C01").queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("config", "Set up your config for this guild")
			.addOptions(
				new OptionData(OptionType.CHANNEL, "modlog",
					"This is where all system messages and mod actions are sent", true),
				new OptionData(OptionType.BOOLEAN, "disablelvlmsgs",
					"Turn off everyone's level up messages?", true),
				new OptionData(OptionType.BOOLEAN, "onlyallowstaffpolls",
					"Only allow staff to make polls?", true)
			);
	}
}
