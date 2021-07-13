package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.awt.*;


public class ReportCommand implements ICommand {

	private final EventWaiter waiter;
	public ReportCommand(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		User user = event.getUser();
		Guild guild = event.getGuild();
		@SuppressWarnings("ConstantConditions") //SlashCommandEvent is not accepted from DMs which is only case for NPE
		Config config = Database.getConfig(guild.getId());
		if (config == null) {
			event.replyEmbeds(Embeds.getPleaseDoConfig()).setEphemeral(true).queue();
			return;
		}
		OptionMapping offender = event.getOption("offender");
		if (offender == null) {
			if (config.getModlogId() == null) {
				event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
				return;
			}
			TextChannel modlog = guild.getTextChannelById(config.getModlogId());
			if (modlog == null) {
				event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
				return;
			}
			//noinspection ConstantConditions option is required, cant be null
			modlog.sendMessageEmbeds(
				new EmbedBuilder()
					.setColor(Color.YELLOW)
					.setTitle(user.getAsTag() + " submitted a report!")
					.setDescription(event.getOption("report").getAsString())
					.addField("Timestamp", TimeFormat.RELATIVE.now().toString(), false)
					.build()
			).setActionRow(
				Button.success("claim", "Mark Done"),
				Button.primary("faux", "Mark Faux"),
				Button.danger("delete", "Delete").withEmoji(Emoji.fromUnicode("⚠"))
			).queue(msg -> {
				waitForButtons(msg);
				event.reply("Report Submitted").setEphemeral(true).queue();
			});
		} else {
			if (offender.getAsMember() != null) {
				if (config.getModlogId() == null) {
					event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				TextChannel modlog = guild.getTextChannelById(config.getModlogId());
				if (modlog == null) {
					event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
					return;
				}
				//noinspection ConstantConditions option is required, cant be null
				modlog.sendMessageEmbeds(
					new EmbedBuilder()
						.setColor(Color.YELLOW)
						.setTitle(user.getAsTag() + " reported " + offender.getAsUser().getAsTag())
						.setDescription(event.getOption("report").getAsString())
						.addField("Timestamp", TimeFormat.RELATIVE.now().toString(), false)
						.build()
				).setActionRow(
					Button.success("claim", "Mark Resolved"),
					Button.primary("faux", "Mark Faux/NEI"),
					Button.danger("delete", "Delete").withEmoji(Emoji.fromUnicode("⚠"))
				).queue(msg -> {
					waitForButtons(msg);
					event.reply("Report Submitted").setEphemeral(true).queue();
				});
			} else event.replyEmbeds(Embeds.getUnknownMember()).setEphemeral(true).queue();
		}
	}

	private void waitForButtons(Message msg) {
		waiter.waitForEvent(
			ButtonClickEvent.class,
			(bce) -> bce.getMessageId().equals(msg.getId()),
			(bce) -> editEmbed(bce, msg, bce.getComponentId())
		);
	}

	private void editEmbed(ButtonClickEvent event, Message msg, String componentId) {
		MessageEmbed messageEmbed = msg.getEmbeds().get(0);
		EmbedBuilder builder = copyEmbed(messageEmbed);
		switch (componentId) {
			case "claim" -> {
				builder.setColor(Color.GREEN);
				builder.addField("Report resolved", "Handled by: " + event.getUser().getAsTag(), false);
				event.reply("Marked as resolved").setEphemeral(true).queue();
			}
			case "faux" -> {
				builder.setColor(Color.DARK_GRAY);
				builder.addField("Report faux/Not enough information", "Marked by: " + event.getUser().getAsTag(), false);
				event.reply("Marked as faux/NEI").setEphemeral(true).queue();
			}
			case "delete" -> msg.delete().queue();
		}
		msg.editMessageEmbeds(builder.build()).setActionRow().queue();
	}

	private EmbedBuilder copyEmbed(MessageEmbed embed) {
		EmbedBuilder builder = new EmbedBuilder()
			.setColor(embed.getColor())
			.setTitle(embed.getTitle())
			.setDescription(embed.getDescription());
		for (MessageEmbed.Field f: embed.getFields())
			builder.addField(f);
		return builder;
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("report", "Report something to the higher ups")
			.addOptions(
				new OptionData(OptionType.STRING, "report", "What to report?", true),
				new OptionData(OptionType.USER, "offender", "Who committed the offense?")
			);
	}
}
