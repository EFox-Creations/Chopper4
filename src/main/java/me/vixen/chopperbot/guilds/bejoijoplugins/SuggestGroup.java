package me.vixen.chopperbot.guilds.bejoijoplugins;

import me.vixen.chopperbot.commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;

public class SuggestGroup implements ICommand {
    @Override
    public void handle(SlashCommandEvent event) {
        String asTag = event.getUser().getAsTag();
        //noinspection ConstantConditions cant be null; is required
        String text = event.getOption("text").getAsString();
        //noinspection ConstantConditions cant be null; can only be fired from guild
        TextChannel ideadump = event.getGuild().getTextChannelById("663796720173580317");
        if (ideadump == null) return;
        ideadump.sendMessageEmbeds(
                new EmbedBuilder()
                    .setColor(Color.CYAN)
                    .setTitle(asTag)
                    .addField("New Suggestion", text, false)
                    .build()
        ).queue(msg -> {
            msg.addReaction("⬆").queue();
        });
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("suggest", "Make a new suggestion").addOptions(
            new OptionData(OptionType.STRING, "text", "The suggestion", true)
        );
    }
}
