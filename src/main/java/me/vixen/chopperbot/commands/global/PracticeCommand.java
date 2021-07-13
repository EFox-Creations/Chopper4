package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.Random;

public class PracticeCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		String userid = event.getUser().getId();
		Guild guild = event.getGuild();
		//noinspection ConstantConditions is required
		int numoflocks = (int) event.getOption("numoflocks").getAsLong();
		//noinspection ConstantConditions cant be null
		DBMember member = Database.getMember(guild, userid);
		if (member == null) {
			event.reply("An unknown error occurred; aborting with Error Code PLC1").queue();
			return;
		}
		if (member.getLockCount() < numoflocks) {
			event.replyEmbeds(Embeds.getInsufficientLocks()).queue();
			return;
		}

		int skill = member.getSkill();
		int skillIncrease = 0;
		int usedLocks = 0;
		for (int i = 1; i <= numoflocks; i++ , usedLocks++, member.adjustLockCount(-1)) {
			int rand =
				skill+skillIncrease < 10 ?
					new Random().nextInt(10)+1 :
					new Random().nextInt(100)+1;
			if (skill > rand) {
				member.adjustSkill(1);
				skillIncrease++;
			}

		}
		member.update();

		int newSkill = skill+skillIncrease;

		event.replyEmbeds(
			new EmbedBuilder()
				.setColor(skillIncrease > 0 ? Color.GREEN : Color.YELLOW)
				.setTitle("Practice Lock Results")
				.setDescription("Used " + usedLocks + " locks!\nSkill: " + skill + " -> " + newSkill)
				.build()
		).queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("practice", "Use your locks to up your skill")
			.addOptions(
				new OptionData(OptionType.INTEGER, "numoflocks", "How many locks to use?",true)
			);
	}
}
