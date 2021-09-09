package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.Logger;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import java.util.concurrent.TimeUnit;

public class ShopCommand implements ICommand {
	EventWaiter waiter;

	public ShopCommand(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event, UserProfile profile) {
		int price = getPrice(profile);
		int exp = profile.getExp();
		int maxCoins = (exp-(exp%10))/10; //round exp to the closest bottom 10
		event.deferReply().queue();
		//noinspection ConstantConditions can't be null, is required
		String item = event.getOption("item").getAsString();
		switch (item) {
			case "coins" -> {
				if (exp < 100) {
					event.getHook().editOriginal("You don't have enough EXP for this").queue();
					return;
				}
				event.getHook().editOriginalFormat("Exchange 100 exp for 10 coins or %d (max) exp for %d coins?", exp, exp*10)
					.setActionRow(
						Button.primary("oneexp", "100xp"),
						Button.secondary("allexp", "Max").withEmoji(Emoji.fromUnicode("⚠")),
						Button.secondary("cancel", "Cancel")
					)
				.queue(msg ->
					waiter.waitForEvent(
						ButtonClickEvent.class,
						(bce) -> bce.getMessageId().equals(msg.getId()) && bce.getUser().equals(event.getUser()),
						(bce) -> resolveClick(event, bce, profile),
						30L, TimeUnit.SECONDS,
						() -> msg.delete().queue()
					)
				);
			}
			case "exp" -> {
				if (profile.getCoins() < 10) {
					event.getHook().editOriginal("You don't have enough coins for this").queue();
					return;
				}
				event.getHook().editOriginalFormat("Exchange 10 coins for 100 exp or %d (max) coins for %d exp?", (exp-(exp%10)), maxCoins)
					.setActionRow(
						Button.primary("onecoin", "100xp"),
						Button.secondary("allcoin", "Max").withEmoji(Emoji.fromUnicode("⚠")),
						Button.secondary("cancel", "Cancel")
					)
					.queue(msg ->
						waiter.waitForEvent(
							ButtonClickEvent.class,
							(bce) -> bce.getMessageId().equals(msg.getId()) && bce.getUser().equals(event.getUser()),
							(bce) -> resolveClick(event, bce, profile),
							30L, TimeUnit.SECONDS,
							() -> msg.delete().queue()
						)
					);
			}
			case "locks" -> {
				event.getHook().editOriginalFormat("Buy a lock for %d coins?", price)
					.setActionRow(
						Button.primary("yes", "Yes"),
						Button.secondary("cancel", "Cancel")
					)
				.queue(msg ->
					waiter.waitForEvent(
						ButtonClickEvent.class,
						(bce) -> bce.getMessage().equals(msg) && bce.getUser().equals(event.getUser()),
						(bce) -> {
							switch (bce.getComponentId()) {
								case "yes" -> {
									profile.adjustLockCount(1);
									profile.adjustCoins(-price);
									profile.update(bce.getMember());
									event.getHook().editOriginalFormat("Bought 1 lock for %d coins", price)
										.setActionRows().queue();
								}
								case "cancel" -> {
									bce.deferEdit().queue();
									msg.delete().queue();
								}
							}
						},
						30L, TimeUnit.SECONDS,
						() -> msg.delete().queue()
					)
				);
			}
			case "book" -> {
				event.getHook().editOriginalFormat("Buy a Textbook for %d coins?", price*2)
					.setActionRow(
						Button.primary("yes", "Yes"),
						Button.secondary("cancel", "Cancel")
					)
					.queue(msg ->
						waiter.waitForEvent(
							ButtonClickEvent.class,
							(bce) -> bce.getMessage().equals(msg) && bce.getUser().equals(event.getUser()),
							(bce) -> {
								switch (bce.getComponentId()) {
									case "yes" -> {
										profile.adjustSkill(2);
										profile.adjustCoins(-(price*2));
										profile.update(bce.getMember());
										event.getHook().editOriginalFormat("Bought 1 textbook for %d coins", price*2)
											.setActionRows().queue();
									}
									case "cancel" -> {
										bce.deferEdit().queue();
										msg.delete().queue();
									}
								}
							},
							30L, TimeUnit.SECONDS,
							() -> msg.delete().queue()
						)
					);
			}
		}
	}

	private void resolveClick(SlashCommandEvent sce, ButtonClickEvent bce, UserProfile profile) {
		bce.deferEdit().queue();
		int exp = profile.getExp();
		int coins = profile.getCoins();
		switch (bce.getComponentId()) {
			case "oneexp" -> { // Exchange 100 exp for 10 coins
				profile.adjustExp(-100);
				profile.adjustCoins(10);
				sce.getHook().editOriginal("Exchanged 100 exp for 10 coins").setActionRows().queue();
			}
			case "allexp" -> { // Exchange max exp for max coins
				int maxExpForCoins = (exp-(exp%10));
				int maxCoinsForExp = (exp-(exp%10))/10; //round exp to the closest bottom 10
				profile.adjustCoins(maxCoinsForExp);
				profile.adjustExp(-maxExpForCoins);
				sce.getHook().editOriginalFormat("Exchanged %d exp for %d coins", maxExpForCoins, maxCoinsForExp)
					.setActionRows().queue();
			}
			case "onecoin" -> { // Exchange 10 coins for 100 exp
				profile.adjustExp(100);
				profile.adjustCoins(-10);
				sce.getHook().editOriginal("Exchanged 10 coins for 100 exp").setActionRows().queue();
			}
			case "allcoin" -> { // Exchange max coins for max exp
				coins = Math.min(coins, 21474836); //Limit exchange to (Integer.MAX_VALUE\100) so that exp becomes Integer.MAX_VALUE
				profile.adjustCoins(-coins);
				profile.adjustExp(coins*100);
				sce.getHook().editOriginalFormat("Exchanged %d coins for %d exp", coins, coins*100).setActionRows().queue();
			}
			case "cancel" -> bce.getMessage().delete().queue();
		}
		profile.update(bce.getMember());
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("shop", "Displays the Shop!")
			.addOptions(
				new OptionData(OptionType.STRING, "item", "What would you like to shop for?", true)
					.addChoice("Coins", "coins")
					.addChoice("Exp", "exp")
					.addChoice("Practice Locks", "locks")
					.addChoice("Textbook (2x the price of locks, guaranteed 2 skill points)", "book")
			);
	}


	private static int getPrice(UserProfile dbMember) {
		int skill = dbMember.getSkill();
		if (isBetween(skill, 1, 10)) return 40;
		else if (isBetween(skill, 11, 20)) return 75;
		else if (isBetween(skill, 21, 30)) return 115;
		else if (isBetween(skill, 31, 40)) return 150;
		else if (isBetween(skill, 41, 50)) return 225;
		else if (isBetween(skill, 51, 60)) return 300;
		else if (isBetween(skill, 61, 70)) return 375;
		else if (isBetween(skill, 71, 80)) return 450;
		else if (isBetween(skill, 81, 90)) return 425;
		else return 600; //if (isBetween(skill, 91, 100))
	}

	private static boolean isBetween(int x, int lower, int upper) {
		return x >= lower && x <=upper;
	}
}
