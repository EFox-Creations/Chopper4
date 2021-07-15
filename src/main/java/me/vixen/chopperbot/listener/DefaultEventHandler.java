package me.vixen.chopperbot.listener;

import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DefaultEventHandler {
	public static void handleSlashCommand(SlashCommandEvent event, GlobalCommandManager cManager) {
		final ICommand command = cManager.getGlobalCommand(event.getName());
		if (command == null) event.replyEmbeds(Embeds.getCommandMissing()).queue();
		else command.handle(event);
		updateStickies((TextChannel) event.getChannel());
	}

	public static void updateStickies(TextChannel channel) {
		final String stickyId = Database.getStickyId(channel);
		if (stickyId != null) {
			channel.retrieveMessageById(stickyId).queueAfter(500L, TimeUnit.MILLISECONDS, message -> {
				//wait .5 second for DB to close
				final MessageEmbed messageEmbed = message.getEmbeds().get(0);
				//wait for old message to be deleted before sending the new one
				message.delete().queue(unused -> channel.sendMessageEmbeds(messageEmbed).queue(newMsg -> {
					boolean success = Database.upsertSticky(channel, newMsg);
					if (!success) channel.sendMessage("Couldn't update sticky").queue();
				}));
			});
		}
	}

	public static void handleGMsgReceived(GuildMessageReceivedEvent event) {
		updateStickies(event.getChannel());
		DBMember member = Database.getMember(event.getGuild(), event.getAuthor().getId());
		if (member != null) {
			Config config = Database.getConfig(event.getGuild().getId());
			if (member.awardExp(false) && config != null && !config.arelvlMsgOverridden()) {
				event.getChannel()
					.sendMessageEmbeds(Embeds.getLevelUpEmbed(member.getLevel()))
					.append(event.getAuthor().getAsMention())
					.queue();
			}
		} else System.out.println("DB failed to return member");

	}

	@SuppressWarnings("unused")
	public static void handleGMsgReactAdd(GuildMessageReactionAddEvent event) { }

	public static void handleGMemJoin(GuildMemberJoinEvent event) {
		TextChannel systemChannel = event.getGuild().getSystemChannel();
		if (systemChannel != null)
			systemChannel.sendMessageEmbeds(Embeds.getWelcomeEmbed(event.getUser())).queue();
	}

	public static void handleGMemRemove(GuildMemberRemoveEvent event) {
		TextChannel systemChannel = event.getGuild().getSystemChannel();
		if (systemChannel != null)
			systemChannel.sendMessageEmbeds(Embeds.getLeaveEmbed(event.getUser())).queue();
	}

	@SuppressWarnings("unused")
	public static void handleGVoiceJoin(GuildVoiceJoinEvent event) { }
	@SuppressWarnings("unused")
	public static void handleGVoiceLeave(GuildVoiceLeaveEvent event) { }
	@SuppressWarnings("unused")
	public static void handleGVoiceMove(GuildVoiceMoveEvent event) { }

	public static void nightlyReset(Guild g) {
		Database.resetDailyCounts(g, null, null, null, null);
	}

	public static List<TextChannel> getDefaultTreasureChannels(Guild g) {
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
}
