package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.GuildManager;
import me.vixen.chopperbot.listener.DefaultEventHandler;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class ResetCommand implements ICommand {

    GuildManager gManager;
    public ResetCommand(GuildManager gManager) {
        this.gManager = gManager;
    }

    @Override
    public void handle(SlashCommandEvent event) {
        if (!event.getUser().getId().equals(Entry.CREATOR_ID)) {
            event.replyEmbeds(Embeds.getPermissionMissing()).queue();
            return;
        }

        event.reply("Resetting...").setEphemeral(true).queue();
        for (Guild g : Entry.jda.getGuilds()) {
            if (gManager.contains(g)) {
                gManager.getGuild(g).doNightlyReset();
            } else DefaultEventHandler.nightlyReset(g);
        }
        event.getHook().editOriginal("Done!").queue();
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("reset-day", "Reset day counts (Only works for creator)");
    }
}
