package me.vixen.chopperbot.guilds.outlierscoaching;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.guilds.CustomGuild;
import me.vixen.chopperbot.listener.DefaultEventHandler;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Comparator;
import java.util.List;

public class OutliersCoaching extends CustomGuild {


    public OutliersCoaching(String guildId, EventWaiter waiter) {
        super(guildId, waiter);
    }

    @Override
    public void setLocalCommands(EventWaiter waiter) {
        localCommands = List.of(
            new VoiceGroup()
        );
    }

    @Override
    public boolean hasCustomClaims() {
        return false;
    }

    @Override
    public void getCustomClaim(SlashCommandEvent event) {
        return;
    }

    @Override
    public void handleSlashCommand(SlashCommandEvent event, EventWaiter waiter, GlobalCommandManager cManager, UserProfile profile) {
        boolean found = false;
        for (ICommand c : getLocalCommands()) {
            if (c.getName().equals(event.getName())) {
                c.handle(event, profile);
                found = true;
                break;
            }
        }
        if (!found) DefaultEventHandler.handleSlashCommand(event, cManager, profile);
    }

    @Override
    public void handleGVoiceJoin(GuildVoiceJoinEvent event) {
        createVoice(event.getChannelJoined(), event.getMember());
    }

    @Override
    public void handleGVoiceLeave(GuildVoiceLeaveEvent event) {
        deleteVoice(event.getChannelLeft());
    }

    @Override
    public void handleGVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getChannelLeft().getName().equalsIgnoreCase("Create Lobby"))
            return;

        createVoice(event.getChannelJoined(), event.getMember());
        deleteVoice(event.getChannelLeft());
    }

    private void deleteVoice(VoiceChannel channelLeft) {
        Category parent = channelLeft.getParent();
        //Don't delete the creation lobby
        if (channelLeft.getName().equalsIgnoreCase("Create Lobby"))
            return;
        //If is a dynamic lobby and is empty
        if (parent.getName().equalsIgnoreCase("Dynamic Lobbies")
            && channelLeft.getMembers().isEmpty()) {
            channelLeft.delete().queue(); //delete it
        }
    }

    private void createVoice(VoiceChannel channelJoined, Member m) {
        Category parent = channelJoined.getParent();
        if (channelJoined.getName().equalsIgnoreCase("Create Lobby") &&
            parent.getName().equalsIgnoreCase("Dynamic Lobbies")) {

            parent.createVoiceChannel(m.getEffectiveName() + "'s Lobby").queue(vc -> {
                vc.getGuild().moveVoiceMember(m, vc).queue();

                parent.modifyVoiceChannelPositions()
                    .sortOrder(Comparator.comparing(guildChannel -> guildChannel.getTimeCreated())).reverseOrder()
                    .selectPosition(channelJoined)
                        .moveTo(0)
                .queue();
            });
        }
    }
}
