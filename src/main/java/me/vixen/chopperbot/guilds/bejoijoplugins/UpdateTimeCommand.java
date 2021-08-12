package me.vixen.chopperbot.guilds.bejoijoplugins;

import me.vixen.chopperbot.commands.ICommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
			if (months >= 24) {
				//noinspection ConstantConditions cant be null
				newRole = event.getGuild().getRolesByName("2 years", true).get(0);
			} else if (months >= 12) {
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
			String search;
			if (months >= 24)
				search = "2 Years";
			else if (months >= 12)
				search = "1 Year";
			else
				search = months + " months";
			event.reply("I was looking for: " + search + " but couldn't find it").queue();
			return;
		}

		List<Role> newRoles = modifyRoles(event.getMember().getRoles(), newRole);
		event.getGuild().modifyMemberRoles(event.getMember(), newRoles).queue( v ->
			event.reply("Role: `" + newRole.getName() + "` added!")
		);
	}

	private List<Role> modifyRoles(List<Role> oldRoles, Role roleToAdd) {
		List<String> timeRoleIds = List.of(
			"873323152187461662", "696195867484356690", // 2Y, 1Yr
			"773668848792502302", "773668524127682612", // 11m, 10m
			"773668322318614619", "773668060316958741", // 9m, 8m
			"773667953898946600", "696195824719233034", // 7m, 6m
			"696195789038551110", "696195752975794176", // 5m, 4m
			"696195710634164256", "696195657093873674", // 3m, 2m
			"696195521240498197" // 1m
		);

		oldRoles.removeIf(r -> timeRoleIds.contains(r));
		oldRoles.add(roleToAdd);
		return oldRoles;
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("updatetime", "Update your time role");
	}
}
