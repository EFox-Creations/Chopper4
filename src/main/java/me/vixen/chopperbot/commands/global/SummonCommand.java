package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.Scheduling;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.listener.DefaultEventHandler;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;
import java.util.stream.Collectors;

public class SummonCommand implements ICommand {

	EventWaiter waiter;
	GuildManager gManager;
	public SummonCommand(EventWaiter waiter, GuildManager gManager) {
		this.waiter = waiter;
		this.gManager = gManager;
	}

	@Override
	public void handle(SlashCommandEvent event, UserProfile profile) {
		if (!event.getUser().getId().equalsIgnoreCase(Entry.getCreatorId())) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}
		List<Guild> guilds = Entry.getJDA().getGuilds();

		event.reply("Summoning in " + guilds.size() + " guilds...").queue();

		for (Guild g : Entry.getJDA().getGuilds()) {
			Config config = Database.getConfig(g.getId());
			if (config == null || config.getChannels().isEmpty()) {
				DefaultEventHandler.getDefaultTreasureChannels(g);
				continue; //Skip to next guild
			}

			List<GuildChannel> treasureChannels = g.getChannels()
				.stream()
				.filter(it -> Scheduling.shouldInclude(it, config)) //Remove all but appointed channels
				.filter(it -> it.getType().equals(ChannelType.TEXT)) //Remove non-text channels
				.collect(Collectors.toList());
			Scheduling.makeTreasureChest(treasureChannels);
		}

		event.getHook().editOriginal("Done").queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("summon", "Summon safes in all discords (Only works for Creator)");
	}
}
