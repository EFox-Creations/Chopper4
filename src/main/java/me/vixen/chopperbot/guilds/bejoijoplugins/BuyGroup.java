package me.vixen.chopperbot.guilds.bejoijoplugins;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BuyGroup implements ICommand {

	public static java.util.List<String> colorRoles = List.of(
		"Blue", "Teal", "White", "Yellow", "Green", "Lime", "Gold", "Black"
	);

	EventWaiter waiter;
	public BuyGroup(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		final String name = event.getSubcommandName();
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		switch (name) {
			case "color" -> {
				final OptionMapping number = event.getOption("number");
				event.deferReply().queue();

				if (number == null) showColorMenu(event);
				else { // buy the role
					int index = (((int) number.getAsLong()) - 1);
					try {
						final String colorRole = colorRoles.get(index);
						if (dbMember.getCoins() <= BejoIjoPlugins.COLOR_COST ) {
							event.getHook().editOriginalEmbeds(Embeds.getInsufficientCoins()).queue();
							return;
						}

						final Role role = event.getGuild().getRolesByName(colorRole, true).get(0);
						event.getGuild().addRoleToMember(event.getMember(), role).queue((unused -> {
							dbMember.adjustCoins(BejoIjoPlugins.COLOR_COST  * -1);
							event.getHook().editOriginal("Role " + colorRole + " added successfully").queue();
							dbMember.update();
						}));

					} catch (IndexOutOfBoundsException e) {
						event.getHook().editOriginal("Invalid entry").queue();
					}
				}
			}
			case "role" -> {
				final OptionMapping option = event.getOption("desiredrole");
				if (option == null) showRoleMenu(event);
				if (dbMember.getCoins() <= BejoIjoPlugins.ROLE_COST ) {
					event.getHook().editOriginalEmbeds(Embeds.getInsufficientCoins()).queue();
					return;
				}
				else if (option !=null) {
					if (getAllUnownedRoles(event).contains(option.getAsRole())) {
						event.getGuild().addRoleToMember(event.getMember(), option.getAsRole()).queue();
						dbMember.adjustCoins(BejoIjoPlugins.ROLE_COST * -1);
						event.reply("Bought `" + option.getAsRole().getName() + "` for " + BejoIjoPlugins.ROLE_COST + " coins").queue();
					} else event.reply("Invalid Role Selection. This is either not a vanity role or you already own it").setEphemeral(true).queue();
				} else  event.reply("Unknown Error, Please report to bot admin").queue();
			}
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("buy", "Access the Chop Shop").addSubcommands(
			new SubcommandData("color", "Buy a colored vanity role")
				.addOption(OptionType.STRING, "number", "The number from the menu. (Leave blank to show menu)"),
			new SubcommandData("role", "Buy a vanity role")
				.addOption(OptionType.ROLE, "desiredrole", "The role you want (Leave blank to show menu)")
		);
	}

	public void showColorMenu(SlashCommandEvent event) {
		String[] roles = colorRoles.toArray(new String[colorRoles.size()]);
		Paginator pager = new Paginator.Builder()
			.addItems(roles)
			.setText(event.getUser().getAsMention() +
				"\nUse Command \"buycolor\" to buy a role!\n" +
				"Each role cost " + BejoIjoPlugins.COLOR_COST +" coins!" +
				"\n⚠ WARNING colors reset at 12:00am (Midnight) CentralUS⚠")
			.setEventWaiter(waiter)
			.setTimeout(20, TimeUnit.SECONDS)
			.setBulkSkipNumber(5)
			.allowTextInput(false)
			.setColor(Color.CYAN)
			.setColumns(1)
			.setItemsPerPage(10)
			.setLeftRightText("Left", "Right")
			.showPageNumbers(true)
			.waitOnSinglePage(true)
			.useNumberedItems(true)
			.wrapPageEnds(true)
			.addUsers(event.getUser())
			.build();
		pager.paginate(event.getTextChannel(), 1);
		event.getHook().editOriginal("Menu Displayed").queue();
	}
	private void showRoleMenu(SlashCommandEvent event) {
		final List<Role> allUnownedRoles = getAllUnownedRoles(event);
		if (allUnownedRoles.isEmpty()) {
			event.reply("You own all the roles! WOW! 🎆").queue();
			return;
		}

		String[] unownedRoleNames = new String[allUnownedRoles.size()];

		for (int i=0; i < allUnownedRoles.size(); i++) unownedRoleNames[i] = allUnownedRoles.get(i).getName();

		Paginator pager = new Paginator.Builder()
			.addItems(unownedRoleNames)
			.setText(event.getMember().getAsMention())
			.setEventWaiter(waiter)
			.setTimeout(20, TimeUnit.SECONDS)
			.setBulkSkipNumber(5)
			.allowTextInput(false)
			.setColor(Color.CYAN)
			.setColumns(1)
			.setItemsPerPage(10)
			.setLeftRightText("Left", "Right")
			.showPageNumbers(true)
			.waitOnSinglePage(true)
			.useNumberedItems(true)
			.wrapPageEnds(true)
			.addUsers(event.getUser())
			.build();
		pager.paginate(event.getTextChannel(), 1);
		event.reply("Menu Displayed").setEphemeral(true).queue();
	}

	private List<Role> getAllUnownedRoles(SlashCommandEvent event) {
		List<Role> temp = event.getMember().getRoles();
		List<Role> memberRoles = new ArrayList<>();
		memberRoles.addAll(temp); //Temp is immutable, need to copy to mutable
		List<Role> vanityRoles = getVanityRoles(event.getGuild());

		//Remove all non-vanity roles from member roles
		memberRoles.retainAll(vanityRoles);
		//member roles now only contains owned vanity roles
		//Remove all member-owned roles from vanity roles
		vanityRoles.removeAll(memberRoles);
		//vanity roles now contains only unowned roles

		return vanityRoles;
	}

	private static List<Role> getVanityRoles(Guild g) {

		List<Role> roles = new ArrayList<>();

		boolean collecting = false;
		for (Role r : g.getRoles()) {
			if (r.getName().equalsIgnoreCase("--- Chop Shop ---")) {
				collecting = true;
				continue;
			}
			else if (r.getName().equalsIgnoreCase("@everyone")) collecting = false;
			if (collecting) roles.add(r);
		}
		return roles;
	}
}
