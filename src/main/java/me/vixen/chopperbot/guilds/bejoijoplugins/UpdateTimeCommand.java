package me.vixen.chopperbot.guilds.bejoijoplugins;

import me.vixen.chopperbot.commands.ICommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateTimeCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		final OffsetDateTime timeJoined = event.getMember().getTimeJoined();
		final OffsetDateTime now = OffsetDateTime.now();

		final long months = ChronoUnit.MONTHS.between(timeJoined, now);

		final List<Role> monthRoles = event.getMember().getRoles().stream()
			.filter(role -> role.getName().contains("month")).collect(Collectors.toList());

		for (Role r : monthRoles) //noinspection ConstantConditions
			event.getGuild().removeRoleFromMember(event.getMember(), r).queue();

		Role newRole;
		try {
			if (months >= 12) {
				//noinspection ConstantConditions cant be null
				newRole = event.getGuild().getRolesByName("1 year", true).get(0);
			} else if (months == 1) {
				//noinspection ConstantConditions cant be null
				newRole = event.getGuild().getRolesByName("1 month", true).get(0);
			} else {
				//noinspection ConstantConditions cant be null
				newRole = event.getGuild().getRolesByName(months + " months", true).get(0);
			}
		} catch (IndexOutOfBoundsException | NullPointerException e) {
			event.reply("I was looking for: " + (months >= 12 ? "1 Year" : months + " months") + " but couldn't find it").queue();
			return;
		}
		event.getGuild().addRoleToMember(event.getMember(), newRole).queue(
			success -> event.reply("Role " + newRole.getName() + " added!").queue(),
			failure -> event.reply("Could not add Role").queue());
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("updatetime", "Update your time role");
	}
}
