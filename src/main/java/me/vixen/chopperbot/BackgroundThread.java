package me.vixen.chopperbot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.DBMember;
import me.vixen.chopperbot.database.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.awt.*;
import java.util.Random;
//import me.vixen.chopperbot.commands.global.LottoGroup;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.guilds.IGuild;
import me.vixen.chopperbot.listener.DefaultEventHandler;
//import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.Button;
//import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

//import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
//import java.util.Random;

public class BackgroundThread {
	private static boolean reset = true;

	@SuppressWarnings("LoopConditionNotUpdatedInsideLoop") //This needs to run forever in the background
	public static void go(boolean runForever, GuildManager gManager, EventWaiter waiter) {
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
				reset = true;
				if (currentHour == hourToSpawn) {
					for (Guild g : Entry.jda.getGuilds()) {
						if (gManager.contains(g)) {
							final IGuild ig = gManager.getGuild(g);
							makeTreasureChest(ig.getTreasureChannels());
						} else {
							final List<TextChannel> defaultTreasureChannels = DefaultEventHandler.getDefaultTreasureChannels(g);
							makeTreasureChest(defaultTreasureChannels);
						}
					}
					hourToSpawn = OffsetDateTime.now().plusHours(new Random().nextInt(3)+4).getHour();
				}
			}
			currentHour = OffsetDateTime.now().getHour();
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

	public static void makeTreasureChest(List<TextChannel> availableChannels) {
		if (availableChannels.isEmpty()) return;
		final TextChannel targetChannel = availableChannels.get(new Random().nextInt(availableChannels.size()));
		targetChannel.sendMessageEmbeds(
			new EmbedBuilder()
				.setColor(new Color(0,143,186))
				.setTitle("🏝 A safe has washed ashore!")
				.setDescription("React with 🔑 to pick the lock!")
				.build()
		).setActionRow(Button.primary("treasureclaim", "Claim").withEmoji(Emoji.fromUnicode("🔑"))).queue();
	}
}