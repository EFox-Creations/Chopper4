package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class AvatarCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		User target;
		OptionMapping username = event.getOption("username");
		if (username == null) target = event.getUser();
		else target = username.getAsUser();

		event.replyEmbeds(Embeds.getAvatarEmbed(target)).queue();
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("avatar", "Get someones avatar")
			.addOption(OptionType.USER, "username", "The user", false);
	}
}
