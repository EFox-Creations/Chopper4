package me.vixen.chopperbot.commands.global.gamble;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScratchOff {
    private static String EMPTY_LABEL = "   ";

    private EventWaiter waiter;
    public ScratchOff(EventWaiter waiter) {
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

        int rows = 5;
        int columns = 3;
        Cell[][] cellGrid = new Cell[rows][columns];

        int topPrizes = 1;
        int mediumPrizes = 2;
        int smallPrizes = 3;
        int tinyPrizes = 5;
        int noPrizes = 4;

        List<Integer> prizes = new ArrayList<>();
        for (int i = 0; i < topPrizes; i++)
            prizes.add(1);
        for (int i = 0; i < mediumPrizes; i++)
            prizes.add(2);
        for (int i = 0; i < smallPrizes; i++)
            prizes.add(3);
        for (int i = 0; i < tinyPrizes; i++)
            prizes.add(4);
        for (int i = 0; i < noPrizes; i++)
            prizes.add(5);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                Integer prizeNum = prizes.get(new Random().nextInt(prizes.size()));
                prizes.remove(prizeNum);
                switch (prizeNum) {
                    case 1 -> cellGrid[r][c] = new Cell(r, c, "topprize");
                    case 2 -> cellGrid[r][c] = new Cell(r, c, "mediumprize");
                    case 3 -> cellGrid[r][c] = new Cell(r, c, "smallprize");
                    case 4 -> cellGrid[r][c] = new Cell(r, c, "tinyprize");
                    case 5 -> cellGrid[r][c] = new Cell(r, c, "noprize");
                }
            }
        }
        int turnsLeft = 3;
        event.getHook().editOriginalEmbeds(getEmbed(member, turnsLeft, bet, 0)).setContent("")
            .setActionRows(getActionRows(cellGrid)).queue(msg ->
                waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (bce) -> bce.getMessageId().equals(msg.getId()) && bce.getMember().equals(member),
                    (bce) -> advanceGame(bce, cellGrid, turnsLeft, bet, 0)
                )
            );
    }

    private MessageEmbed getEmbed(Member m, int turnsLeft, double bet, double winnings) {
        return new EmbedBuilder()
            .setColor(turnsLeft == 0 ? getEndColor(bet, winnings) : Color.YELLOW)
            .setTitle(m.getUser().getName() + "'s Scratch-off")
            .setDescription(
                String.format("You may scratch **%d** more fields", turnsLeft) + "\n" +
                "You've won **" +  Math.round(winnings-bet) + "** so far"
            ).build();
    }

    private Color getEndColor(double bet, double winnings) {
        return winnings - bet > 0.0 ? Color.GREEN: Color.RED;
    }

    private List<ActionRow> getActionRows(Cell[][] cellArr) {
        return List.of(
            ActionRow.of(cellArr[0][0].button, cellArr[0][1].button, cellArr[0][2].button),
            ActionRow.of(cellArr[1][0].button, cellArr[1][1].button, cellArr[1][2].button),
            ActionRow.of(cellArr[2][0].button, cellArr[2][1].button, cellArr[2][2].button),
            ActionRow.of(cellArr[3][0].button, cellArr[3][1].button, cellArr[3][2].button),
            ActionRow.of(cellArr[4][0].button, cellArr[4][1].button, cellArr[4][2].button)
        );
    }

    private void advanceGame(ButtonClickEvent bce, Cell[][] cellArr, int turnsLeft, double bet, double winnings) {
        bce.deferEdit().queue();
        String buttonName = bce.getComponentId();
        String[] coordsArr = buttonName.split(",");
        int r = Integer.parseInt(coordsArr[0]);
        int c = Integer.parseInt(coordsArr[1]);
        Cell cell = cellArr[r][c];
        cell.visited = true;
        double earnings = winnings;
        switch (cell.prizeType) {
            case "topprize" -> {
                earnings += bet * 1.5;
                cell.button = Button.success(String.format("%d,%d", r, c), "👑").asDisabled();
            }
            case "mediumprize" -> {
                earnings += bet * .75;
                cell.button = Button.success(String.format("%d,%d", r, c), "🏆").asDisabled();
            }
            case "smallprize" -> {
                earnings += bet * .5;
                cell.button = Button.success(String.format("%d,%d", r, c), "🏅").asDisabled();
            }
            case "tinyprize" -> {
                earnings += bet * .15;
                cell.button = Button.success(String.format("%d,%d", r, c), "💰").asDisabled();
            }
            case "noprize" -> {
                earnings += 0;
                cell.button = Button.danger(String.format("%d,%d", r, c), EMPTY_LABEL).asDisabled();
            }
        }
        final int trueTurnsLeft = turnsLeft - 1;
        final double trueWinnings = earnings;
        MessageEmbed embed = getEmbed(bce.getMember(),trueTurnsLeft, bet, trueWinnings);

        if (trueTurnsLeft == 0) {
            UserProfile member = Database.getMember(bce.getGuild(), bce.getUser().getId());
            int payout = (int) Math.ceil(trueWinnings - bet);
            if (payout < 0) Database.addToPot(payout*-1);
            if (member != null) {
                member.adjustCoins(payout);
                member.update(null);
            }
            endGame(bce, embed, cellArr);
            return;
        }

        bce.getMessage().editMessageEmbeds(embed).setActionRows(getActionRows(cellArr)).queue(msg -> {
            waiter.waitForEvent(
                ButtonClickEvent.class,
                (bce1) -> bce1.getMessageId().equals(msg.getId()) && bce1.getMember().equals(bce.getMember()),
                (bce1) -> advanceGame(bce1, cellArr, trueTurnsLeft, bet, trueWinnings)
            );
        });
    }

    private void endGame(ButtonClickEvent event, MessageEmbed embed, Cell[][] cellArr) {
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 3; c++)
                cellArr[r][c].revealButton();
        event.getMessage().editMessageEmbeds(embed).setActionRows(getActionRows(cellArr)).queue();
    }

    private static class Cell {
        public boolean visited = false;
        public Button button;
        public int rCoord;
        public int cCoord;
        public String prizeType;

        private Cell(int rCoord, int cCoord, String prizeType) {
            this.rCoord = rCoord;
            this.cCoord = cCoord;
            this.prizeType = prizeType;
            this.button = Button.secondary(String.format("%d,%d", rCoord, cCoord), EMPTY_LABEL).asEnabled();
        }

        public void revealButton() {
            if (visited) return;
            switch (prizeType) {
                case "topprize" -> {
                    button = Button.secondary(String.format("%d,%d", rCoord, cCoord), "👑").asDisabled();
                }
                case "mediumprize" -> {
                    button = Button.secondary(String.format("%d,%d", rCoord, cCoord), "🏆").asDisabled();
                }
                case "smallprize" -> {
                    button = Button.secondary(String.format("%d,%d", rCoord, cCoord), "🎖").asDisabled();
                }
                case "tinyprize" -> {
                    button = Button.secondary(String.format("%d,%d", rCoord, cCoord), "💰").asDisabled();
                }
                case "noprize" -> {
                    button = Button.secondary(String.format("%d,%d", rCoord, cCoord), EMPTY_LABEL).asDisabled();
                }
            }
        }
    }
}
