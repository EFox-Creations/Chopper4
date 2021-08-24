package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DonateCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event, UserProfile donator) {
		Guild guild = event.getGuild();

		//noinspection ConstantConditions is required
		int amount = (int) event.getOption("amount").getAsLong();
		if (donator.getCoins() < amount) {
			event.replyEmbeds(Embeds.getInsufficientCoins()).queue();
			return;
		}

		//noinspection ConstantConditions is required
		Member member = event.getOption("user").getAsMember();
		if (member == null) {
			event.replyEmbeds(Embeds.getUnknownMember()).queue();
			return;
		}

		UserProfile reciever = Database.getMember(guild, member.getUser().getId());

		if (reciever == null) {
			event.reply("An error occurred; aborting with code " + Errors.DBNULLRETURN).queue();
			return;
		}

		donator.adjustCoins(-amount);
		reciever.adjustCoins(amount);
		donator.update(null);
		reciever.update(null);

		event.reply(
			String.format("Donated %d coins to %s", amount, member.getUser().getAsTag())
		).queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("donate", "Donate coins to another member")
			.addOptions(
				new OptionData(OptionType.INTEGER, "amount", "The amount to donate", true),
				new OptionData(OptionType.USER, "user", "Who to donate to? (Must be in this guild)", true)
			);
	}
}
