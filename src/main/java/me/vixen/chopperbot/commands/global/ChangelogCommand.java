package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class ChangelogCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event, UserProfile profile) {
		if (!event.getUser().getId().equalsIgnoreCase(Entry.CREATOR_ID)) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		@SuppressWarnings("ConstantConditions") //cannot be null as it is required client-side
		String text = event.getOption("text")
			.getAsString()
			.replaceAll("<n>", "\n");

		List<Guild> guilds = Entry.jda.getGuilds();
		event.reply("Dispatching to " + guilds.size() + "guilds").queue();

		for (Guild g : guilds) {
			Config config = Database.getConfig(g.getId());
			if (config == null) continue;
			String modlogId = config.getModlogId();
			TextChannel channel;
			if (modlogId != null)
				channel = g.getTextChannelById(modlogId);
			else
				channel = g.getSystemChannel();
			if (channel == null) continue;
			channel.sendMessage(text).queue();
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("changelog", "Post a new changelog (Only works for bot owner)")
			.addOptions(
				new OptionData(OptionType.STRING, "text", "The text of the broadcast", true)
			);
	}
}
