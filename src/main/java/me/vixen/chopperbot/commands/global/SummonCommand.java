package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.BackgroundThread;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.guilds.IGuild;
import me.vixen.chopperbot.listener.DefaultEventHandler;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public class SummonCommand implements ICommand {

	EventWaiter waiter;
	GuildManager gManager;
	public SummonCommand(EventWaiter waiter, GuildManager gManager) {
		this.waiter = waiter;
		this.gManager = gManager;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		if (!event.getUser().getId().equalsIgnoreCase(Entry.CREATOR_ID)) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}
		List<Guild> guilds = Entry.jda.getGuilds();

		event.reply("Summoning in " + guilds.size() + " guilds...").queue();

		for (Guild g : guilds) {
			if (gManager.contains(g)) {
				final IGuild ig = gManager.getGuild(g);
				BackgroundThread.makeTreasureChest(ig.getTreasureChannels());
			} else {
				final List<TextChannel> defaultTreasureChannels = DefaultEventHandler.getDefaultTreasureChannels(g);
				BackgroundThread.makeTreasureChest(defaultTreasureChannels);
			}
		}

		event.getHook().editOriginal("Done").queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("summon", "Summon safes in all discords (Only works for Creator)");
	}
}
