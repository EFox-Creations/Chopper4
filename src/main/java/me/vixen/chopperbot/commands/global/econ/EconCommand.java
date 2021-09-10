package me.vixen.chopperbot.commands.global.econ;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.guilds.GuildManager;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.concurrent.TimeUnit;

public class EconCommand implements ICommand {

    EventWaiter waiter;
    GuildManager guildManager;
    public EconCommand(EventWaiter waiter, GuildManager guildManager) {
        this.waiter = waiter;
        this.guildManager = guildManager;
    }

    @Override
    public void handle(SlashCommandEvent event, UserProfile profile) {
        event.deferReply().queue();

        SelectionMenu econMenu = SelectionMenu.create("menu:econMenu")
            .setPlaceholder("Choose your command")
            .setRequiredRange(1,1)
            .addOption("Claim Daily", "claimdaily", "Claim your daily prizes", Emoji.fromUnicode("📆"))
//          .addOption("Search", "search", "Search for coins", Emoji.fromUnicode("🔍"))
//          .addOption("Beg", "beg", "Beg for coins", Emoji.fromUnicode("💬"))
//          .addOption("Work", "work", "Work for coins", Emoji.fromUnicode("🏢"))
            .addOption("Rob", "rob", "Try and rob someone", Emoji.fromUnicode("🚓"))
            .build();

        event.getHook().editOriginal("").setActionRow(econMenu).queue(msg ->
            waiter.waitForEvent(
                SelectionMenuEvent.class,
                (sme) -> sme.getMessage().equals(msg) && sme.getUser().equals(event.getUser()),
                (sme) -> {
                    //noinspection ConstantConditions at least one selection is required
                    switch (sme.getSelectedOptions().get(0).getValue()) {
                        case "claimdaily" -> new DailyClaim(guildManager).handle(event, profile);
                        case "rob" -> new Rob().handle(event, profile);
                    }
                },
                30L, TimeUnit.SECONDS,
                () -> msg.delete().queue()
            )
        );
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("econ", "Open the economy command menu");
    }
}
