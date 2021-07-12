package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.Database.Warning;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WarningGroup implements ICommand {

	EventWaiter waiter;
	public WarningGroup(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		DBMember moderator = Database.getMember(event.getGuild(), event.getUser().getId());
		if (!moderator.isAuthorized()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}
		OptionMapping userOpt = event.getOption("user");
		if (userOpt == null) {
			event.replyEmbeds(Embeds.getInvalidArgumentEmbed("user", "Must be provided")).queue();
			return;
		}

		User user = userOpt.getAsUser();
		DBMember target = Database.getMember(event.getGuild(), user.getId());
		final String name = event.getSubcommandName();

		switch (name) {
			case "give" -> warn(event, target, moderator);
			case "infractions" -> getInfractions(event, target);
			case "delete" -> {
				final int id = (int) event.getOption("id").getAsLong();
				Warning warning = target.getWarnings().stream().filter(it -> it.getWarningNumber() == id).findFirst().orElse(null);
				if (!warning.equals(null)) {
					target.removeWarning(warning);
					target.update();
					event.reply("Warning removed successfully!").queue();
				} else event.reply("An error occurred! Most likely an Invalid Id#").setEphemeral(true).queue();
			}
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("warn", "Warning Commands").addSubcommands(
			new SubcommandData("give", "Give a new warning").addOptions(
				new OptionData(OptionType.USER, "user", "The user to warn",true),
				new OptionData(OptionType.STRING, "reason", "The reason for warning",true)
			),
			new SubcommandData("delete", "Delete a warning (needs warning id from /warn infractions)").addOptions(
					new OptionData(OptionType.INTEGER, "warnnum", "The warning number",true),
					new OptionData(OptionType.USER, "user", "The user to delete from", true)
			),
			new SubcommandData("infractions", "Get a users warnings")
				.addOption(OptionType.USER, "user", "The user to get", true)
		).setDefaultEnabled(false);
	}

	private static void warn(SlashCommandEvent event, DBMember target, DBMember moderator) {
		event.deferReply().queue();
		event.getGuild().retrieveMembersByIds(target.getUserId(), moderator.getUserId()).onSuccess(members -> {
			Member targetMem = null, modMem = null;
			for (Member m : members) {
				if (m.getId().equals(target.getUserId())) targetMem = m;
				else if (m.getId().equals(moderator.getUserId())) modMem = m;
			}
			if (targetMem == null || modMem == null) {
				event.getHook().editOriginal("Couldn't add warning; submit bug report").queue();
				return;
			}
			target.addWarning(targetMem.getId(), modMem.getUser(), event.getOption("reason").getAsString());
			target.update();

			MessageEmbed embed = new EmbedBuilder()
				.setTitle("New Warning Given!")
				.addField(modMem.getUser().getAsTag() + " warned " + targetMem.getUser().getAsTag(), event.getOption("reason").getAsString(), false)
				.setColor(Color.YELLOW)
				.build();
			event.getHook().editOriginalEmbeds(embed).queue();
			Config config = Database.getConfig(event.getGuild().getId());
			if (config == null) {
				event.getTextChannel().sendMessageEmbeds(Embeds.getPleaseDoConfig()).queue();
			} else {
				event.getGuild().getTextChannelById(config.getModlogId()).sendMessageEmbeds(embed).queue();
			}
		});
	}

	private void getInfractions(SlashCommandEvent event, DBMember target) {
		List<Warning> warnings = target.getWarnings();
		String[] warningArr = new String[warnings.size()];
		for (int i = 0; i<= warnings.size(); i++) {
			warningArr[i] = warnings.get(i).toString();
		}
		Paginator pager = new Paginator.Builder()
			.setItems(warningArr)
			.setItemsPerPage(10)
			.showPageNumbers(true)
			.allowTextInput(true)
			.setLeftRightText("Left", "Right")
			.setBulkSkipNumber(5)
			.wrapPageEnds(false)
			.showPageNumbers(true)
			.setColumns(1)
			.setColor(Color.BLUE)
			.waitOnSinglePage(true)
			.useNumberedItems(false)
			.addUsers(event.getUser())
			.setEventWaiter(waiter)
			.setTimeout(30L, TimeUnit.SECONDS)
			.build();
		pager.paginate(event.getTextChannel(), 1);
		event.reply("Warnings Displayed").setEphemeral(true).queue();
	}
}
