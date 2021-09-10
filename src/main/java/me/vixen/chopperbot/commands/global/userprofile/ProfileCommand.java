package me.vixen.chopperbot.commands.global.userprofile;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import java.util.concurrent.TimeUnit;

public class ProfileCommand implements ICommand {

    EventWaiter waiter;
    public ProfileCommand(EventWaiter waiter) {
        this.waiter = waiter;
    }
    @Override
    public void handle(SlashCommandEvent event, UserProfile profile) {
        event.deferReply().queue();

        User target;
        OptionMapping userOpt = event.getOption("user");
        if (userOpt == null) {
            target = event.getUser();
        } else target = userOpt.getAsUser();

        SelectionMenu profileMenu = SelectionMenu.create("menu:profileMenu")
            .setPlaceholder("Choose an action")
            .setRequiredRange(1,1)
            .addOption("Avatar", "avatar", "Shows the avatar of the specified user", Emoji.fromUnicode("🙍‍♂️"))
            .addOption("Inventory", "inventory", "Shows your profile card", Emoji.fromUnicode("📇"))
            .addOption("Practice", "practice", "Use your practice locks!", Emoji.fromUnicode("🔒"))
            .addOption("Toggle Level Messages", "toggle", "Toggle on or off your level messages", Emoji.fromUnicode("💬"))
            .build();

        User finalTarget = target;
        event.getHook().editOriginal("Choose your action:").setActionRow(profileMenu).queue(msg -> {
            waiter.waitForEvent(
                SelectionMenuEvent.class,
                (sme) -> sme.getMessage().equals(msg) && sme.getUser().equals(event.getUser()),
                (sme) -> {
                    //noinspection ConstantConditions at least one option is required
                    switch (sme.getSelectedOptions().get(0).getValue()) {
                        case "avatar" -> event.getHook().editOriginalEmbeds(Embeds.getAvatarEmbed(target)).setContent("").setActionRows().queue();
                        case "inventory" -> new Inventory().handle(event, target);
                        case "practice" -> {
                            if (finalTarget != event.getUser()) {
                                event.getHook().editOriginal("You cannot use someone else's locks").setEmbeds().setActionRows().queue();
                            } else new Practice().handle(event, profile);
                        }
                        case "toggle" -> {
                            if (finalTarget != event.getUser()) {
                                event.getHook().editOriginal("You cannot edit someone else's settings").setEmbeds().setActionRows().queue();
                            } else new ToggleLvlMsgs(waiter).handle(event, profile);
                        }
                    }
                },
                30L, TimeUnit.SECONDS,
                () -> msg.delete().queue()
            );
        });


    }

    @Override
    public CommandData getCommandData() {
        return new CommandData("profile", "Open the profile menu")
            .addOption(OptionType.USER, "user", "The profile you want to see");
    }
}
