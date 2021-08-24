package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
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
	public void handle(SlashCommandEvent event, UserProfile profile) {
		//noinspection ConstantConditions is required
		int numoflocks = (int) event.getOption("numoflocks").getAsLong();
		//noinspection ConstantConditions cant be null
		if (profile.getLockCount() < numoflocks) {
			event.replyEmbeds(Embeds.getInsufficientLocks()).queue();
			return;
		}

		int skill = profile.getSkill();
		int skillIncrease = 0;
		int usedLocks = 0;
		for (int i = 1; i <= numoflocks; i++ , usedLocks++, profile.adjustLockCount(-1)) {
			int rand =
				skill+skillIncrease < 10 ?
					new Random().nextInt(10)+1 :
					new Random().nextInt(100)+1;
			if (skill > rand) {
				profile.adjustSkill(1);
				skillIncrease++;
			}

		}
		profile.update(null);

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
