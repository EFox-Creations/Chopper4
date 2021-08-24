package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class EchoCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		//noinspection ConstantConditions cant be null
		UserProfile member = Database.getMember(guild, user.getId());
		if (member == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		if (!member.isAuthorized()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).queue();
			return;
		}

		//noinspection ConstantConditions cant be null
		String phrase = event.getOption("phrase").getAsString();
		OptionMapping msgid = event.getOption("msgid");
		event.deferReply(true).queue();
		if (msgid == null) {
			event.getTextChannel().sendMessage(phrase).queue(msg ->
				event.getHook().editOriginal("Phrase Echoed").queue());
		} else {
			event.getTextChannel().retrieveMessageById(msgid.getAsString()).queue(msg -> {
				msg.reply(phrase).mentionRepliedUser(true).queue();
				event.getHook().editOriginal("Phrase Replied").queue();
			});
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("say", "Send bot messages").addOptions(
			new OptionData(OptionType.STRING, "phrase", "The phrase to echo", true),
			new OptionData(OptionType.STRING, "msgid", "A message id to reply to (leave blank to send a normal msg)")
		);
	}
}
