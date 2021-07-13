package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.steppschuh.markdowngenerator.table.Table;
import java.io.*;

public class QueryCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		if (!event.getUser().getId().equalsIgnoreCase(Entry.CREATOR_ID)) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		event.deferReply().setEphemeral(true).queue();

		//noinspection ConstantConditions cant be null
		String argument = event.getOption("argument").getAsString();
		Table table = Database.query(argument);
		if (table == null) {
			event.reply("Null return").setEphemeral(true).queue();
			return;
		}
		try {
			File file = new File("table.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(table.toString());
			writer.close();
			//noinspection ResultOfMethodCallIgnored
			event.getHook().editOriginal("Sending File").queue(unused ->
				event.getTextChannel().sendFile(file).queue(unused1 -> file.delete())
			);
		} catch (IOException e) {
			if (table.toString().length() > Message.MAX_CONTENT_LENGTH)
				event.getHook().editOriginal("Table too big to display and IO Error occurred; aborting").queue();
			else
				event.getHook().editOriginal(table.toString()).queue();
		} catch (NullPointerException e) {
			event.getHook().editOriginal("Null response").queue();
		}

	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("query", "Query (Only works for Creator)")
			.addOptions(new OptionData(OptionType.STRING, "argument", "The argument"));
	}
}
