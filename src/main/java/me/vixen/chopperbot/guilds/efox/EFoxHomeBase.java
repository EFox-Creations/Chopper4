package me.vixen.chopperbot.guilds.efox;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import jdk.jfr.Event;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.IGuild;
import me.vixen.chopperbot.listener.DefaultEventHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;
import java.util.stream.Collectors;


public class EFoxHomeBase implements IGuild {

	String guildId;
	private static List<ICommand> localCommands;
	public EFoxHomeBase(String guildId, EventWaiter waiter) {
		this.guildId = guildId;
		setLocalCommands(waiter);
	}

	@Override
	public void setLocalCommands(EventWaiter waiter) {
		localCommands = List.of(
			new IssueCommand(waiter)
		);
	}

	@Override
	public List<ICommand> getLocalCommands() {
		return localCommands;
	}

	@Override
	public String getId() {
		return guildId;
	}

	@Override
	public List<TextChannel> getTreasureChannels() {
		List<String> channelIds = List.of(
			"762470329759039499", "768325475923001385",
			"795147170785001522"
		);
		return getGuild().getTextChannels().stream().filter(it ->
			channelIds.contains(it.getId()))
			.collect(Collectors.toList());
	}

	protected static boolean isTicketTeam(Member member) {
		Guild guild = member.getGuild();
		Role vixen = guild.getRoleById("869659092174667806");
		Role ticketTeam = guild.getRoleById("858170472037744650");
		List<Role> roles = member.getRoles();
		return (roles.contains(vixen) || roles.contains(ticketTeam));
	}

	@Override
	public void handleSlashCommand(SlashCommandEvent event, EventWaiter waiter, GlobalCommandManager cManager) {
		boolean found = false;
		for (ICommand c : getLocalCommands()) {
			if (c.getName().equals(event.getName())) {
				c.handle(event);
				found = true;
			}
		}
		if (!found) DefaultEventHandler.handleSlashCommand(event, cManager);
	}
}
