package me.vixen.chopperbot.guilds.lspdfrts;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.IGuild;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LSPDFRTS implements IGuild {

	String guildId;
	public LSPDFRTS(String guildId) {
		this.guildId = guildId;
	}

	@Override
	public void setLocalCommands(EventWaiter waiter) {
		//return;
	}

	@Override
	public List<ICommand> getLocalCommands() {
		return new ArrayList<>();
	}

	@Override
	public String getId() {
		return guildId;
	}

	@Override
	public List<TextChannel> getTreasureChannels() {
		List<String> channelIds = List.of(
			"788821946312687656", "815733622216589332",
			"790373472173555719"
		);
		return getGuild().getTextChannels().stream().filter(it ->
			channelIds.contains(it.getId()))
			.collect(Collectors.toList());
	}

	@Override
	public void handleGMemJoin(GuildMemberJoinEvent event, EventWaiter waiter) {
		TextChannel welcome = getGuild().getTextChannelById("811247930836779038");
		if (welcome != null)
			welcome.sendMessageEmbeds(Embeds.getWelcomeEmbed(event.getUser())).queue();
	}

	@Override
	public void handleGMemRemove(GuildMemberRemoveEvent event, EventWaiter waiter) {
		TextChannel welcome = getGuild().getTextChannelById("811247930836779038");
		if (welcome != null)
			welcome.sendMessageEmbeds(Embeds.getLeaveEmbed(event.getUser())).queue();
	}
}
