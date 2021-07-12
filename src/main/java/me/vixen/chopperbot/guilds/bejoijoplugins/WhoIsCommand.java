package me.vixen.chopperbot.guilds.bejoijoplugins;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;


import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class WhoIsCommand implements ICommand {
	EventWaiter waiter;
	public WhoIsCommand(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (!dbMember.isAuthorized()) {
			event.reply("You do not have the correct permissions").queue();
			return;
		}

		final User user = event.getOption("user").getAsUser();
		if (user == null) {
			event.reply("I cannot find this user").queue();
			return;
		}

		final String NOTIN = "Not currently in this guild";
		try {
			event.getGuild().retrieveMember(user).queue(member -> {
				final OffsetDateTime timeJoined = member.getTimeJoined();
				final String joinF = timeJoined.format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy HH:mm"));
				final int numOfRoles = member.getRoles().size();

				boolean purge = false;
				if (ChronoUnit.DAYS.between(timeJoined, OffsetDateTime.now()) > 5 && numOfRoles == 0) purge = true;
				final MessageEmbed whoIsEmbed = getEmbed(user, joinF, numOfRoles, getRoleString(member), purge);

				if (!purge) event.replyEmbeds(whoIsEmbed).queue();
				else {
					event.replyEmbeds(whoIsEmbed).addActionRow(
						Button.danger("whois_yes_kick", "KICK"),
						Button.secondary("whois_no_kick", "Let Stay")
					).queue(hook -> {
						hook.retrieveOriginal().queue(msg -> {
							waiter.waitForEvent(
								ButtonClickEvent.class,
								(e) -> e.getMessageId().equals(msg.getId()) && e.getUser().getId().equals(event.getUser().getId()),
								(e) -> handleButtonPress(event, e, member, msg)
							);
						});
					});
				}
			});
		} catch (NullPointerException e) {
			event.replyEmbeds(getEmbed(user, NOTIN, 0, NOTIN, false)).queue();
		}
	}

	private void handleButtonPress(SlashCommandEvent event, ButtonClickEvent e, Member member, Message m) {
		if (e.getComponentId().equals("whois_yes_kick")) {
			m.delete().queue();
			member.kick("Older than 5 days with no roles").queue(success -> {
					m.getTextChannel().sendMessage(
						event.getMember().getAsMention() +
							" kicked " + member.getUser().getAsTag() +
							" who was older than 5 days with no roles").queue();
				},
				failure -> {
					m.getTextChannel().sendMessage("Failed to kick: " + member.getUser().getAsTag()).queue();
				});
		} else {
			final MessageEmbed embed = m.getEmbeds().get(0);
			m.delete().queue(success -> m.getTextChannel().sendMessageEmbeds(embed).queue());
		}
	}

	private String getRoleString(Member m) {
		StringBuilder builder = new StringBuilder();
		for (Role r : m.getRoles()) {
			builder.append(r.getAsMention()).append("\n");
		}
		return builder.toString().trim();
	}

	private MessageEmbed getEmbed(User u, String timeJoined, int roleCount, String roles, boolean purgeMsg) {
		EmbedBuilder eb = new EmbedBuilder()
			.setTitle(u.getAsTag())
			.setThumbnail(u.getAvatarUrl())
			.setDescription(u.getAsMention())
			.setColor(Color.CYAN)
			.addField("Joined", timeJoined, true)
			.addField("Registered", u.getTimeCreated().format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy HH:mm")), true)
			.addField("Roles [" + roleCount + "]", roles, false)
			.setFooter("ID: " + u.getId());

		if (purgeMsg) eb.addField("This user is 5+ days old with no roles! Kick them?", " ", false);

		return eb.build();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("whois", "Pull up info on a user")
			.addOption(OptionType.USER, "user", "The user to get", true);
	}
}
