package me.vixen.chopperbot.commands.global;

import com.google.gson.GsonBuilder;
import me.vixen.chopperbot.database.Command;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.CustomEmbed;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;

public class CustomCommand implements ICommand {

	@Override
	public void handle(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		//noinspection ConstantConditions must be provided, cannot be null
		String cmdname = event.getOption("cmdname").getAsString();
		//noinspection ConstantConditions
		Command cmd = Database.getCommandByName(guild, cmdname);
		if (cmd == null) {
			StringBuilder builder = new StringBuilder();
			for (Command c : Database.getCommands(guild))
				if (FuzzySearch.ratio(c.getName(), cmdname) >= 50)
					builder.append(c.getName()).append("\n");
			event.replyEmbeds(
				new EmbedBuilder()
					.setColor(Color.RED)
					.setTitle(String.format("⛔ No Command \"%s\" exists!", cmdname))
					.addField("Did you mean: ", builder.toString(), false)
					.build()
			).queue();
			return;
		}
		UserProfile member = Database.getMember(guild, event.getUser().getId());
		if (member == null) {
			event.reply("An error occurred, aborting with code " + Errors.DBNULLRETURN).setEphemeral(true).queue();
			return;
		}
		if (cmd.isStaffOnly() && !member.isAuthorized()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}
		OptionMapping msgidOpt = event.getOption("msgid");
		String cmdResponse = cmd.getResponse();
		if (msgidOpt == null)
			if (cmdResponse.startsWith("{")) {
				event.replyEmbeds(
					new GsonBuilder().create().fromJson(cmdResponse, CustomEmbed.class).toMessageEmbed()
				).queue();
			} else event.reply(cmdResponse).queue();
		else {
			String msgId = msgidOpt.getAsString();
			event.getTextChannel().retrieveMessageById(msgId).queue(msg -> {
				if (cmdResponse.startsWith("{")) {
					msg.replyEmbeds(
						new GsonBuilder().create().fromJson(cmdResponse, CustomEmbed.class).toMessageEmbed()
					).queue();
				} else msg.reply(cmdResponse).mentionRepliedUser(true).queue();
				event.reply("Replied").setEphemeral(true).queue();
			});
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("c", "Invokes a custom command")
			.addOptions(
				new OptionData(OptionType.STRING, "cmdname", "The command name", true),
				new OptionData(OptionType.STRING, "msgid", "The ID of the msg to reply to", false)
			);
	}
}
