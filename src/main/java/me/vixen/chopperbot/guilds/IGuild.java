package me.vixen.chopperbot.guilds;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.listener.DefaultEventHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.List;
import java.util.stream.Collectors;

public interface IGuild {

	void setLocalCommands(EventWaiter waiter);
	List<ICommand> getLocalCommands();
	String getId();

	default Guild getGuild() {
		Checks.notNull(getId(), "Guild Id");
		Checks.notBlank(getId(), "Guild Id");
		return Entry.jda.getGuildById(getId());
	}

	default String getName() {
		Checks.notNull(getId(), "Guild Id");
		Checks.notBlank(getId(), "Guild Id");
		return getGuild().getName();
	}

	@SuppressWarnings("unused") //Temporarily disabled
	default TextChannel getLottoChannel() {
		return getGuild().getSystemChannel();
	}

	default boolean hasCustomClaims() {
		return false;
	}

	default void getCustomClaim(SlashCommandEvent event) {

	}

	default void doNightlyReset() {
		DefaultEventHandler.nightlyReset(Entry.jda.getGuildById(getId()));
	}

	default List<TextChannel> getTreasureChannels() {
		final Guild g = getGuild();
		final List<TextChannel> textChannels = g.getTextChannels();
		final Role publicRole = g.getPublicRole();
		return textChannels.stream().filter(it ->
			publicRole.hasPermission(it, List.of(
				Permission.MESSAGE_WRITE,
				Permission.MESSAGE_READ,
				Permission.MESSAGE_HISTORY,
				Permission.MESSAGE_ADD_REACTION
			))
		).collect(Collectors.toList());
	}

	default void handleSlashCommand(SlashCommandEvent event, EventWaiter waiter, GlobalCommandManager cManager) {
		DefaultEventHandler.handleSlashCommand(event, cManager);
	}

	default void handleGMsgReceived(GuildMessageReceivedEvent event, EventWaiter waiter) {
		DefaultEventHandler.handleGMsgReceived(event);
	}

	default void handleGMsgReactAdd(GuildMessageReactionAddEvent event, EventWaiter waiter) {
		DefaultEventHandler.handleGMsgReactAdd(event);
	}
	default void handleGMemJoin(GuildMemberJoinEvent event, EventWaiter waiter) {
		DefaultEventHandler.handleGMemJoin(event, waiter);
	}
	default void handleGMemRemove(GuildMemberRemoveEvent event, EventWaiter waiter) {
		DefaultEventHandler.handleGMemRemove(event);
	}

	@SuppressWarnings("unused")
	default void handleGVoiceJoin(GuildVoiceJoinEvent event) {
		DefaultEventHandler.handleGVoiceJoin(event);
	}
	@SuppressWarnings("unused")
	default void handleGVoiceLeave(GuildVoiceLeaveEvent event) {
		DefaultEventHandler.handleGVoiceLeave(event);
	}
	@SuppressWarnings("unused")
	default void handleGVoiceMove(GuildVoiceMoveEvent event) {
		DefaultEventHandler.handleGVoiceMove(event);
	}
}
