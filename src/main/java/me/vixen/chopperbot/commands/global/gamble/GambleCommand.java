package me.vixen.chopperbot.commands.global.gamble;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.concurrent.TimeUnit;

public class GambleCommand implements ICommand {

    private EventWaiter waiter;
    public GambleCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(SlashCommandEvent event, UserProfile profile) {
        SelectionMenu gameMenu = SelectionMenu.create("menu:gameMenu")
            .setPlaceholder("Choose your game!")
            .setRequiredRange(1,1)
            .addOption("Scratch Off", "scratchoff", "Play a scratch ticket!", Emoji.fromUnicode("🎫"))
            .addOption("Bet", "bet", "A basic game of chance!", Emoji.fromUnicode("💵"))
            .addOption("Slot Machine", "slot", "A Real slot machine!", Emoji.fromUnicode("🎰"))
            .addOption("High-Low", "highlow", "Is the number higher or lower?", Emoji.fromUnicode("↕"))
            .addOption("Dice", "dice", "Roll of the dice", Emoji.fromUnicode("🎲"))
            .build();

        event.deferReply().queue();
        event.getHook().editOriginal("What game would you like to play?").setActionRow(gameMenu).queue(msg -> {
            waiter.waitForEvent(
                SelectionMenuEvent.class,
                (sme) -> sme.getMessage().equals(msg) && sme.getUser().equals(event.getUser()),
                (sme) -> {
                    //noinspection ConstantConditions at least 1 selection is required
                    sme.deferEdit().queue();
                    switch (sme.getSelectedOptions().get(0).getValue()) {
                        case "scratchoff" -> new ScratchOff(waiter).handle(event, profile);
                        case "bet" -> new Bet().handle(event, profile);
                        case "slot" -> new Slot().handle(event,profile);
                        case "highlow" -> new HighLow(waiter).handle(event, profile);
                        case "dice" -> new Dice().handle(event, profile);
                    }
                },
                1L, TimeUnit.MINUTES,
                () -> msg.delete().queue()
            );
        });
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("gamble", "Gamble your coins!")
            .addOption(OptionType.INTEGER, "bet", "How much to bet?", true);
    }
}
