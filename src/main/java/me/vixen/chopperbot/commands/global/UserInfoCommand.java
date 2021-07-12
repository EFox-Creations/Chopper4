package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class UserInfoCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		OptionMapping userOpt = event.getOption("user");
		Member targetMem;

		if (userOpt != null)
			targetMem = userOpt.getAsMember();
		else {
			event.replyEmbeds(
				Embeds.getInvalidArgumentEmbed("user", "Must provide a user"))
				.queue();
			return;
		}

		if (targetMem == null) {
			try {
				event.replyEmbeds(Embeds.getUserInfo(userOpt.getAsUser())).queue();
			} catch (Exception e) {
				event.reply("User is not visible. Cannot display information").queue();
			}
		} else {
			event.replyEmbeds(Embeds.getMemberInfo(targetMem)).queue();
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("userinfo", "Gets Information about a user")
			.addOption(OptionType.USER, "user", "The user to look up", true);
	}
}
