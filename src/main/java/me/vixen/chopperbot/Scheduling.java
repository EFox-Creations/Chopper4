package me.vixen.chopperbot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.guilds.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Random;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.guilds.IGuild;
import me.vixen.chopperbot.listener.DefaultEventHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.Button;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Scheduling {

	private static DateTimeFormatter BASICDATEFORMAT = DateTimeFormatter.RFC_1123_DATE_TIME;
	private static DateTimeFormatter CUSTOMFORMAT = DateTimeFormatter.ofPattern("uuuu-MMM-dd @ HH:mm:ss OOOOO");
	private static ZoneId ZONEID = ZoneId.of("America/Chicago");
	private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private static long SECONDSINDAY = TimeUnit.DAYS.toSeconds(1L);

	public static void startScheduling(GuildManager gManager) {
		ScheduledFuture<?> chestsFuture = fireChests(); // Immediately spawn and schedule next (returns next schedule)
		ScheduledFuture<?> resetFuture = executorService.scheduleWithFixedDelay(
			() -> startResets(gManager), //Reset daily claims in database
			now().until(startOfNextDay(), ChronoUnit.SECONDS), // Delay reset until start of next day
			SECONDSINDAY, //After first delay, delay for 1 whole day //TODO reschedule this for start of next day everytime? DST?
			TimeUnit.SECONDS
		);
		Logger.log("Reset Future is : " + getTimeOfScheduledFuture(chestsFuture));
	}

	private static LocalDateTime now() {
		return LocalDateTime.now(ZONEID);
	}

	private static LocalDateTime startOfNextDay() {
		return LocalDate.now(ZONEID).plusDays(1L).atStartOfDay();
	}

	private static String getTimeOfScheduledFuture(ScheduledFuture<?> sf) {
		long delay = sf.getDelay(TimeUnit.MILLISECONDS);
		return OffsetDateTime.now(ZONEID)
			.plus(delay, ChronoUnit.MILLIS)
			.format(CUSTOMFORMAT);
	}

	private static ScheduledFuture<?> fireChests() {
		loadChests();
		return rescheduleChest();
	}

	private static ScheduledFuture<?> rescheduleChest() {
		ScheduledFuture<?> chestFuture = executorService.schedule(
			() -> fireChests(),
			(long) new Random().nextInt(3) + 4,
			TimeUnit.HOURS
		);
		Logger.log("Chests will spawn: " + getTimeOfScheduledFuture(chestFuture));
		return chestFuture;
	}

	private static void startResets(GuildManager gManager) {
		for (Guild g : Entry.jda.getGuilds()) {
			if (gManager.contains(g)) {
				gManager.getGuild(g).doNightlyReset();
			} else DefaultEventHandler.nightlyReset(g);
		}
	}


	/* TEMPORARILY REMOVED
	private static void startLotto() {
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
	}

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
	TEMPORARILY REMOVED */

	public static boolean shouldInclude(GuildChannel gc, Config c) {
		Config.TreasureMode mode = c.getMode();
		if (mode.equals(Config.TreasureMode.BLACKLIST))
			return !c.getChannels().contains(gc.getId());
		else if (mode.equals(Config.TreasureMode.WHITELIST))
			return c.getChannels().contains(gc.getId());
		else return false;
	}

	static void loadChests() {
		for (Guild g : Entry.jda.getGuilds()) {
			Config config = Database.getConfig(g.getId());
			if (config == null || config.getChannels().isEmpty()) {
				DefaultEventHandler.getDefaultTreasureChannels(g);
				continue; //Skip to next guild
			}

			List<GuildChannel> treasureChannels = g.getChannels()
				.stream()
				.filter(it -> Scheduling.shouldInclude(it, config)) //Remove all but appointed channels
				.filter(it -> it.getType().equals(ChannelType.TEXT)) //Remove non-text channels
				.collect(Collectors.toList());
			makeTreasureChest(treasureChannels);
		}
	}

	public static void makeTreasureChest(List<GuildChannel> availableChannels) {
		if (availableChannels.isEmpty()) return;
		final TextChannel targetChannel;
		GuildChannel selectedChannel = availableChannels.get(new Random().nextInt(availableChannels.size()));
		try {
			targetChannel = (TextChannel) selectedChannel;
		} catch (ClassCastException e) {
			Logger.log("Could not create treasure in " + selectedChannel.getName()
				+ "::" + selectedChannel.getGuild().getName(), e);
			return;
		}
		targetChannel.sendMessageEmbeds(
			new EmbedBuilder()
				.setColor(new Color(0,143,186))
				.setTitle("🏝 A safe has washed ashore!")
				.setDescription("Click 🔑 to pick the lock!")
				.build()
		).setActionRow(Button.primary("treasureclaim", "Claim").withEmoji(Emoji.fromUnicode("🔑"))).queue();
	}
}