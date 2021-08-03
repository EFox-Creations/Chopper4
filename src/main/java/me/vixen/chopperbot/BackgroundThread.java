package me.vixen.chopperbot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.DBMember;
import me.vixen.chopperbot.database.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.awt.*;
import java.util.Random;
//import me.vixen.chopperbot.commands.global.LottoGroup;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.guilds.IGuild;
import me.vixen.chopperbot.listener.DefaultEventHandler;
//import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
//import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

//import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
//import java.util.Random;

public class BackgroundThread {
	private static final Color TREASURE_COLOR = new Color(0,143,186);
	private static boolean reset = true;

	@SuppressWarnings("LoopConditionNotUpdatedInsideLoop") //This needs to run forever in the background
	public static void go(boolean runForever, GuildManager gManager, EventWaiter waiter) {
		System.out.println("BG Thread Started");
		int currentHour = OffsetDateTime.now().getHour();
		int hourToSpawn = currentHour;
		do {
			if (currentHour == 0 && reset) { //If Midnight
				/*
				//Start lotto
				StringBuilder builder = new StringBuilder();
				for (int i=1; i<=5; i++) builder.append(new Random().nextInt(LottoGroup.UPPER)+1).append(", ");
				String winningBet = Database.getWinningBet(builder.toString().trim());
				if (winningBet == null) {
					MessageEmbed lottoEmbed = new EmbedBuilder()
						.setColor(Color.YELLOW)
						.setTitle("😢 No one won the lotto today")
						.addField("🎲 Today's Draw 🎲", builder.toString().trim(), false)
						.build();
					sendLottoEmbed(gManager, lottoEmbed);
				} else {
					Entry.jda.retrieveUserById(winningBet).queue(user -> {
						MessageEmbed lottoEmbed1 = new EmbedBuilder()
							.setColor(Color.GREEN)
							.setTitle("Today's Lotto Winner!")
							.addField("🎲 Today's Draw 🎲", builder.toString().trim(), false)
							.addField("Winner", user.getAsTag(), false)
							.build();
						sendLottoEmbed(gManager, lottoEmbed1);

					});
				}
				//END LOTTO
				*/ //Temporarily removed
				//Start Resets
				for (Guild g : Entry.jda.getGuilds()) {
					if (gManager.contains(g)) {
						gManager.getGuild(g).doNightlyReset();
					} else DefaultEventHandler.nightlyReset(g);
				}
				reset = false;
				//END RESETS
			} else {
				if (currentHour == hourToSpawn) {
					for (Guild g : Entry.jda.getGuilds()) {
						if (gManager.contains(g)) {
							final IGuild ig = gManager.getGuild(g);
							makeTreasureChest(ig.getTreasureChannels(), waiter);
						} else {
							final List<TextChannel> defaultTreasureChannels = DefaultEventHandler.getDefaultTreasureChannels(g);
							makeTreasureChest(defaultTreasureChannels, waiter);
						}
					}
					hourToSpawn = OffsetDateTime.now().plusHours(new Random().nextInt(3)+4).getHour();
				} else reset = true;
			}
		} while (runForever);
	}

	/*
	private static void sendLottoEmbed(GuildManager gManager, MessageEmbed lotto) {
		Database.deleteAllBets();
		for (Guild g : Entry.jda.getGuilds()) {
			if (gManager.contains(g))
				gManager.getGuild(g).getLottoChannel().sendMessageEmbeds(lotto).queue();
			else {
				TextChannel systemChannel = g.getSystemChannel();
				if (systemChannel == null) {
					TextChannel defaultChannel = g.getDefaultChannel();
					if (defaultChannel != null)
						defaultChannel.sendMessageEmbeds(lotto).queue();
				} else systemChannel.sendMessageEmbeds(lotto).queue();
			}
		}
	}
	*/

	private enum ChestRewardsEnum {
		Empty,
		ForgottenTreasure,
		Spoils,
		Fortune,
		Motherload,
		FoxbeardsHorde;

		private static int getValue(ChestRewardsEnum reward) {
			return switch (reward) {
				case Empty -> 0;
				case ForgottenTreasure -> new Random().nextInt(4) + 1;
				case Spoils -> new Random().nextInt(4) + 6;
				case Fortune -> new Random().nextInt(4) + 11;
				case Motherload -> new Random().nextInt(4) + 16;
				case FoxbeardsHorde -> new Random().nextInt(4) + 21;
			};
		}

		private static ChestRewardsEnum getRandom() {
			int rand = new Random().nextInt(100)+1;
			if (isBetween(rand, 0, 10)) return Empty; //10%
			if (isBetween(rand, 11, 30)) return ForgottenTreasure; //20%
			if (isBetween(rand, 31, 60)) return Spoils; //30%
			if (isBetween(rand, 61, 85)) return Fortune; //25%
			if (isBetween(rand, 86, 95)) return Motherload; //10%
			if (isBetween(rand, 95, 100)) return FoxbeardsHorde; //5%
			else return getRandom();
		}

		private static boolean isBetween(int x, int lower, int upper) {
			return lower <= x && x <= upper;
		}

		private static String getName(ChestRewardsEnum reward) {
			return switch (reward) {
				case Empty -> "Empty";
				case ForgottenTreasure -> "Forgotten Treasure";
				case Spoils -> "Spoils";
				case Fortune -> "Fortune";
				case Motherload -> "Motherload";
				case FoxbeardsHorde -> "Foxbeard's Horde";
			};
		}
	} //End Enum

	public static void makeTreasureChest(List<TextChannel> availableChannels, EventWaiter waiter) {
		if (availableChannels.isEmpty()) return;
		final TextChannel targetChannel = availableChannels.get(new Random().nextInt(availableChannels.size()));
		final ChestRewardsEnum rewardName = ChestRewardsEnum.getRandom();
		targetChannel.sendMessageEmbeds(
			new EmbedBuilder()
				.setColor(TREASURE_COLOR)
				.setTitle("🏝 A safe has washed ashore!")
				.setDescription("React with 🔑 to pick the lock!")
				.build()
		).queue(message ->
			waiter.waitForEvent(
				GuildMessageReactionAddEvent.class,
				(e) -> e.getMessageId().equals(message.getId()) && e.getReactionEmote().getName().equals("🔑"),
				(e) -> rewardUser(e, rewardName)
			)
		);
	}

	private static void rewardUser(GuildMessageReactionAddEvent event, ChestRewardsEnum reward) {
		int value = ChestRewardsEnum.getValue(reward);
		event.retrieveMember().queue(member -> {
			final DBMember dbMember = Database.getMember(member.getGuild(), member.getUser().getId());
			if (dbMember == null) return; //something fucky happened
			final int skill = dbMember.getSkill();
			final int rand = skill < 10 ? new Random().nextInt(10)+1 : new Random().nextInt(100)+1;
			boolean opened = skill > rand;
			dbMember.adjustSkill(opened ? 1 : 2);
			dbMember.adjustCoins(opened ? value : 0);
			dbMember.update();

			if (opened) {
				event.retrieveMessage().queue(message -> {
					message.clearReactions().queue();
					message.editMessageEmbeds(
						new EmbedBuilder()
							.setTitle(ChestRewardsEnum.getName(reward))
							.setColor(TREASURE_COLOR)
							.setDescription(
								String.format("You found %d coins!", value)
							)
							.setFooter("Skill increased by 2!")
							.build()
					).queue();
				});
			} else {
				event.retrieveMessage().queue(message -> {
					message.clearReactions().queue();
					message.editMessageEmbeds(
						new EmbedBuilder()
							.setTitle("⛔ The lock broke!")
							.setColor(Color.RED)
							.setFooter("Skill increased by 1 anyway!")
							.build()
					).queue();
				});
			}
		});
	}
}