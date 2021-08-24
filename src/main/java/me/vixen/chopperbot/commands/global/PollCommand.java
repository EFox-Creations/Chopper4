package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.guilds.Config;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollCommand implements ICommand {

	@Override
	public void handle(SlashCommandEvent event) {
		//noinspection ConstantConditions
		UserProfile dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		Config config = Database.getConfig(event.getGuild().getId());
		if (config == null) {
			event.replyEmbeds(Embeds.getPleaseDoConfig()).queue();
			return;
		}
		if (!dbMember.isAuthorized() && config.isOnlyStaffPolls()) {
			event.replyEmbeds(Embeds.getPermissionMissing()).setEphemeral(true).queue();
			return;
		}

		event.deferReply().queue(hook -> hook.deleteOriginal().queueAfter(5L, TimeUnit.SECONDS));
		StringBuilder builder = new StringBuilder();

		final List<OptionMapping> options = event.getOptions();
		for (int i = 0; i < options.size(); i++) {
			final String text = options.get(i).getAsString();
			builder.append(voteEmotes[i]).append(" ").append(text).append("\n");
		}
		String messageText = event.getOption("message").getAsString();

		MessageEmbed embed = new EmbedBuilder()
			.setColor(Color.cyan)
			.setTitle("Poll: " + messageText)
			.setDescription(builder.toString().trim())
			.build();

		event.getTextChannel().sendMessageEmbeds(embed).queue(message -> {
			for (int i=0; i < options.size(); i++) message.addReaction(voteEmotes[i]).queue();
		});
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("makepoll", "Create a poll with up to ten options (Minimum two options)").addOptions(
			new OptionData(OptionType.STRING, "message", "The poll message", true),
			new OptionData(OptionType.STRING, "one", "The first Poll option",true),
			new OptionData(OptionType.STRING, "two", "The second Poll option",true),
			new OptionData(OptionType.STRING, "three", "The third Poll option"),
			new OptionData(OptionType.STRING, "four", "The fourth Poll option"),
			new OptionData(OptionType.STRING, "five", "The fifth Poll option"),
			new OptionData(OptionType.STRING, "six", "The sixth Poll option"),
			new OptionData(OptionType.STRING, "seven", "The seventh Poll option"),
			new OptionData(OptionType.STRING, "eight", "The eighth Poll option"),
			new OptionData(OptionType.STRING, "nine", "The ninth Poll option"),
			new OptionData(OptionType.STRING, "ten", "The tenth Poll option")
		);
	}

	private final String[] voteEmotes = {
		"\u0031\u20E3", //1
		"\u0032\u20E3", //2
		"\u0033\u20E3", //3
		"\u0034\u20E3", //4
		"\u0035\u20E3", //5
		"\u0036\u20E3", //6
		"\u0037\u20E3", //7
		"\u0038\u20E3", //8
		"\u0039\u20E3", //9
		"\u0030\u20E3"  //0
	};

}
