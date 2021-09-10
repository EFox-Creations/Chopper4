package me.vixen.chopperbot.commands.global.userprofile;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.guilds.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import java.awt.Color;
import java.util.concurrent.TimeUnit;

class ToggleLvlMsgs {

	EventWaiter waiter;
	public ToggleLvlMsgs(EventWaiter waiter) {
		this.waiter = waiter;
	}

	public void handle(SlashCommandEvent event, UserProfile profile) {

		Config config = Database.getConfig(event.getGuild().getId());
		if (config != null && config.arelvlMsgOverridden()) {
			event.getHook().editOriginal("This server has set LvlMsgs to off. You may not turn them on").setEmbeds().setActionRows().queue();
			return;
		}

		event.getHook().editOriginalEmbeds(
			new EmbedBuilder()
				.setTitle("Turn level messages ON or OFF?")
				.setColor(Color.YELLOW)
				.build()
		).setContent("").setActionRow(
			Button.success("msgson", "ON"),
			Button.danger("msgsoff", "OFF")
		).queue(msg -> {
			waiter.waitForEvent(
				ButtonClickEvent.class,
				(bce) -> bce.getMessage().equals(msg) && bce.getUser().equals(event.getUser()),
				(bce) -> {
					String buttonName = bce.getComponentId();
					if (buttonName.equals("msgson")) {
						profile.setLvlMsgs(true);
						profile.update(event.getMember());
						event.getHook().editOriginal("Your level up messages are now **ON**").setEmbeds().setActionRows().queue();
					} else if (buttonName.equals("msgsoff")) {
						profile.setLvlMsgs(false);
						event.getHook().editOriginal("Your level up messages are now **OFF**").setEmbeds().setActionRows().queue();
						profile.update(event.getMember());
					}
				},
				30L, TimeUnit.SECONDS,
				() -> msg.delete().queue()
			);
		});
	}
}
