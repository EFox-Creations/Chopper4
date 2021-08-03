package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import me.vixen.chopperbot.database.Command;
import me.vixen.chopperbot.database.DBMember;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CustomGroup implements ICommand {

	GlobalCommandManager manager;
	EventWaiter waiter;
	public CustomGroup(GlobalCommandManager manager, EventWaiter waiter) {
		this.manager = manager;
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		@SuppressWarnings("ConstantConditions") //This cant be null as we don't accept DM SCE
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		if (!dbMember.isAuthorized()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).setEphemeral(true).queue();
			return;
		}

		final Guild guild = event.getGuild();
		final String name = event.getSubcommandName();

		//noinspection ConstantConditions cant be null
		switch (name) {
			case "add" -> {
				//noinspection ConstantConditions
				final String cmdname = event.getOption("name").getAsString();
				//noinspection ConstantConditions
				final boolean staffonly = event.getOption("staffonly").getAsBoolean();
				//noinspection ConstantConditions
				final String response = event.getOption("response").getAsString();
				Command cmd = new Command.Builder()
					.setName(cmdname.toLowerCase())
					.setStaffOnly(staffonly)
					.setResponse(response)
					.build();
				final boolean success = Database.addCommand(guild, cmd);
				if (success) event.reply("Command added!").queue();
				else event.reply("An error occurred").setEphemeral(true).queue();
			}
			case "delete" -> {
				//noinspection ConstantConditions
				final String cmdname = event.getOption("cmdname").getAsString();
				final Command command = Database.getCommandByName(guild, cmdname);
				if (command == null) {
					event.reply("No such command exists").queue();
					return;
				}

				final boolean success = Database.deleteCommand(guild, cmdname);

				if (success) event.reply("Command deleted successfully").queue();
				else event.reply("An error occurred; aborting with code " + Errors.COMMAND1).queue();
			}
			case "setname" -> {
				//noinspection ConstantConditions
				final String oldname = event.getOption("oldname").getAsString();
				final Command command = Database.getCommandByName(guild, oldname);
				if (command == null) {
					event.reply("No such command exists").queue();
					return;
				}
				//noinspection ConstantConditions
				final String newname = event.getOption("newname").getAsString();
				boolean success = Database.changeCommandName(guild, oldname, newname);
				if (success) event.reply("Command name changed successfully").queue();
				else event.reply("An error occurred").setEphemeral(true).queue();
			}
			case  "setstaffonly" -> {
				//noinspection ConstantConditions
				final String commandname = event.getOption("commandname").getAsString();
				Command command = Database.getCommandByName(guild, commandname);
				if (command == null) {
					event.reply("No such command exists").queue();
					return;
				}
				//noinspection ConstantConditions
				final boolean staffonly = event.getOption("staffonly").getAsBoolean();
				final boolean success = Database.changeStaffOnly(guild, commandname, staffonly);
				if (success) event.reply("Command staff only changed successfully").queue();
				else event.reply("An error occurred").setEphemeral(true).queue();
			}
			case "setresponse" -> {
				//noinspection ConstantConditions
				final String commandname = event.getOption("commandname").getAsString();
				Command command = Database.getCommandByName(guild, commandname);
				if (command == null) {
					event.reply("No such command exists").queue();
					return;
				}
				//noinspection ConstantConditions
				final String response = event.getOption("newresponse").getAsString();
				final boolean success = Database.changeResponse(guild, commandname, response);
				if (success) event.reply("Command response changed successfully").queue();
				else event.reply("An error occurred").setEphemeral(true).queue();
			}
			case "viewall" -> {
				final List<Command> commands = Database.getCommands(guild);
				commands.sort(Comparator.comparing(Command::getName));
				String[] strArr = new String[commands.size()];
				for (int i = 0; i < commands.size(); i++) strArr[i] = commands.get(i).getName();
				//noinspection ConstantConditions
				Paginator pager = new Paginator.Builder()
					.setText(event.getMember().getAsMention())
					.setEventWaiter(waiter)
					.setItems(strArr)
					.waitOnSinglePage(true)
					.allowTextInput(false)
					.setColor(Color.CYAN)
					.setColumns(2)
					.setTimeout(1L, TimeUnit.MINUTES)
					.setBulkSkipNumber(2)
					.setItemsPerPage(10)
					.showPageNumbers(true)
					.useNumberedItems(false)
					.wrapPageEnds(true)
					.addUsers(event.getUser())
					.build();
				event.reply("Menu shown").setEphemeral(true).queue();
				pager.display(event.getTextChannel());
			}
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("custom", "Custom command commands").addSubcommands(
			new SubcommandData("add", "Add a new custom command").addOptions(
				new OptionData(OptionType.STRING, "name", "The name of the command", true),
				new OptionData(OptionType.BOOLEAN, "staffonly", "Should only authorized members be able to use this?", true),
				new OptionData(OptionType.STRING, "response", "The response for the command (put \"<n>\" for a newline)", true)
			),
			new SubcommandData("delete", "Delete a custom command")
				.addOption(OptionType.STRING, "cmdname", "The command to delete", true),
			new SubcommandData("setname", "Change the name of a custom command").addOptions(
				new OptionData(OptionType.STRING, "oldname", "The current command name", true),
				new OptionData(OptionType.STRING, "newname", "The new name", true)
			),
			new SubcommandData("setstaffonly", "Set a new value for staff only").addOptions(
				new OptionData(OptionType.STRING, "commandname", "The command to change", true),
				new OptionData(OptionType.BOOLEAN, "staffonly", "Should only authorized members be able to use this?", true)
			),
			new SubcommandData("setresponse", "Change the response for a custom command").addOptions(
				new OptionData(OptionType.STRING, "commandname", "The name of the command",true),
				new OptionData(OptionType.STRING, "newresponse", "The new response for the command", true)
			),
			new SubcommandData("viewall", "Shows a list of all custom commands")
		);
	}
}
