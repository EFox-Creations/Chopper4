package me.vixen.chopperbot.guilds;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.ChopBot;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.listener.DefaultEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomGuild {

    protected final List<ICommand> EMPTY_COMMANDS = new ArrayList<>();

    protected final String guildId;
    protected final EventWaiter waiter;
    protected List<ICommand> localCommands = new ArrayList<>();

    protected CustomGuild(String guildId, EventWaiter waiter){
        this.waiter = waiter;
        this.guildId = guildId;
        setLocalCommands(waiter);
    }

    protected abstract void setLocalCommands(EventWaiter waiter);

    public final List<ICommand> getLocalCommands() {
        return localCommands;
    }

    public final String getId() {
        return guildId;
    }

    public final Guild getGuild() {
        return ChopBot.getJDA().getGuildById(guildId);
    }

    public final String getName() {
        return getGuild().getName();
    }

    //Temporarily disabled
    // TextChannel getLottoChannel() { return getGuild().getSystemChannel(); }

    public abstract boolean hasCustomClaims();

    public abstract void getCustomClaim(SlashCommandEvent event);

    public void doNightlyReset() {
        DefaultEventHandler.nightlyReset(Entry.getJDA().getGuildById(getId()));
    }

    public void handleSlashCommand(SlashCommandEvent event, EventWaiter waiter, GlobalCommandManager cManager, UserProfile profile) {
        DefaultEventHandler.handleSlashCommand(event, cManager, profile);
    }

    public void handleGMsgReceived(GuildMessageReceivedEvent event, EventWaiter waiter, UserProfile profile) {
        DefaultEventHandler.handleGMsgReceived(event, profile);
    }

    public void handleGMsgReactAdd(GuildMessageReactionAddEvent event, EventWaiter waiter) {
        DefaultEventHandler.handleGMsgReactAdd(event);
    }
    public void handleGMemJoin(GuildMemberJoinEvent event, EventWaiter waiter) {
        DefaultEventHandler.handleGMemJoin(event, waiter);
    }
    public void handleGMemRemove(GuildMemberRemoveEvent event, EventWaiter waiter) {
        DefaultEventHandler.handleGMemRemove(event);
    }

    @SuppressWarnings("unused")
    public void handleGVoiceJoin(GuildVoiceJoinEvent event) {
        DefaultEventHandler.handleGVoiceJoin(event);
    }
    @SuppressWarnings("unused")
    public void handleGVoiceLeave(GuildVoiceLeaveEvent event) {
        DefaultEventHandler.handleGVoiceLeave(event);
    }
    @SuppressWarnings("unused")
    public void handleGVoiceMove(GuildVoiceMoveEvent event) {
        DefaultEventHandler.handleGVoiceMove(event);
    }
}
