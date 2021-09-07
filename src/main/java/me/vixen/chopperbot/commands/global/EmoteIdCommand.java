package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class EmoteIdCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event, UserProfile profile) {
		if (!event.getUser().getId().equalsIgnoreCase(Entry.getCreatorId())) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		//noinspection ConstantConditions is required
		event.reply(event.getOption("emote").getAsString()).setEphemeral(true).queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("emoteid", "Get an emote id (Only works for Creator)").addOptions(
			new OptionData(OptionType.STRING, "emote", "The emote", true)
		);
	}
}
