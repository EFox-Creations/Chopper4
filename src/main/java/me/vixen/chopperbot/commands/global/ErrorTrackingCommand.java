package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class ErrorTrackingCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		if (!event.getUser().getId().equalsIgnoreCase(Entry.CREATOR_ID)) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		//noinspection ConstantConditions is required cant be null
		String errorcode = event.getOption("errorcode").getAsString().toUpperCase();

		Errors error;
		try {
			error = Errors.valueOf(errorcode);
			event.reply(error.get()).setEphemeral(true).queue();
		} catch (IllegalArgumentException e) {
			event.reply("Not a valid error code").setEphemeral(true).queue();
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("errtrack", "Track an error (Only works for creator)")
			.addOption(OptionType.STRING, "errorcode", "The error code to track", true);
	}
}
