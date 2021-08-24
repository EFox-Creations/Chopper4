package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class ToggleLvlMsgsCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event, UserProfile profile) {
		boolean newSetting = profile.toggleLvlMsgs();
		event.reply(String.format("Your level up messages are `%s`", newSetting ? "ON" : "OFF")).queue();
		profile.update(event.getMember());
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("toggle-lvl-up-msgs", "Toggle your level up messages");
	}
}
