package me.vixen.chopperbot.guilds;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.guilds.bejoijoplugins.BejoIjoPlugins;
import me.vixen.chopperbot.guilds.efox.EFoxHomeBase;
import me.vixen.chopperbot.guilds.lspdfrts.LSPDFRTS;
import me.vixen.chopperbot.guilds.outlierscoaching.OutliersCoaching;
import me.vixen.chopperbot.guilds.vincentgsmmods.VincentsGMMods;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

public class GuildManager {
	List<CustomGuild> guilds = new ArrayList<>();

	public GuildManager(EventWaiter waiter) {
		addGuild(new BejoIjoPlugins("663796409635569664", waiter));
		addGuild(new LSPDFRTS("788491012553179217", waiter));
		addGuild(new EFoxHomeBase("882694324112994315", waiter));
		addGuild(new VincentsGMMods("692321202508922931", waiter));
		addGuild(new OutliersCoaching("613412156460761109", waiter));
	}

	private void addGuild(CustomGuild guild) {
		if (this.contains(guild)) throw new IllegalArgumentException("This guild already added: " + guild.getId());
		guilds.add(guild);
	}

	public boolean contains(Guild g) {
		return guilds.stream().anyMatch(it -> it.getId().equalsIgnoreCase(g.getId()));
	}

	public boolean contains(CustomGuild g) {
		return guilds.stream().anyMatch(it -> it.getId().equalsIgnoreCase(g.getId()));
	}

	public List<CustomGuild> getGuilds() { return List.copyOf(guilds); }

	public CustomGuild getGuild(String guildId) {
		for (CustomGuild g : guilds) {
			if (g.getId().equalsIgnoreCase(guildId)) return g;
		}
		return null;
	}

	public CustomGuild getGuild(Guild g) {
		for (CustomGuild ig : guilds) {
			if (ig.getId().equalsIgnoreCase(g.getId())) return ig;
		}
		return null;
	}
}
