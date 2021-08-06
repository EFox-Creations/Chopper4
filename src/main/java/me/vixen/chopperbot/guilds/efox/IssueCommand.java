package me.vixen.chopperbot.guilds.efox;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.Logger;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

public class IssueCommand implements ICommand {

    EventWaiter waiter;
    public IssueCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(SlashCommandEvent event) {

        //noinspection ConstantConditions can't be null - we don't except non-guild commands
        if (!EFoxHomeBase.isTicketTeam(event.getMember())) {
            event.replyEmbeds(Embeds.getPermissionMissing()).queue();
            return;
        }

        //noinspection ConstantConditions cant be null
        String title = event.getOption("title").getAsString();
        //noinspection ConstantConditions cant be null
        String body = event.getOption("body").getAsString();

        event.replyEmbeds(
                new EmbedBuilder()
                    .setTitle(title)
                    .setDescription(body)
                    .setColor(Color.YELLOW)
                    .build()
        ).setContent(event.getMember().getAsMention() +
            """
            `
            ⚠[Warn]⚠       ⚠[Warn]⚠       ⚠[Warn]⚠
            -This posts directly as Vixen
            -This will tag them here
            -This action is irreversible and
            cannot be undone
            
            -Are you sure you want to submit this?
            ⚠[Warn]⚠       ⚠[Warn]⚠       ⚠[Warn]⚠
            `
            """
        ).addActionRow(
                Button.danger("no", "NO"),
                Button.secondary("yes", "Yes")
        ).queue(hook -> hook.retrieveOriginal().queue(msg -> {
            waiter.waitForEvent(
                    ButtonClickEvent.class,
                    (bce) -> bce.getMessageId().equals(msg.getId()) && bce.getMember().equals(event.getMember()),
                    (bce) -> handleButtonClick(bce, event, msg)
            );
        }));
    }

    private void handleButtonClick(ButtonClickEvent bce, SlashCommandEvent sce, Message msg) {
        switch (bce.getComponentId().toLowerCase()) {
            case "yes" -> {
                bce.deferEdit().queue();
                sce.getHook().deleteOriginal().queue();
                String response = submitIssue(sce) ? "Submitted" : "An error occurred";
                msg.getTextChannel().sendMessage(bce.getMember().getAsMention() + "\n" + response).queue();
                if (response.equals("Submitted")) {
                    bce.getGuild().retrieveMemberById(Entry.CREATOR_ID).queue(member -> {
                        bce.getGuild().getTextChannelById("872224056785641492")
                            .sendMessage(member.getAsMention() + " New Issue Opened!").queue();
                    });
                }
            }
            case "no" -> {
                msg.delete().queue();
                bce.reply(bce.getMember().getAsMention() + "\nAborted submission").queue();
                sce.getHook().deleteOriginal().queue();
            }
        }
    }

    private boolean submitIssue(SlashCommandEvent sce) {
        //noinspection ConstantConditions cant be null
        String title = sce.getOption("title").getAsString();
        //noinspection ConstantConditions cant be null
        String body = sce.getOption("body").getAsString();

        try {
            //TODO this should be an JSON for easy editing
            BufferedReader reader = new BufferedReader(new FileReader("ghtoken.txt"));
            String token = reader.lines().findFirst().orElseThrow();
            GitHub github = new GitHubBuilder().withOAuthToken(token).build();
            github.getRepository("VixenKasai/Chopper4")
                .createIssue(title)
                    .body(body)
                    .assignee(github.getUser("VixenKasai"))
                .create();
            return true;
        } catch (IOException | NoSuchElementException e) {
            Logger.log("Failed to submit GH Issue: ", e);
            return false;
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("open-issue", "Open a new issue for Vixen to see").addOptions(
            new OptionData(OptionType.STRING, "title", "The title of the issue", true),
            new OptionData(OptionType.STRING, "body", "describe the issue", true)
        );
    }
}
