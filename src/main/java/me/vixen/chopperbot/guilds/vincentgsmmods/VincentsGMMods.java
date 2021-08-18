package me.vixen.chopperbot.guilds.vincentgsmmods;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.IGuild;

import java.util.ArrayList;
import java.util.List;

public class VincentsGMMods implements IGuild {

    private final String guildId;
    private static List<ICommand> localCommands = new ArrayList<>();
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
}
