package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class HelpCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event, UserProfile profile) {
		event.replyEmbeds(new EmbedBuilder()
			.setColor(Embeds.Colors.FOXORANGE.get())
			.setTitle("🆘 Chopper Help Page 🆘")
			.addField("How to use?", """
				All commands are used through the discord / menu.\sBe sure to read the instructions there!
				Daily chests may be claimed with /claimdaily
				Random `safes` also appear, try and unlock them!
				These `safes` require a `lockpick skill` and can be leveled up by interacting with locks.
				Take a look into the menu and play around with the commands!
				""",false)
			.addField("Questions? Comments? Concerns?", "[Join the Discord](https://www.discord.com/B8449N8QZM)", false)
			.build()
		).queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("help", "Displays the help");
	}
}
