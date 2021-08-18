package me.vixen.chopperbot.guilds.outlierscoaching;

import me.vixen.chopperbot.commands.ICommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class VoiceGroup implements ICommand {
    @Override
    public void handle(SlashCommandEvent event) {
        //noinspection ConstantConditions member is not null
        GuildVoiceState voiceState = event.getMember().getVoiceState();
        if (voiceState == null || !voiceState.inVoiceChannel()) {
            event.reply("You are not currently in a voice channel").queue();
            return;
        }

        String subcommandName = event.getSubcommandName();
        //noinspection ConstantConditions
        switch (subcommandName) {
            case "lock" -> {
                event.deferReply().queue();
                //noinspection ConstantConditions channel is not null
                int userLimit = voiceState.getChannel().getUserLimit();
                if (userLimit == 1) {
                    voiceState.getChannel().getManager().setUserLimit(0).queue(v ->
                        event.getHook().editOriginal("Unlocked!").queue()
                    );
                } else {
                    voiceState.getChannel().getManager().setUserLimit(1).queue(v ->
                        event.getHook().editOriginal("Locked!").queue()
                    );
                }
            }
            case "set-limit" -> {
                //noinspection ConstantConditions option is required
                long limit = event.getOption("limit").getAsLong();
                if (limit > 99 || limit < 0) {
                    event.reply("Invalid argument provided! \"Limit\" must be 0-99").queue();
                    return;
                }
                event.deferReply().queue();
                //noinspection ConstantConditions channel is not null
                voiceState.getChannel().getManager().setUserLimit((int) limit).queue(v ->
                    event.getHook().editOriginal("Set new limit!").queue()
                );
            }
            case "set-name" -> {
                //noinspection ConstantConditions option is required
                String name = event.getOption("name").getAsString();
                event.deferReply().queue();
                //noinspection ConstantConditions channel is not null
                voiceState.getChannel().getManager().setName(name).queue(v ->
                    event.getHook().editOriginal("Set new name!").queue()
                );
            }
            case "leave" -> {
                event.deferReply().queue();
                //noinspection ConstantConditions channel is not null
                event.getGuild().moveVoiceMember(event.getMember(), null).queue(v ->
                    event.getHook().editOriginal("Booted!").queue()
                );
            }
        }
    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("voice", "Voice channel commands")
            .addSubcommands(
                new SubcommandData("lock", "(Un)Lock your current voice channel"),
                new SubcommandData("set-limit", "Set a new user limit (1-99 or 0 to reset)")
                    .addOption(OptionType.INTEGER, "limit", "The limit to set", true),
                new SubcommandData("set-name", "The new name for the channel")
                    .addOption(OptionType.STRING, "name", "The new name", true),
                new SubcommandData("leave", "Leave the voice channel")
            );
    }
}
