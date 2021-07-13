package me.vixen.chopperbot.commands;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.global.*;
import me.vixen.chopperbot.guilds.GuildManager;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.List;

public class GlobalCommandManager {

	public List<ICommand> globalCommands = new ArrayList<>();

	public GlobalCommandManager(EventWaiter waiter, GuildManager guildManager) {

		//Admin Commands
		addGlobalCommand(new ChangelogCommand());
		addGlobalCommand(new EmoteIdCommand());
		addGlobalCommand(new InjectionCommand());
		addGlobalCommand(new QueryCommand());
		addGlobalCommand(new SummonCommand(waiter, guildManager));

		//Global Commands
		addGlobalCommand(new AvatarCommand());
		addGlobalCommand(new ConfigCommand());
		addGlobalCommand(new CustomCommand());
		addGlobalCommand(new CustomGroup(this, waiter));
		addGlobalCommand(new DailyClaimCommand(guildManager));
		addGlobalCommand(new DonateCommand());
		addGlobalCommand(new EchoCommand());
		addGlobalCommand(new HelpCommand());

		addGlobalCommand(new LeaderboardCommand(waiter));
		addGlobalCommand(new LottoGroup());
		addGlobalCommand(new ModGroup()); //houses moderation commands
		addGlobalCommand(new OddsCommand()); //displays GOC odds
		addGlobalCommand(new PollCommand());
		addGlobalCommand(new PracticeCommand());
		addGlobalCommand(new ProfileCommand());
		addGlobalCommand(new ReportCommand(waiter));
		addGlobalCommand(new RobCommand());
		addGlobalCommand(new ScratchOffCommand());
		addGlobalCommand(new ShopCommand(waiter));
		addGlobalCommand(new SlotCommand());
		addGlobalCommand(new StickyGroup());
		addGlobalCommand(new ToggleLvlMsgsCommand());
		addGlobalCommand(new UserInfoCommand()); //whois
		addGlobalCommand(new WarningGroup(waiter));
	}

	private void addGlobalCommand(ICommand cmd) {
		boolean nameFound = globalCommands.stream().anyMatch(it -> it.getName().equalsIgnoreCase(cmd.getName()));
		if (nameFound) throw new IllegalArgumentException("A Command with this name already exists: " + cmd.getName());
		globalCommands.add(cmd);
	}


	public ICommand getGlobalCommand(String commandName) {
		String searchLower = commandName.toLowerCase();
		for (ICommand cmd : globalCommands) {
			if (cmd.getName().equalsIgnoreCase(searchLower)) return cmd;
		}
		return null;
	}

	public List<CommandData> getAllGlobalCommandData() {
		List<CommandData> cmdData = new ArrayList<>();
		for (ICommand c : globalCommands) {
			cmdData.add(c.getCommandData());
		}
		return cmdData;
	}
}
