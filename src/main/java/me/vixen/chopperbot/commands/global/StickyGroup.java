package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;

public class StickyGroup implements ICommand {
	@Override
	public void handle(SlashCommandEvent event, UserProfile profile) {
		if (!profile.isAuthorized()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		//noinspection ConstantConditions cant be null
		switch (event.getSubcommandName()) {
			case "add" -> addSticky(event);
			case "delete" -> deleteSticky(event);
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("sticky", "Sticky message commands").addSubcommands(
			new SubcommandData("add", "Add a sticky message to this channel")
				.addOption(OptionType.STRING, "content", "The content of the sticky (\"<n>\" for newline)", true),
			new SubcommandData("delete", "Delete the sticky message from this channel")
		);
	}

	private static void addSticky(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		final String content = event.getOption("content").getAsString().replaceAll("<n>", "\n");
		MessageEmbed embed = new EmbedBuilder()
			.setTitle("📌 Pinned 📌")
			.setColor(Color.YELLOW)
			.setDescription(content)
			.build();
		event.getTextChannel().sendMessageEmbeds(embed).queue(message -> {
			final boolean success = Database.upsertSticky(event.getTextChannel(), message);
			if (success) event.reply("Sticky added successfully").setEphemeral(true).queue();
			else event.reply("Could not apply glue to the message!").setEphemeral(true).queue();
		});
	}

	private static void deleteSticky(SlashCommandEvent event) {
		String messageId;
		if ((messageId = Database.getStickyId(event.getTextChannel())) == null) {
			event.reply("I couldn't find a sticky message in this channel").queue();
			return;
		}

		final boolean success = Database.deleteSticky(event.getTextChannel());
		event.getTextChannel().retrieveMessageById(messageId).queue(message -> {
			message.delete().queue();
			if (success) event.reply("Sticky scraped off").setEphemeral(true).queue();
			else event.reply("This is some tough stuff! Try again later").setEphemeral(true).queue();
		});
	}
}
