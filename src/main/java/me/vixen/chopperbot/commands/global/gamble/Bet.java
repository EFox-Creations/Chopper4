package me.vixen.chopperbot.commands.global.gamble;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.Random;

public class Bet {

	public void handle(SlashCommandEvent event, UserProfile profile) {
		//noinspection ConstantConditions cant be null
		final int bet = (int) event.getOption("bet").getAsLong();

		if (bet > profile.getCoins()) {
			event.getHook().editOriginalEmbeds(Embeds.getInsufficientCoins()).setActionRows().queue();
			return;
		} else if (bet <= 0) {
			event.getHook().editOriginal("Your bet must be greater than 0").setActionRows().queue();
			return;
		}

		int payout = bet;
		boolean win = true;

		switch (OUTCOME.getRandom()) {
			case X0 -> {
				payout = 0;
				win = false;
			}
			case X2 -> payout = bet*2;
			case X4 -> payout = bet*4;
			case X8 -> payout = bet*8;
			case X16 -> payout = bet*16;
		}

		MessageEmbed embed = new EmbedBuilder()
			.setTitle("💵 Gamble Results 💵")
			.setColor(win ? Color.GREEN : Color.RED)
			.setDescription("You bet " + bet + " and " + (win ? "WON 🎉" : "LOST 😭"))
			.setFooter("Payout: " + payout + " Net: " + (payout-bet))
			.build();

		profile.adjustCoins(payout-bet);
		profile.playLotto();
		event.getHook().editOriginalEmbeds(embed).setActionRows().queue();
		profile.update(null);
		if (payout <= 0) Database.addToPot(bet);
	}

	private enum OUTCOME {
		X0,  //Lose 50%
		X1,  //Even 25%
		X2,  //Double 13%
		X4,  //Quad 7%
		X8,  //Eight 3%
		X16; //Jack 2%

		public static OUTCOME getRandom() {
			int x = new Random().nextInt(100)+1; //1-100
			if (isBetween(x, 1, 50)) return X0;
			else if (isBetween(x, 51, 75)) return X1;
			else if (isBetween(x, 76, 88)) return X2;
			else if (isBetween(x, 89, 95)) return X4;
			else if (isBetween(x, 96, 98)) return X8;
			else return X16; // if (isBetween(x, 99, 100))
		}

		private static boolean isBetween(int x, int lower, int upper) {
			return x >= lower && x <= upper;
		}
	}
}
