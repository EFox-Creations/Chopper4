package me.vixen.chopperbot.guilds.bejoijoplugins;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FindCommand implements ICommand {
	EventWaiter waiter;
	public FindCommand(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		if (!dbMember.isAuthorized()) {
			event.reply("You do not have the correct permissions").setEphemeral(true).queue();
			return;
		}

		event.getGuild().loadMembers()
			.onSuccess(m-> {
				List<Member> members = new ArrayList<>(m);
				final List<Member> filtered = members.stream()
					.filter(mem -> ChronoUnit.DAYS.between(mem.getTimeJoined(), OffsetDateTime.now()) > 5)
					.filter(mem -> mem.getRoles().isEmpty())
					.collect(Collectors.toList());

				if (filtered.isEmpty()) {
					event.reply("There are no members older than 5 days with 0 roles").queue();
					return;
				}

				String[] nerds = new String[filtered.size()];
				for (int i = 0; i < filtered.size(); i++) {
					final Member temp = filtered.get(i);
					nerds[i] = temp.getUser().getAsTag();
				}

				Paginator pager = new Paginator.Builder()
					.setItems(nerds)
					.setItemsPerPage(20)
					.setColumns(2)
					.setText("Members older than 5 days with no roles: ")
					.allowTextInput(false)
					.setBulkSkipNumber(2)
					.setColor(Color.ORANGE)
					.setEventWaiter(waiter)
					.setTimeout(1L, TimeUnit.MINUTES)
					.showPageNumbers(true)
					.waitOnSinglePage(true)
					.addUsers(event.getUser())
					.build();

				event.reply("See Menu Below").setEphemeral(true).queue();
				pager.paginate(event.getTextChannel(), 1);
			})
			.onError(voided -> event.reply("An error occurred").setEphemeral(true).queue());
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("find", "Finds nerds who haven't verified");
	}
}
