package me.vixen.chopperbot.listener;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.database.DBMember;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		//Check blacklisted domain links
		Config config = Database.getConfig(event.getGuild().getId());
		if (config == null || config.getDomains() == null || config.getDomains().isEmpty()) return;
		String rawMsg = event.getMessage().getContentRaw();

		//Check secure links
		Matcher matcher = Pattern.compile("(https:\\/\\/.+\\.)[^\\/]{2,63}", Pattern.CASE_INSENSITIVE)
			.matcher(rawMsg.replaceFirst("www\\.", ""));
		while (matcher.find()) {
			if (config.getDomains().contains(matcher.group(0).replace("https://", "")))
				doPunishment(event, config);
		}

		//Check regular links
		Matcher matcher1 = Pattern.compile("(http:\\/\\/.+\\.)[^\\/]{2,63}", Pattern.CASE_INSENSITIVE)
			.matcher(rawMsg.replaceFirst("www\\.", ""));
		while (matcher1.find()){
			if (config.getDomains().contains(matcher1.group(0).replace("http://", "")))
				doPunishment(event, config);
		}
	}

	private void doPunishment(GuildMessageReceivedEvent event, Config config) {
		event.getMessage().delete().queue();
		switch (config.getPunishment()) {
			case NONE -> {
				return;
			}
			case WARN -> {
				DBMember member = Database.getMember(event.getGuild(), event.getAuthor().getId());
				member.addWarning(event.getAuthor().getAsTag(), Entry.jda.getSelfUser(), "Posting blacklisted links");
				member.update();
				MessageEmbed embed = new EmbedBuilder()
					.setTitle("New Warning Given!")
					.addField(Entry.jda.getSelfUser().getAsTag() + " warned " + event.getAuthor().getAsTag(),
						"Posting blacklisted links", false)
					.setColor(Color.YELLOW)
					.build();
				event.getGuild().getTextChannelById(config.getModlogId()).sendMessageEmbeds(embed).queue();
				event.getChannel().sendMessageEmbeds(embed).queue();
			}
			case KICK -> {
				event.getMember().kick("Posting blacklisted links").queue(v -> {
					event.getGuild().getTextChannelById(config.getModlogId()).sendMessageEmbeds(
						Embeds.getKickedEmbed(event.getAuthor(), Entry.jda.getSelfUser(), "Posting blacklisted links")
					).queue();
				});
			}
			case BAN -> {
				event.getMember().ban(7, "Posting blacklisted links").queue(v -> {
					event.getGuild().getTextChannelById(config.getModlogId()).sendMessageEmbeds(
						Embeds.getBannedEmbed(event.getAuthor(), Entry.jda.getSelfUser(), "Posting blacklisted links")
					).queue();
				});
			}
		}
	}

	@Override
	public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
		if (guildManager.contains(event.getGuild())) //If guild has custom actions
			guildManager.getGuild(event.getGuild()).handleGMsgReactAdd(event, waiter);
		else DefaultEventHandler.handleGMsgReactAdd(event); //else send to default handler

		event.retrieveMessage().queue(m -> { //allow me to delete dud chests
			if (m.getEmbeds().size() > 0) {
				String title = m.getEmbeds().get(0).getTitle();
				if (title != null && title.equals("🏝 A safe has washed ashore!")
					&& event.getUserId().equals(Entry.CREATOR_ID)
					&& event.getReactionEmote().getName().equals("🚫"))
					m.delete().queue();
			}
		});
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		if (guildManager.contains(event.getGuild())) //If guild has custom actions
			guildManager.getGuild(event.getGuild()).handleGMemJoin(event, waiter);
		else DefaultEventHandler.handleGMemJoin(event, waiter); //else send to default handler
	}

	@Override
	public void onButtonClick(@NotNull ButtonClickEvent event) {
		switch (event.getComponentId()) {
			case "getjoinid" -> {
				event.deferEdit().queue();
				event.getChannel().sendMessage(
					event.getMessage().getEmbeds().get(0).getFooter().getText().replaceFirst("Id: ", "")
				).mention(event.getMember()).queue(msg -> msg.delete().queueAfter(10L, TimeUnit.SECONDS));
			}
		}
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		if (guildManager.contains(event.getGuild())) //If guild has custom actions
			guildManager.getGuild(event.getGuild()).handleGMemRemove(event, waiter);
		else DefaultEventHandler.handleGMemRemove(event); //else send to default handler
	}
}