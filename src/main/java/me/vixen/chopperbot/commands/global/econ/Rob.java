package me.vixen.chopperbot.commands.global.econ;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import java.util.Random;

public class Rob {
	public void handle(SlashCommandEvent event, UserProfile profile) {
		if (profile.hasRobbed()) {
			event.getHook().editOriginal("You have already robbed today").setActionRows().queue();
			return;
		}

		final OUTCOME random = OUTCOME.getRandom();

		switch (random) {
			case FINED -> {
				profile.adjustCoins(-50);
				profile.update(event.getMember());
				event.getHook().editOriginal("You were caught by the State Police and fined 50 coins").setActionRows().queue();
			}
			case NOTHING -> event.getHook().editOriginal("You tried your best but came up empty handed").setActionRows().queue();
			case SUCCESS -> {
				final UserProfile unfortunateSoul =
					Database.getRandomProfile(event.getGuild(), event.getUser().getId());

				if (unfortunateSoul == null) {
					event.reply("You tried your best but came up empty handed").queue();
					return;
				}

				final int tenPercent = (int) Math.floor(unfortunateSoul.getCoins() * .10);

				unfortunateSoul.adjustCoins(-tenPercent);
				unfortunateSoul.update(null);
				profile.adjustCoins(tenPercent);
				profile.rob();
				profile.update(event.getMember());
				event.getHook().editOriginal("You stole " + tenPercent + " coins from " + unfortunateSoul.getNickname()).setActionRows().queue();
			}
		}
	}

	private enum OUTCOME {
		FINED,
		NOTHING,
		SUCCESS;

		public static OUTCOME getRandom() {
			int rand = new Random().nextInt(100)+1; //1-100
			if (isBetween(rand, 1, 25)) return FINED;
			if (isBetween(rand, 26, 75)) return NOTHING;
			else return SUCCESS; //if (isBetween(rand, 76, 100))
		}

		private static boolean isBetween(int x, int lower, int upper) {
			return x >= lower && x <= upper;
		}
	}
}
