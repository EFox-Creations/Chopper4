package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class LeaderboardCommand implements ICommand {

	EventWaiter waiter;
	public LeaderboardCommand(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		//noinspection ConstantConditions is required
		final int entries = (int) event.getOption("entries").getAsLong();
		//noinspection ConstantConditions cant be null
		final String[] leaderboard = Database.getLeaderboard(event.getGuild(), entries);

		if (leaderboard == null) {
			event.reply("Couldn't retrieve leaderboard").setEphemeral(true).queue();
			return;
		}

		//noinspection ConstantConditions cant be null
		Paginator pager = new Paginator.Builder()
			.setText(event.getMember().getAsMention() + "\nName --- EXP --- Currency")
			.addItems(leaderboard)
			.setEventWaiter(waiter)
			.setTimeout(20L, TimeUnit.SECONDS)
			.setBulkSkipNumber(5)
			.allowTextInput(true)
			.setLeftRightText("Left", "Right")
			.setColor(Color.CYAN)
			.setColumns(1)
			.setItemsPerPage(10)
			.showPageNumbers(true)
			.waitOnSinglePage(true)
			.useNumberedItems(true)
			.wrapPageEnds(true)
			.addUsers(event.getUser())
			.build();
		pager.paginate(event.getTextChannel(), 1);
		event.reply("Leaderboard shown").setEphemeral(true).queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("leaderboard", "Shows the leaderboard of the guild")
			.addOption(OptionType.INTEGER, "entries", "The number of entries you want to see", true);
	}
}
