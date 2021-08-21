package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.database.Warning;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WarningGroup implements ICommand {

	EventWaiter waiter;
	public WarningGroup(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		UserProfile moderator = Database.getMember(event.getGuild(), event.getUser().getId());
		if (moderator == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
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
		UserProfile target = Database.getMember(event.getGuild(), user.getId());
		if (target == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		final String name = event.getSubcommandName();
		event.deferReply().queue();

		//noinspection ConstantConditions cant be null
		switch (name) {
			case "give" -> warn(event, target, moderator);
			case "infractions" -> getInfractions(event, target);
			case "delete" -> {
				//noinspection ConstantConditions cant be null
				final int id = (int) event.getOption("id").getAsLong();
				Warning warning = target.getWarnings().stream().filter(it -> it.getWarningNumber() == id).findFirst().orElse(null);
				if (warning != null) {
					target.removeWarning(warning);
					target.update(null);
					event.getHook().editOriginal("Warning removed successfully!").queue();
				} else event.getHook().editOriginal("An error occurred! Most likely an Invalid Id#").queue();
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
		);
	}

	private static void warn(SlashCommandEvent event, UserProfile target, UserProfile moderator) {
		if (target.getUserId().equals(moderator.getUserId())) {
			event.reply("You cannot warn yourself").queue();
			return;
		}
		//noinspection ConstantConditions this will not be null as we do not accept slashcommands from private channels
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
			//noinspection ConstantConditions cant be null
			target.addWarning(targetMem.getId(), modMem.getUser(), event.getOption("reason").getAsString());
			target.update(null);

			//noinspection ConstantConditions cant be null
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
				String modlogId = config.getModlogId();
				TextChannel modlog;
				if (modlogId == null || (modlog = event.getGuild().getTextChannelById(modlogId)) == null)
					event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
				else if (event.getGuild().getTextChannelById(modlogId) != null) {
					modlog.sendMessageEmbeds(embed).queue();
				}
			}
		});
	}

	private void getInfractions(SlashCommandEvent event, UserProfile target) {
		List<Warning> warnings = Database.getWarnings(target.getGuildId(), target.getUserId());
		if (warnings == null || warnings.isEmpty()) {
			event.reply("This user has no infractions in this server").queue();
			return;
		}
		List<String> forArr = new ArrayList<>();
		for (Warning w : warnings)
			forArr.add(w.toPrettyString());
		//noinspection ConstantConditions cant be null
		Paginator pager = new Paginator.Builder()
			.setText("Warnings for:" + event.getOption("user").getAsUser().getAsTag())
			.setItems(forArr.toArray(new String[0]))
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
		event.getHook().editOriginal("Warnings Displayed").queue();
	}
}
