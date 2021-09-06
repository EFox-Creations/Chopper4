package me.vixen.chopperbot.commands.global.gamble;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class GambleGroup implements ICommand {

    private EventWaiter waiter;
    public GambleGroup(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(SlashCommandEvent event, UserProfile profile) {
        switch (event.getSubcommandName()) {
            case "scratchoff" -> new ScratchOff(waiter).handle(event, profile);
            case "bet" -> new Bet().handle(event, profile);
            case "slot" -> new Slot().handle(event,profile);
            case "highlow" -> new HighLow(waiter).handle(event, profile);
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("gamble", "Gamble your coins!").addSubcommands(
            new SubcommandData("scratchoff", "Play a scratch off ticket")
                .addOption(OptionType.INTEGER, "bet", "How much to bet?", true),
            new SubcommandData("bet", "The game of chance!")
                .addOption(OptionType.INTEGER, "bet", "How much to bet?", true),
            new SubcommandData("slot", "Play a slot machine!")
                .addOption(OptionType.INTEGER, "bet", "How much to bet?", true),
            new SubcommandData("highlow", "Play HighLow!")
                .addOption(OptionType.INTEGER, "bet", "How much to bet?", true),
            new SubcommandData("lotto_pool", "Displays the current pool"),
            new SubcommandData("buy_lotto", "Place a new Lotto bet").addOptions(
                new OptionData(OptionType.INTEGER, "betamount", "The amount you want to bet", true),
                new OptionData(OptionType.INTEGER, "firstnumber", "The first number (1-" + Lotto.UPPER + ")", true),
                new OptionData(OptionType.INTEGER, "secondnumber", "The second number (1-" + Lotto.UPPER + ")", true),
                new OptionData(OptionType.INTEGER, "thirdnumber", "The third number (1-" + Lotto.UPPER + ")", true),
                new OptionData(OptionType.INTEGER, "fourthnumber", "The fourth number (1-" + Lotto.UPPER + ")", true),
                new OptionData(OptionType.INTEGER, "fifthnumber", "The fifth number (1-" + Lotto.UPPER + ")", true)
            )
        );
    }
}
