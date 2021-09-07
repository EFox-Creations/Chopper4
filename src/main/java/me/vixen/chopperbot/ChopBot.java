package me.vixen.chopperbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.DatabaseHandler;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.guilds.CustomGuild;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.listener.Listener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ChopBot {

	public enum TOKEN {
		CHOPPER,
		TESTBOT
	}


	private static ChopBot bot = null;
	private static JDA jda = null;
	private static DatabaseHandler dbHandler;
	private static String CREATORID;

	@Nonnull
	public static ChopBot createNew(TOKEN token) throws ExceptionInInitializerError, FileNotFoundException, LoginException, InterruptedException {
		if (bot == null) {
			bot = new ChopBot(token);
			return bot;
		} else throw new ExceptionInInitializerError("There is already a bot class created!");

	}

	private ChopBot(TOKEN token) throws FileNotFoundException, LoginException, InterruptedException {
		ChopConfig config = ChopConfig.load();
		if (config == null) {
			Logger.log("Config failed to load, exiting...");;
			throw new FileNotFoundException("Config Missing");
		}
		CREATORID = config.getCreatorId();

		String tokenToUse = "";
		switch (token) {
			case CHOPPER -> tokenToUse = config.getChopToken();
			case TESTBOT -> tokenToUse = config.getTestToken();
		}

		//INIT STARTUP VARS
		EnumSet<GatewayIntent> enabledIntents = EnumSet.of(
			GatewayIntent.GUILD_MESSAGES,
			GatewayIntent.GUILD_BANS,
			GatewayIntent.GUILD_MEMBERS,
			GatewayIntent.GUILD_MESSAGE_REACTIONS,
			GatewayIntent.GUILD_EMOJIS,
			GatewayIntent.GUILD_VOICE_STATES
		);

		EventWaiter waiter = new EventWaiter();
		GuildManager guildManager = new GuildManager(waiter);
		GlobalCommandManager commandManager = new GlobalCommandManager(waiter, guildManager);

		jda = JDABuilder.createDefault(tokenToUse, enabledIntents)
			.addEventListeners(waiter, new Listener(waiter, commandManager, guildManager))
			.setActivity(Activity.listening("/help"))
			.enableCache(CacheFlag.VOICE_STATE)
			.setMemberCachePolicy(MemberCachePolicy.VOICE)
			.setChunkingFilter(ChunkingFilter.NONE)
			.setStatus(OnlineStatus.ONLINE).build().awaitReady();

		dbHandler = new DatabaseHandler(jda);

		//DeleteAllLocalCommands
		//for (Guild g : jda.getGuilds()) g.updateCommands().queue(unused -> System.out.println("Commands Cleared"));

		//DeleteAllGlobalCommands
		//jda.updateCommands().queue();

		//Load local Commands
		for (CustomGuild g : guildManager.getGuilds()) { //Local first
			final Guild guild = jda.getGuildById(g.getId());
			if (guild == null) continue;
			List<CommandData> data = new ArrayList<>();
			List<ICommand> localCommands = g.getLocalCommands();
			if (localCommands == null || localCommands.isEmpty()) {
				Logger.log(g.getName() + " has no local commands");
				continue;
			}
			for (ICommand c : localCommands) data.add(c.getCommandData());
			guild.updateCommands().addCommands(data).queue();
		}
		//Then Global (Note: Takes up to 1 hour to update)
		jda.updateCommands().addCommands(commandManager.getAllGlobalCommandData()).queue();

		Logger.log("JDA for Chopper4 fully loaded; globals awaiting update on discord (up to 1hr response time)");

		//Start scheduling
		Scheduling.startScheduling(jda.getGuilds(), guildManager);
	}

	@Nullable
	public ChopBot getBot() {
		return this.bot;
	}

	@Nullable
	public JDA getJda() {
		return jda;
	}

	@Nullable
	public DatabaseHandler getDbHandler() {
		return dbHandler;
	}

	public static String getCreatorId() {
		return CREATORID;
	}

	public static JDA getJDA() {
		return jda;
	}
}
