package me.vixen.chopperbot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.global.gamble.Lotto;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
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
	private static DateTimeFormatter CUSTOMFORMAT = DateTimeFormatter.ofPattern("uuuu-MMM-dd @ HH:mm:ss OOOO");
	private static ZoneId ZONEID = ZoneId.of("America/Chicago");
	private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private static long SECONDSINDAY = TimeUnit.DAYS.toSeconds(1L);

	public static void startScheduling(List<Guild> botGuilds, GuildManager gManager) {
		fireChests(botGuilds); // Immediately spawn and schedule next (returns next schedule)
		scheduleResetsAtMidnight(botGuilds, gManager); // Schedule resets for midnight
	}

	private static void doResets(List<Guild> botGuilds, GuildManager gManager) {
		startResets(botGuilds, gManager);
		tryLotto();
		scheduleResetsAtMidnight(botGuilds, gManager);
	}

	private static void scheduleResetsAtMidnight(List<Guild> botGuilds, GuildManager gManager) {
		ScheduledFuture<?> resetFuture = executorService.schedule(
			() -> doResets(botGuilds, gManager),
			now().until(startOfNextDay(), ChronoUnit.SECONDS),
			TimeUnit.SECONDS
		);
		Logger.log("Reset Future is : " + getTimeOfScheduledFuture(resetFuture));
	}

	private static LocalDateTime now() {
		return LocalDateTime.now(ZONEID);
	}

	private static LocalDateTime startOfNextDay() {
		return LocalDate.now(ZONEID).plusDays(1L).atStartOfDay().plusSeconds(1L);
	}

	private static String getTimeOfScheduledFuture(ScheduledFuture<?> sf) {
		long delay = sf.getDelay(TimeUnit.MILLISECONDS);
		return OffsetDateTime.now(ZONEID)
			.plus(delay, ChronoUnit.MILLIS)
			.format(CUSTOMFORMAT);
	}

	private static void fireChests(List<Guild> botGuilds) {
		loadChests(botGuilds);
		rescheduleChest(botGuilds);
	}

	private static void rescheduleChest(List<Guild> botGuilds) {
		ScheduledFuture<?> chestFuture = executorService.schedule(
			() -> fireChests(botGuilds),
			(long) new Random().nextInt(3) + 4,
			TimeUnit.HOURS
		);
		Logger.log("Chests will spawn: " + getTimeOfScheduledFuture(chestFuture));
	}

	private static void startResets(List<Guild> botGuilds, GuildManager gManager) {
		for (Guild g : botGuilds) {
			if (gManager.contains(g)) {
				gManager.getGuild(g).doNightlyReset();
			} else DefaultEventHandler.nightlyReset(g);
		}
	}

	private static void tryLotto() {
		//Start lotto
		StringBuilder builder = new StringBuilder();
		for (int i=1; i<=5; i++) builder.append(new Random().nextInt(Lotto.UPPER)+1).append(", ");
		String winningBet = Database.getWinningBet(builder.toString().trim());
		if (winningBet != null)  {
			String userId = winningBet.split(",")[0];
			String guildId = winningBet.split(",")[1];
			UserProfile member = Database.getMember(Entry.getJDA().getGuildById(guildId), userId);
			member.adjustCoins(Database.getPot());
			member.update(null);
			Entry.getJDA().retrieveUserById(userId).queue(user -> {
				user.openPrivateChannel()
					.flatMap(pc -> pc.sendMessage("You won the lotto! " +
						"The coins have been added to the guild you placed the winning bet in!"))
					.queue();
			});
			Database.deleteAllBets();
			Database.setPot(0);
		}
	}

	static void loadChests(List<Guild> botGuilds) {
		for (Guild g : botGuilds) {
			Config config = Database.getConfig(g.getId());
			if (config == null || config.getChannels().isEmpty()) {
				DefaultEventHandler.getDefaultTreasureChannels(g);
				continue; //Skip to next guild
			}

			//Remove all but appointed channels and non-text channels
			List<GuildChannel> treasureChannels = g.getChannels().stream()
				.filter(it -> Scheduling.shouldInclude(it, config) && it.getType().equals(ChannelType.TEXT))
				.collect(Collectors.toList());
			makeTreasureChest(treasureChannels);
		}
	}

	public static boolean shouldInclude(GuildChannel gc, Config c) {
		Config.TreasureMode mode = c.getMode();
		if (mode.equals(Config.TreasureMode.BLACKLIST))
			return !c.getChannels().contains(gc.getId());
		else if (mode.equals(Config.TreasureMode.WHITELIST))
			return c.getChannels().contains(gc.getId());
		else return false;
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