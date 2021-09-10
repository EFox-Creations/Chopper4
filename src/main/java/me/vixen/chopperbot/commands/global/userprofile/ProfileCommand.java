package me.vixen.chopperbot.commands.global.userprofile;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.util.Random;
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
                    sme.deferEdit().queue();
                    //noinspection ConstantConditions at least one option is required
                    switch (sme.getSelectedOptions().get(0).getValue()) {
                        case "avatar" -> event.getHook().editOriginalEmbeds(Embeds.getAvatarEmbed(target)).setContent("").setActionRows().queue();
                        case "inventory" -> new Inventory().handle(event, target);
                        case "practice" -> {
                            if (finalTarget != event.getUser()) {
                                event.getHook().editOriginal("You cannot use someone else's locks").setEmbeds().setActionRows().queue();
                            } else {
                                int skill = profile.getSkill();
                                int skillIncrease = 0;
                                int usedLocks = 0;
                                for (int i = 1; i <= profile.getLockCount(); i++ , usedLocks++, profile.adjustLockCount(-1)) {
                                    int rand = skill+skillIncrease < 10 ? new Random().nextInt(10)+1 : new Random().nextInt(100)+1;
                                    if (skill > rand) {
                                        profile.adjustSkill(1);
                                        skillIncrease++;
                                    }
                                }
                                profile.update(event.getMember());
                                event.getHook().editOriginalEmbeds(
                                    new EmbedBuilder()
                                        .setColor(skillIncrease > 0 ? Color.GREEN : Color.YELLOW)
                                        .setTitle("Practice Lock Results")
                                        .setDescription(String.format("Used %d locks!\nSkill: %d -> %d", usedLocks, skill, skill+skillIncrease))
                                        .build()
                                ).setActionRows().setContent("").queue();
                            }
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
