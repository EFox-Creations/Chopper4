package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Database.Command;
import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class CustomCommand implements ICommand {

	private EventWaiter waiter;
	public CustomCommand(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		OptionMapping cmdOpt = event.getOption("cmdname");
		if (cmdOpt != null) {
			String cmdname = cmdOpt.getAsString();
			Command cmd = Database.getCommandByName(guild, cmdname);
			if (cmd == null) {
				StringBuilder builder = new StringBuilder();
				for (Command c : Database.getCommands(guild))
					if (FuzzySearch.ratio(c.getName(), cmdname) >= 50)
						builder.append(c.getName()).append("\n");
				event.replyEmbeds(
					new EmbedBuilder()
						.setColor(Color.RED)
						.setTitle(String.format("⛔ No Command \"%s\" exists!", cmdname))
						.addField("Did you mean: ", builder.toString(), false)
						.build()
				).queue();
				return;
			}
			DBMember member = Database.getMember(guild, event.getUser().getId());
			if (cmd.isStaffOnly() && !member.isAuthorized()) {
				event.replyEmbeds(Embeds.getPermissionMissing()).queue();
				return;
			}
			OptionMapping msgidOpt = event.getOption("msgid");
			if (msgidOpt == null)
				event.reply(cmd.getResponse()).queue();
			else {
				String msgId = msgidOpt.getAsString();
				event.getTextChannel().retrieveMessageById(msgId).queue(msg -> {
					msg.reply(cmd.getResponse()).mentionRepliedUser(true).queue();
					event.reply("Replied").setEphemeral(true).queue();
				});
			}
		} else {
			event.reply("Please type command name now: (10sec)").setEphemeral(true).queue(hook -> {
				hook.retrieveOriginal().queue(msg -> {
					waiter.waitForEvent(GuildMessageReceivedEvent.class,
						(e) -> e.getAuthor().equals(event.getUser()) && e.getChannel().equals(event.getTextChannel()),
						(e) -> {
							e.getMessage().delete().queue();
							String cmdname = e.getMessage().getContentRaw();
							Command cmd = Database.getCommandByName(guild, cmdname);
							if (cmd == null) {
								StringBuilder builder = new StringBuilder();
								for (Command c : Database.getCommands(guild))
									if (FuzzySearch.ratio(c.getName(), cmdname) >= 50)
										builder.append(c.getName()).append("\n");
								event.replyEmbeds(
									new EmbedBuilder()
										.setColor(Color.RED)
										.setTitle(String.format("⛔ No Command \"%s\" exists!", cmdname))
										.addField("Did you mean: ", builder.toString(), false)
										.build()
								).queue();
								return;
							} else {
								DBMember member = Database.getMember(guild, event.getUser().getId());
								if (cmd.isStaffOnly() && !member.isAuthorized()) {
									event.replyEmbeds(Embeds.getPermissionMissing()).queue();
									return;
								} else {
									e.getChannel().sendMessage(cmd.getResponse()).queue();
								}
							}
						},
						10L, TimeUnit.SECONDS,
						() -> hook.editOriginal("Timed out").queue()
						);
				});
			});
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("c", "Invokes a custom command")
			.addOptions(
				new OptionData(OptionType.STRING, "cmdname", "The command name", false),
				new OptionData(OptionType.STRING, "msgid", "The ID of the msg to reply to", false)
			);
	}
}
