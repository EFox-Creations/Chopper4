package me.vixen.chopperbot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICommand {
	void handle(SlashCommandEvent event);

	default String getName() {
		return getCommandData().getName();
	}

	CommandData getCommandData();
}