package me.vixen.chopperbot.commands.global.gamble;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.util.Random;

public class Dice {

    public void handle(SlashCommandEvent event, UserProfile profile) {
        int bet = (int) event.getOption("bet").getAsLong();
        if (bet <= 0) {
            event.getHook().editOriginalEmbeds(Embeds.getInvalidArgumentEmbed("bet", " Must be more than 0")).setActionRows().setContent("").queue();
            return;
        }

        int availableCoins = profile.getCoins();

        if (bet > availableCoins) {
            event.getHook().editOriginalEmbeds(Embeds.getInsufficientCoins()).setActionRows().setContent("").queue();
            return;
        }

        int myRoll = new Random().nextInt(6)+1;
        int yourRoll = new Random().nextInt(6)+1;

        Color color;
        String title;
        int payout;
        if (yourRoll > myRoll) {
            color = Color.GREEN;
            title = "You win!";
            payout = (int) Math.round(bet * new Random().nextDouble() + 1.1);
        } else if (yourRoll == myRoll) {
            color = Color.YELLOW;
            title = "We tied!";
            payout = bet;
        } else {
            color = Color.RED;
            title = "You lose!";
            payout = bet * -1;
        }

        event.getHook().editOriginalEmbeds(new EmbedBuilder()
            .setAuthor(event.getUser().getAsTag(), null, event.getUser().getAvatarUrl())
            .setColor(color)
            .setTitle(title)
            .setDescription(
                String.format("Your roll: %d\nMy roll: %d", yourRoll, myRoll) +"\n\n" +
                    "Payout: " + payout
            ).build()
        ).setActionRows().setContent("").queue();

        profile.adjustCoins(payout);
        profile.update(event.getMember());
        if (payout < 0) Database.addToPot(payout*-1);
    }
}
