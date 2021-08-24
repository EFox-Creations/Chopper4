package me.vixen.chopperbot.guilds.bejoijoplugins;

import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;

public class PatreonCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event, UserProfile profile) {
		event.reply("These are the two Patreons that make your experience here a cut above the rest").addActionRow(
			Button.link("https://www.patreon.com/bejoijo", "BejoIjo Plugins"),
			Button.link("https://www.patreon.com/efoxcreations", "EFox Creations")
		).queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("patreon", "Shows you patreon links");
	}
}
