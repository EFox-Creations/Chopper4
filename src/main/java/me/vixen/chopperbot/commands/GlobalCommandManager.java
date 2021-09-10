package me.vixen.chopperbot.commands;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.global.*;
import me.vixen.chopperbot.commands.global.DonateCommand;
import me.vixen.chopperbot.commands.global.econ.EconCommand;
import me.vixen.chopperbot.commands.global.gamble.GambleCommand;
import me.vixen.chopperbot.commands.global.gamble.LottoGroup;
import me.vixen.chopperbot.commands.global.userprofile.ProfileCommand;
import me.vixen.chopperbot.guilds.GuildManager;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.List;

public class GlobalCommandManager {

	public List<ICommand> globalCommands = new ArrayList<>();

	public GlobalCommandManager(EventWaiter waiter, GuildManager guildManager) {

		//Admin Commands
		addGlobalCommand(new ResetCommand(guildManager));
		addGlobalCommand(new SummonCommand(waiter, guildManager));

		//Global Commands
		addGlobalCommand(new ConfigCommand());
		addGlobalCommand(new CustomCommand());
		addGlobalCommand(new EconCommand(waiter, guildManager));
		addGlobalCommand(new EmbedSendCommand(waiter));
		addGlobalCommand(new CustomGroup(this, waiter));
		addGlobalCommand(new DonateCommand());
		addGlobalCommand(new EchoCommand());
		addGlobalCommand(new GambleCommand(waiter));
		addGlobalCommand(new HelpCommand());
		addGlobalCommand(new LottoGroup());
		addGlobalCommand(new ModGroup()); //houses moderation commands
		addGlobalCommand(new PollCommand());;
		addGlobalCommand(new ProfileCommand(waiter));
		addGlobalCommand(new ReportCommand(waiter));
		addGlobalCommand(new ShopCommand(waiter));
		addGlobalCommand(new StickyGroup());
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
