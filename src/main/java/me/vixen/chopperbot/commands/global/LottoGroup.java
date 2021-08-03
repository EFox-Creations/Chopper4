package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.DBMember;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class LottoGroup implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		switch (event.getSubcommandName()) {
			case "pool" -> getStatus(event);
			case "place_bet" -> placeBet(event);
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("lotto", "Lotto Commands").addSubcommands(
			new SubcommandData("pool", "Displays the current pool"),
			new SubcommandData("place_bet", "Place a new Lotto bet").addOptions(
				new OptionData(OptionType.INTEGER, "betamount", "The amount you want to bet", true),
				new OptionData(OptionType.INTEGER, "firstnumber", "The first number (1-" + UPPER + ")", true),
				new OptionData(OptionType.INTEGER, "secondnumber", "The second number (1-" + UPPER + ")", true),
				new OptionData(OptionType.INTEGER, "thirdnumber", "The third number (1-" + UPPER + ")", true),
				new OptionData(OptionType.INTEGER, "fourthnumber", "The fourth number (1-" + UPPER + ")", true),
				new OptionData(OptionType.INTEGER, "fifthnumber", "The fifth number (1-" + UPPER + ")", true)
			)
		).setDefaultEnabled(false);
	}

	private static void getStatus(SlashCommandEvent event) {
		final int pot = Database.getPot();
		event.replyFormat("The current prize pool is %s coins", pot).queue();
	}

	private static void placeBet(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		event.deferReply().queue();
		//noinspection ConstantConditions cant be null
		final int betAmount = (int) event.getOption("betamount").getAsLong();
		if (dbMember.getCoins() < betAmount) {
			event.getHook().editOriginalEmbeds(Embeds.getInsufficientCoins()).queue();
			return;
		} else if (Database.doesBetExist(event.getUser().getId())) {
			event.getHook().editOriginal("You have already entered the lotto for today!").queue();
			return;
		} else if (betAmount <= 0) {
			event.replyEmbeds(
				Embeds.getInvalidArgumentEmbed("betamount", "Must be greater than 0")
			).setEphemeral(true).queue();
			return;
		}

		//noinspection ConstantConditions cant be null
		final int firstNumber = (int) event.getOption("firstnumber").getAsLong();
		//noinspection ConstantConditions cant be null
		final int secondNumber = (int) event.getOption("secondnumber").getAsLong();
		//noinspection ConstantConditions cant be null
		final int thirdNumber = (int) event.getOption("thirdnumber").getAsLong();
		//noinspection ConstantConditions cant be null
		final int fourthNumber = (int) event.getOption("fourthnumber").getAsLong();
		//noinspection ConstantConditions cant be null
		final int fifthNumber = (int) event.getOption("fifthnumber").getAsLong();

		if (isAcceptable(firstNumber) && isAcceptable(secondNumber) && isAcceptable(thirdNumber)
			&& isAcceptable(fourthNumber) && isAcceptable(fifthNumber)) {
			String betStr = firstNumber + "," + secondNumber + "," + thirdNumber + "," + fourthNumber + "," + fifthNumber;

			boolean betEntered = Database.addBet(event.getUser().getId(), betStr);
			boolean potAdded = Database.addToPot(betAmount);

			if (betEntered && potAdded) {
				dbMember.adjustCoins(-betAmount);
				dbMember.update();
				event.getHook().editOriginal("Bet added!").queue();
			} else event.getHook().editOriginal("An error occurred; aborting with code " + Errors.LOTTOADD).queue();
		} else event.getHook().editOriginalFormat("All numbers must be between 1 and %d, inclusive", UPPER).queue();
	}

	public static int UPPER = 25;

	private static boolean isAcceptable(int a) {
		return a <= UPPER && a > 0;
	}
}