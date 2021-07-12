package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class EchoCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		DBMember member = Database.getMember(guild, user.getId());
		if (!member.isAuthorized()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		String phrase = event.getOption("phrase").getAsString();
		event.deferReply(true).queue();
		event.getTextChannel().sendMessage(phrase).queue(msg -> {
			event.getHook().editOriginal("Phrase Echoed").queue();
		});
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("say", "Send bot messages").addOptions(
			new OptionData(OptionType.STRING, "phrase", "The phrase to echo", true),
			new OptionData(OptionType.STRING, "msgid", "A message id to reply to (leave blank to send a normal msg)")
		);
	}
}
