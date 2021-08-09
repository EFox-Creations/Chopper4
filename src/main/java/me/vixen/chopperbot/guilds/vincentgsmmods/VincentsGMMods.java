package me.vixen.chopperbot.guilds.vincentgsmmods;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.IGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.List;
import java.util.stream.Collectors;

public class VincentsGMMods implements IGuild {

    private final String guildId;
    protected static final int COLOR_COST = 200;
    protected static final int ROLE_COST = 150;
    private static List<ICommand> localCommands;
    public VincentsGMMods(String guildId, EventWaiter waiter) {
        this.guildId = guildId;
        setLocalCommands(waiter);
    }

    @Override
    public void setLocalCommands(EventWaiter waiter) {
        return;
    }

    @Override
    public List<ICommand> getLocalCommands() {
        return localCommands;
    }

    @Override
    public String getId() {
        return guildId;
    }

    @Override
    public List<TextChannel> getTreasureChannels() {
        List<String> whitelistIds = List.of(
            "692325966323908608", //General
            "692322570808066108", //Gallery
            "711172118259433513", //Dank memer 1
            "840232302591869008", //supporters chat
            "692325966323908608", //General
            "692324123044741190" //Support
        );
        return getGuild().getTextChannels().stream().filter(it ->
            whitelistIds.contains(it.getId())
        ).collect(Collectors.toList());
    }
}
