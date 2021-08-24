package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import java.util.Random;

public class RobCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event, UserProfile profile) {
		if (profile.hasRobbed()) {
			event.reply("You have already robbed today").setEphemeral(true).queue();
			return;
		}

		final OUTCOME random = OUTCOME.getRandom();

		switch (random) {
			case FINED -> {
				profile.adjustCoins(-50);
				profile.update(event.getMember());
				event.reply("You were caught by the State Police and fined 50 coins").queue();
			}
			case NOTHING -> event.reply("You tried your best but came up empty handed").queue();
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
				event.reply("You stole " + tenPercent + " coins from " + unfortunateSoul.getNickname()).queue();
			}
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("rob", "Attempt to rob a random person");
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
