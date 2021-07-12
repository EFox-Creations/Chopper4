package me.vixen.chopperbot.guilds;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.guilds.bejoijoplugins.BejoIjoPlugins;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

public class GuildManager {
	List<IGuild> guilds = new ArrayList<>();

	public GuildManager(EventWaiter waiter) {
		addGuild(new BejoIjoPlugins("663796409635569664", waiter));
	}

	private void addGuild(IGuild guild) {
		if (this.contains(guild)) throw new IllegalArgumentException("This guild already added: " + guild.getId());
		guilds.add(guild);
	}

	public boolean contains(Guild g) {
		return guilds.stream().anyMatch(it -> it.getId().equalsIgnoreCase(g.getId()));
	}

	public boolean contains(IGuild g) {
		return guilds.stream().anyMatch(it -> it.getId().equalsIgnoreCase(g.getId()));
	}

	public List<IGuild> getGuilds() { return List.copyOf(guilds); }

	public IGuild getGuild(String guildId) {
		for (IGuild g : guilds) {
			if (g.getId().equalsIgnoreCase(guildId)) return g;
		}
		return null;
	}

	public IGuild getGuild(Guild g) {
		for (IGuild ig : guilds) {
			if (ig.getId().equalsIgnoreCase(g.getId())) return ig;
		}
		return null;
	}
}
