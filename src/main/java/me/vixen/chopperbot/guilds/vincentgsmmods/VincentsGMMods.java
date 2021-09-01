package me.vixen.chopperbot.guilds.vincentgsmmods;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.guilds.CustomGuild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class VincentsGMMods extends CustomGuild {

    public VincentsGMMods(String guildId, EventWaiter waiter) {
        super(guildId, waiter);
    }

    @Override
    protected void setLocalCommands(EventWaiter waiter) {
        localCommands = EMPTY_COMMANDS;
    }

    @Override
    public boolean hasCustomClaims() {
        return false;
    }

    @Override
    public void getCustomClaim(SlashCommandEvent event) {
        return;
    }
}
