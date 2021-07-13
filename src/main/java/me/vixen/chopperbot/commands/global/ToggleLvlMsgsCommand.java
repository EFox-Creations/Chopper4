package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class ToggleLvlMsgsCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		String id = event.getUser().getId();
		//noinspection ConstantConditions cant be null
		DBMember member = Database.getMember(event.getGuild(), id);
		if (member == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		boolean newSetting = member.toggleLvlMsgs();
		event.reply(String.format("Your level up messages are `%s`", newSetting ? "ON" : "OFF")).queue();
		member.update();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("toggle-lvl-up-msgs", "Toggle your level up messages");
	}
}
