package me.vixen.chopperbot.listener;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class Listener extends ListenerAdapter {
	private final EventWaiter waiter;
	private final GlobalCommandManager commandManager;
	private final GuildManager guildManager;

	public Listener(EventWaiter waiter, GlobalCommandManager cManager, GuildManager guildManager) {
		this.waiter = waiter;
		this.commandManager = cManager;
		this.guildManager = guildManager;
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		System.out.println("Fully Ready");
	}

	@Override
	public void onGuildJoin(@NotNull GuildJoinEvent event) {
		Database.createMemberTables(List.of(event.getGuild()));
		event.getGuild().retrieveOwner().queue(owner -> {
			if (owner != null)
				new DBMember(owner, event.getGuild(), true).update(); //Authorize owner
			TextChannel systemChannel = event.getGuild().getSystemChannel();
			if (systemChannel != null)
				systemChannel.sendMessageEmbeds(Embeds.getOnJoin()).queue();
			else {
				TextChannel defaultChannel = event.getGuild().getDefaultChannel();
				if (defaultChannel != null)
					defaultChannel.sendMessageEmbeds(Embeds.getOnJoin()).queue();
			}
		});
	}

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event) {
		if (event.getUser().isBot()) return;
		//noinspection ConstantConditions cant be null
		if (Database.getMember(event.getGuild(), event.getUser().getId()) == null && !event.getUser().isBot()) {
			//noinspection ConstantConditions cant be null
			Database.upsertMember(event.getGuild(), new DBMember(event.getMember(), event.getGuild(), false));
		}
		if (guildManager.contains(event.getGuild())) //If guild has custom actions
			guildManager.getGuild(event.getGuild()).handleSlashCommand(event, waiter, commandManager);
		else DefaultEventHandler.handleSlashCommand(event, commandManager); //else send to default handler
	}

	@Override
	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		if (event.getAuthor().isBot()) return;
		if (Database.getMember(event.getGuild(), event.getAuthor().getId()) == null && !event.getAuthor().isBot()) {
			//noinspection ConstantConditions cant be null
			Database.upsertMember(event.getGuild(), new DBMember(event.getMember(), event.getGuild(), false));
		}
		if (guildManager.contains(event.getGuild())) //If guild has custom actions
			guildManager.getGuild(event.getGuild()).handleGMsgReceived(event, waiter);
		else DefaultEventHandler.handleGMsgReceived(event); //else send to default handler
	}

	@Override
	public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
		if (guildManager.contains(event.getGuild())) //If guild has custom actions
			guildManager.getGuild(event.getGuild()).handleGMsgReactAdd(event, waiter);
		else DefaultEventHandler.handleGMsgReactAdd(event); //else send to default handler
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		if (guildManager.contains(event.getGuild())) //If guild has custom actions
			guildManager.getGuild(event.getGuild()).handleGMemJoin(event, waiter);
		else DefaultEventHandler.handleGMemJoin(event); //else send to default handler
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		if (guildManager.contains(event.getGuild())) //If guild has custom actions
			guildManager.getGuild(event.getGuild()).handleGMemRemove(event, waiter);
		else DefaultEventHandler.handleGMemRemove(event); //else send to default handler
	}
}
