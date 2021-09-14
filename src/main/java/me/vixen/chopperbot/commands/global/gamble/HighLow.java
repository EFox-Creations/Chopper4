package me.vixen.chopperbot.commands.global.gamble;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.util.Random;

public class HighLow {

    EventWaiter waiter;
    public HighLow(EventWaiter waiter) {
        this.waiter = waiter;
    }

    public void handle(SlashCommandEvent event, UserProfile profile) {
        int bet = (int) event.getOption("bet").getAsLong();
        Member member = event.getMember();
        if (bet <= 0) {
            event.getHook().editOriginalEmbeds(Embeds.getInvalidArgumentEmbed("bet", " Must be more than 0")).setContent("").setActionRows().queue();
            return;
        }

        int availableCoins = profile.getCoins();

        if (bet > availableCoins) {
            event.getHook().editOriginalEmbeds(Embeds.getInsufficientCoins()).setContent("").setActionRows().queue();
            return;
        }

        int hint = new Random().nextInt(100)+1;
        int number = new Random().nextInt(100)+1;

        event.getHook().editOriginalEmbeds(new EmbedBuilder()
            .setAuthor(event.getUser().getAsTag(), null, event.getUser().getAvatarUrl())
            .setColor(Color.YELLOW)
            .setTitle("The first number is " + hint)
            .setDescription("Is the second number **Higher** or **Lower**" +
                "\nClick **JACKPOT** if you think the numbers are the same")
            .build()
        ).setActionRows(
            ActionRow.of(
                Button.primary("higher", "Higher").withEmoji(Emoji.fromUnicode("🔼")),
                Button.primary("jackpot", "Jackpot").withEmoji(Emoji.fromUnicode("🤑")),
                Button.primary("lower", "Lower").withEmoji(Emoji.fromUnicode("🔽"))
            )
        ).setContent("").queue(msg -> {
            waiter.waitForEvent(ButtonClickEvent.class,
                (bce) -> bce.getMember().equals(member) && bce.getMessageId().equals(msg.getId()),
                (bce) -> awardUser(bce, event, hint, number, bet, profile)
            );
        });
    }

    private void awardUser(ButtonClickEvent bce, SlashCommandEvent event, int hint, int number, int bet, UserProfile profile) {
        bce.deferEdit().queue();

        boolean won = false;

        switch (bce.getComponentId()) {
            case "higher" -> won = number > hint;
            case "jackpot" -> won = number == hint;
            case "lower" -> won = number < hint;
        }

        // Payout is 1.1x to 1.5x of the original bet
        double modifier = (((double) (new Random().nextInt(40) + 110)) / 100D);
        int payout = (int) Math.round(bet * modifier);

        bce.getMessage().editMessageEmbeds(new EmbedBuilder()
            .setColor(won ? Color.GREEN: Color.RED)
            .setAuthor(event.getUser().getAsTag(), null, event.getUser().getAvatarUrl())
            .setTitle(won? "You Win!": "You lose")
            .setDescription("The first number was " + hint + "\n" +
                "The second number was " + number + "\n\n" +
                (won ? "You won " + payout + " coins!" : "You lost " + bet + " coins!"))
            .build()
        ).setActionRows().queue();

        if (!won) Database.addToPot(payout * -1);
        profile.adjustCoins(won ? payout : payout * -1);
        profile.update(bce.getMember());
    }
}
