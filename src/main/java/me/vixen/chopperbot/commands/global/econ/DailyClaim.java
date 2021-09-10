package me.vixen.chopperbot.commands.global.econ;

import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Random;

public class DailyClaim {
	public GuildManager gManager;

	public DailyClaim(GuildManager gManager) {
		this.gManager = gManager;
	}

	public void handle(SlashCommandEvent event, UserProfile profile) {
		Guild guild = event.getGuild();
		if (gManager.contains(guild) && gManager.getGuild(guild).hasCustomClaims()) {
			gManager.getGuild(guild).getCustomClaim(event);
		} else { //Default claiming
			if (profile == null) {
				event.getHook().editOriginal("An error occurred; aborting with Code " + Errors.DBNULLRETURN).setActionRows().queue();
				return;
			}
			int dailyChests = profile.getChestCount();
			if (dailyChests == 0) {
				event.getHook().editOriginalEmbeds(Embeds.getAlreadyClaimed()).setContent("").setActionRows().queue();
				return;
			}

			EmbedBuilder builder = new EmbedBuilder()
				.setColor(Embeds.Colors.FOXORANGE.get())
				.setTitle("Daily Chests: " + dailyChests);
			for (int i=1; i <= dailyChests; i++) {
				MessageEmbed.Field reward = getReward(profile);
				builder.addField(reward);
				//noinspection ConstantConditions
				if (reward.getName().equals("Another Chest!")) i--;
			}
			profile.setChestCount(0);
			profile.update(null);
			event.getHook().editOriginalEmbeds(builder.build()).setContent(event.getMember().getAsMention()).setActionRows().queue();
		}
	}

	private MessageEmbed.Field getReward(UserProfile dbMember) {
		final REWARDS random = REWARDS.getRandom();

		String title = "";
		String description = "";
		switch (random) {
			case COINS -> {
				int coins = new Random().nextInt(20) + 1;
				title = "Coins!";
				description = "You won " + coins + " coins!";
				dbMember.adjustCoins(coins);
			}
			case EXP -> {
				int exp = new Random().nextInt(190) + 10;
				title = "EXP!";
				description = "You won " + exp + " exp!";
				dbMember.adjustExp(exp);
			}
			case LOCK -> {
				title = "Practice Lock!";
				description = "You won a practice lock! Use /practice to use it";
				dbMember.adjustLockCount(1);
			}
			case CHEST -> {
				title = "Another Chest!";
				description = "You won a reroll! A chest has been added to your inventory";
				dbMember.incrementChestCount();
			}
		}

		return new MessageEmbed.Field(title, description, false);
	}

	private enum REWARDS {
		COINS, // 40%
		EXP,   // 40%
		LOCK,  // 10%
		CHEST, // 8%
		ROLE_VOUCHER, // 1%
		COLOR_VOUCHER; // 1%

		public static REWARDS getRandom() {
			int rand = new Random().nextInt(100)+1; //1-100
			if (isBetween(rand, 1, 40)) return COINS;
			else if (isBetween(rand, 41, 80)) return EXP;
			else if (isBetween(rand, 81,90)) return LOCK;
			else if (isBetween(rand, 91, 98)) return CHEST;
			else if (isBetween(rand, 99, 99)) return ROLE_VOUCHER;
			else return COLOR_VOUCHER; //(isBetween(rand, 100, 100))
		}

		private static boolean isBetween(int x, int lower, int upper) {
			return x >= lower && x <= upper;
		}
	}
}
