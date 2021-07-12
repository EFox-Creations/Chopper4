package me.vixen.chopperbot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.Database.DatabaseHandler;
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
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;

public class Entry {
	public static JDA jda;
	public static DatabaseHandler dbHandler;
	public static final String CREATOR_ID = "354682693918785549";

	public static void main(String[] args) throws IOException, LoginException, InterruptedException {
		BufferedReader reader = new BufferedReader(new FileReader("token.txt"));
		final String token = reader.lines().findFirst().orElse("");
		reader.close();

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
			.setActivity(Activity.listening("/Help"))
			.disableIntents(GatewayIntent.GUILD_VOICE_STATES)
			.setStatus(OnlineStatus.ONLINE).build().awaitReady();

		Database.initDatabase(jda.getGuilds());

		System.out.println("DatabaseLoaded");

		//DeleteAllLocalCommands
		//for (Guild g : JDA.getGuilds()) g.updateCommands().queue(unused -> System.out.println("Commands Cleared"));

		//DeleteAllGlobalCommands
		//JDA.updateCommands().queue();

		//Load local Commands
		for (IGuild g : guildManager.getGuilds()) { //Local first
			final Guild guild = jda.getGuildById(g.getId());
			final CommandListUpdateAction commands = guild.updateCommands();
			for (ICommand c : g.getLocalCommands()) commands.addCommands(c.getCommandData());
			commands.queue();
		}
		//Then Global (Note: Takes up to 1 hour to update)
		jda.updateCommands().addCommands(commandManager.getAllGlobalCommandData()).queue();

		System.out.println("JDA for Chopper4 fully loaded; globals awaiting update on discord (up to 1hr response time)");
		//Start treasure thread
		Thread thread1 = new Thread(() -> BackgroundThread.go(true, guildManager, waiter));
		thread1.setName("Background Thread");
		//TODO thread1.start();
	}
}
