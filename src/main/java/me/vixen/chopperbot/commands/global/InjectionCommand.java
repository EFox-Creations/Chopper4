package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class InjectionCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		if (!event.getUser().getId().equalsIgnoreCase(Entry.CREATOR_ID)) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		event.deferReply().setEphemeral(true).queue();

		String argument = event.getOption("argument").getAsString();
		boolean execute = Database.execute(argument);

		event.getHook().editOriginal("Injection: " + execute).queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("inject", "Injection (Only works for Creator)")
			.addOptions(new OptionData(OptionType.STRING, "argument", "The argument"));
	}
}
