package me.vixen.chopperbot.commands.global;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Logger;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.CustomEmbed;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class EmbedSendCommand implements ICommand {

    EventWaiter waiter;
    public EmbedSendCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }

    @Override
    public void handle(SlashCommandEvent event) {
        Guild guild = event.getGuild();
        User moderator = event.getUser();
        String moderatorId = moderator.getId();
        UserProfile moderatorDB = Database.getMember(guild, moderatorId);
        if (!moderatorDB.isAuthorized()) {
            event.replyEmbeds(Embeds.getPermissionMissing()).queue();
            return;
        }

        boolean sendtemplate = event.getOption("sendtemplate").getAsBoolean();
        if (sendtemplate) {
            event.deferReply().queue();
            event.getHook().editOriginal("Here is the template")
                .addFile(new File("exampleembed.json5"))
                .queue();
            return;
        }

        event.reply("Please paste the text now").queue(hook-> hook.retrieveOriginal().queue(msg ->
            waiter.waitForEvent(GuildMessageReceivedEvent.class,
            (gmre) -> gmre.getChannel().equals(event.getChannel())
                        && gmre.getMember().equals(event.getMember()),
            (gmre) -> {
                event.getHook().editOriginal("Please Wait...").queue();
                sendEmbed(gmre, event);
            },
            20L, TimeUnit.SECONDS,
            () -> event.getHook().deleteOriginal().queue()
        )));
    }

    private void sendEmbed(GuildMessageReceivedEvent gmre, SlashCommandEvent sce) {
        gmre.getMessage().delete().queue();
        String json = gmre.getMessage().getContentRaw();
        try {
            CustomEmbed customEmbed = new GsonBuilder().create().fromJson(json, CustomEmbed.class);
            if (customEmbed == null) {
                sce.getHook().editOriginal("Invalid JSON. Please verify and retry").queue();
                return;
            }
            MessageEmbed embed = customEmbed.toMessageEmbed();
            if (embed.isSendable() && !embed.isEmpty()) {
                gmre.getGuild().getTextChannelById(customEmbed.getChannelId()).sendMessageEmbeds(embed).queue();
                sce.getHook().editOriginal("Embed Sent").queue(v -> sce.getHook().deleteOriginal().queueAfter(2L, TimeUnit.SECONDS));
            } else {
                sce.getHook().editOriginal("Invalid Embed state").queue();
            }

        } catch (JsonSyntaxException e) {
            sce.getHook().editOriginal("Invalid JSON. Please verify and retry").queue();
            Logger.log("Invalid JSON", e);
        }

    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("customembed", "Send a custom embed")
            .addOption(
                OptionType.BOOLEAN,
                "sendtemplate",
                "Set this to false to send your embed",
                true);
    }
}
