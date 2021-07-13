package me.vixen.chopperbot.guilds.bejoijoplugins;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.ICommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.concurrent.TimeUnit;

public class ConvertCommand implements ICommand {

	private final EventWaiter waiter;
	public ConvertCommand(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		final Member slashMember = event.getMember();
		final TextChannel textChannel = event.getTextChannel();
		event.reply("Please paste your EUP config now").queue();
		final InteractionHook hook = event.getHook();
		//noinspection ConstantConditions cant be null
		waiter.waitForEvent(
			GuildMessageReceivedEvent.class,
			(event1) -> (
				event1.getChannel().equals(textChannel)
					&& slashMember.equals(event1.getMember())
					&& !event1.getMessage().getContentRaw().startsWith("/")
			),
			(event2) -> convert(hook, event2),
			10L, TimeUnit.SECONDS,
			() -> hook.editOriginal("Timed out waiting, please start over").queue()
		);
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("convert", "Starts the conversion process. *DO NOT paste your EUP yet*");
	}

	private void convert(InteractionHook hook, GuildMessageReceivedEvent event) {
		event.getMessage().delete().queue();
		try {
			final String convert = EUP2UB.convert(event);
			hook.editOriginal(convert).queue();
		} catch (IllegalArgumentException e) {
			hook.editOriginal("Invalid EUP Form detected! Please check your input! (.convert is no longer needed)").queue();
		}
	}
}
