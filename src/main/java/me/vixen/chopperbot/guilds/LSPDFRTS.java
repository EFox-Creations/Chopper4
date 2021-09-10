package me.vixen.chopperbot.guilds;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.CustomGuild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LSPDFRTS extends CustomGuild {

	public LSPDFRTS(String guildId, EventWaiter waiter) {
		super(guildId, waiter);
	}

	@Override
	protected void setLocalCommands(EventWaiter waiter) {
		localCommands = EMPTY_COMMANDS;
	}

	@Override
	public boolean hasCustomClaims() {
		return false;
	}

	@Override
	public void getCustomClaim(SlashCommandEvent event) {
		return;
	}
}
