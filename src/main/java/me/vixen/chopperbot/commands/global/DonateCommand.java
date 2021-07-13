package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DonateCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		Guild guild = event.getGuild();

		//noinspection ConstantConditions cant be null; no SCE from DMs accepted
		DBMember donator = Database.getMember(guild, event.getUser().getId());
		if (donator == null) {
			event.reply("An unknown error occurred; aborting with Error Code D01").queue();
			return;
		}
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

		DBMember reciever = Database.getMember(guild, member.getUser().getId());

		if (reciever == null) {
			event.reply("An error occurred; aborting with error: DonateTNull").queue();
			return;
		}

		donator.adjustCoins(-amount);
		reciever.adjustCoins(amount);
		donator.update();
		reciever.update();

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
