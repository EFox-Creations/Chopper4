package me.vixen.chopperbot.commands.global.gamble;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Lotto {
	public void handle(SlashCommandEvent event, UserProfile profile) {
		//noinspection ConstantConditions cant be null
		switch (event.getSubcommandName()) {
			case "lotto_pool" -> getStatus(event);
			case "buy_lotto" -> placeBet(event);
		}
	}

	private static void getStatus(SlashCommandEvent event) {
		final int pot = Database.getPot();
		event.replyFormat("The current prize pool is %s coins", pot).queue();
	}

	private static void placeBet(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		UserProfile dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
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
		} else if (Database.doesBetExist(event.getUser().getId(), event.getGuild().getId())) {
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

			boolean betEntered = Database.addBet(event.getUser().getId(), event.getGuild().getId(), betStr);
			boolean potAdded = Database.addToPot(betAmount);

			if (betEntered && potAdded) {
				dbMember.adjustCoins(-betAmount);
				dbMember.update(null);
				event.getHook().editOriginalEmbeds(new EmbedBuilder()
					.setAuthor(event.getUser().getAsTag() + "'s Lotto Ticket", null, event.getUser().getAvatarUrl())
					.setColor(Color.GREEN)
					.setTitle(betStr)
					.setDescription("⚠⚠Be sure you have DMs on in a mutual server to know if you win!⚠⚠")
					.build()
				).queue(msg -> msg.delete().queueAfter(10L, TimeUnit.SECONDS));
			} else event.getHook().editOriginal("An error occurred; aborting with code " + Errors.LOTTOADD).queue();
		} else event.getHook().editOriginalFormat("All numbers must be between 1 and %d, inclusive", UPPER).queue();
	}

	public static int UPPER = 25;

	private static boolean isAcceptable(int a) {
		return a <= UPPER && a > 0;
	}
}