package me.vixen.chopperbot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.DatabaseHandler;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.guilds.IGuild;
import me.vixen.chopperbot.listener.Listener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class Entry {
	public static JDA jda;
	public static DatabaseHandler dbHandler;
	public static String CREATOR_ID;

	public static void main(String[] args) throws IOException, LoginException, InterruptedException {
		ChopConfig config = ChopConfig.load();
		if (config == null) {
			Logger.log("Config failed to load, exiting...");
			System.exit(1);
			return;
		}

		String token = config.getToken();
		CREATOR_ID = config.getCreatorId();

		//INIT STARTUP VARS
		EnumSet<GatewayIntent> enabledIntents = EnumSet.of(
			GatewayIntent.GUILD_MESSAGES,
			GatewayIntent.GUILD_BANS,
			GatewayIntent.GUILD_MEMBERS,
			GatewayIntent.GUILD_MESSAGE_REACTIONS,
			GatewayIntent.GUILD_EMOJIS
		);

		EventWaiter waiter = new EventWaiter();
		GuildManager guildManager = new GuildManager(waiter);
		GlobalCommandManager commandManager = new GlobalCommandManager(waiter, guildManager);
		dbHandler = new DatabaseHandler();

		jda = JDABuilder.createDefault(token, enabledIntents)
			.addEventListeners(waiter, new Listener(waiter, commandManager, guildManager))
			.setActivity(Activity.listening("/help"))
			.setChunkingFilter(ChunkingFilter.NONE)
			.disableIntents(GatewayIntent.GUILD_VOICE_STATES)
			.setStatus(OnlineStatus.ONLINE).build().awaitReady();
		Database.initDatabase(jda.getGuilds());

		Logger.log("DatabaseLoaded");

		//DeleteAllLocalCommands
		//for (Guild g : jda.getGuilds()) g.updateCommands().queue(unused -> System.out.println("Commands Cleared"));

		//DeleteAllGlobalCommands
		//jda.updateCommands().queue();

		//Load local Commands
		for (IGuild g : guildManager.getGuilds()) { //Local first
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
		//Start treasure thread
		Thread thread1 = new Thread(() -> BackgroundThread.go(true, guildManager, waiter));
		thread1.setName("Background Thread");
		thread1.start();
	}
}
