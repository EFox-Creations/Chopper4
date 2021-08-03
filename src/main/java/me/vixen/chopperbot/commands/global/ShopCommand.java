package me.vixen.chopperbot.commands.global;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.DBMember;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import java.util.concurrent.TimeUnit;

public class ShopCommand implements ICommand {
	EventWaiter waiter;

	public ShopCommand(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		int price = getPrice(dbMember);

		SelectionMenu menu = SelectionMenu.create("menu:shop")
			.setPlaceholder("Choose your item!")
			.setRequiredRange(1, 1)
			.addOption("Coins", "coins", "Trade some EXP for coins", Emoji.fromUnicode("💰"))
			.addOption("Exp", "exp", "Buy some exp!", Emoji.fromUnicode("📈"))
			.addOption("Practice Lock", "lock", "Buy a practice lock for " + price + " coins!", Emoji.fromUnicode("🔐"))
			.addOption("Textbook", "book", "Guarantees a skill increase for " + price*2 + " coins!", Emoji.fromUnicode("📚"))
			.build();

		//noinspection ConstantConditions wont be null
		event.reply("Choose your item!").addActionRow(menu).queue(hook ->
			hook.retrieveOriginal().queue(msg ->
				waiter.waitForEvent(SelectionMenuEvent.class,
					e -> e.getMessage().getId().equals(msg.getId()) && e.getUser().equals(event.getUser()),
					this::resolveSelection,
					1L, TimeUnit.MINUTES,
					() -> {
						msg.delete().queue();
						msg.getChannel().sendMessage("Shop Timed Out").mention(event.getUser()).queue();
					}
				)
			)
		);
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("shop", "Displays the Shop!");
	}

	private void resolveSelection(SelectionMenuEvent event) {
		event.getHook().deleteOriginal().queue();
		String selection = event.getValues().stream().findFirst().orElse(null);
		if (selection == null) {
			event.reply("An unknown error occurred; aborting with ErrorCode: `Shop01`").queue();
			return;
		}
		switch (selection) {
			case "exp" -> event.reply("Exchange how many coins?").addActionRow(
				Button.primary("allcoins", "Max"),
				Button.secondary("customcoins", "Custom")
			).queue(hook ->
				hook.retrieveOriginal().queue(msg ->
					waiter.waitForEvent(
						ButtonClickEvent.class,
						e -> e.getUser().equals(event.getUser()) && event.getMessageId().equals(e.getMessageId()),
						this::resolveClick
					)
				)
			);
			case "coins" -> event.reply("Exchange how much exp?").addActionRow(
				Button.primary("allexp", "Max"),
				Button.secondary("customexp", "Custom")
			).queue(hook ->
				hook.retrieveOriginal().queue(msg ->
					waiter.waitForEvent(
						ButtonClickEvent.class,
						e -> e.getUser().equals(event.getUser()) && event.getMessageId().equals(e.getMessageId()),
						this::resolveClick
					)
				)
			);
			case "lock" -> {
				//noinspection ConstantConditions cant be null
				DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
				if (dbMember == null) {
					event.reply("An unknown error occurred; aborting with Error Code ShC2").queue();
					return;
				}
				if (dbMember.getCoins() < getPrice(dbMember)) {
					event.replyEmbeds(Embeds.getInsufficientCoins()).queue();
					return;
				}
				dbMember.adjustLockCount(1);
				dbMember.adjustCoins(-1 * getPrice(dbMember));
				dbMember.update();
			}
			case "book" -> {
				//noinspection ConstantConditions cant be null
				DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
				if (dbMember == null) {
					event.reply("An unknown error occurred; aborting with Error Code ShC3").queue();
					return;
				}
				if (dbMember.getCoins() < getPrice(dbMember)*2) {
					event.replyEmbeds(Embeds.getInsufficientCoins()).queue();
					return;
				}
				dbMember.adjustSkill(2);
				dbMember.adjustCoins(-1 * getPrice(dbMember));
				dbMember.update();
				event.reply("Skill increased by 2!").queue();
			}
		}
	}

	private void resolveClick(ButtonClickEvent event) {
		//noinspection ConstantConditions cant be null
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An unknown error occurred; aborting with Error Code ShC4").queue();
			return;
		}
		switch (event.getComponentId()) {
			case "allcoins" -> { //buying max amount of exp
				int availableCoins = dbMember.getCoins();
				int maxExp = availableCoins*10;
				dbMember.adjustCoins(-1 * availableCoins);
				dbMember.adjustExp(maxExp);
				dbMember.update();
				event.reply(String.format("Exchanged %d coins for %d exp", availableCoins, maxExp)).queue();
			}
			case "customcoins" -> //buying custom amount of exp
				event.getChannel().sendMessage("How many coins to exchange?\n1 Coin = 10 Exp\n(Type in amount)").queue(msg -> waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> e.getAuthor().equals(event.getUser()) && e.getMessageId().equals(event.getMessageId()),
					e -> {
						try {
							int coinsForExchange = Integer.parseInt(e.getMessage().getContentRaw());
							int availableCoins = dbMember.getCoins();
							if (coinsForExchange > availableCoins) {
								event.replyEmbeds(Embeds.getInsufficientCoins()).queue();
								return;
							}
							dbMember.adjustExp(coinsForExchange*10);
							dbMember.adjustCoins(-1 * coinsForExchange);
							dbMember.update();
							event.reply(String.format("Exchanged %d coins for %d exp", coinsForExchange, coinsForExchange*10)).queue();
						} catch (NumberFormatException t) {
							event.reply("Invalid number entered; aborting").queue();
						}
					},
					1L, TimeUnit.MINUTES,
					() -> {
						msg.delete().queue();
						msg.getChannel().sendMessage("Timed out waiting").queue();
					}
				));
			case "allexp" -> { //buying max amount of coins
				int availableExp = dbMember.getExp();
				int maxCoins = (availableExp-(availableExp%10))/10;
				dbMember.adjustCoins(maxCoins);
				dbMember.adjustExp(-1 * (maxCoins*10));
				dbMember.update();
				event.reply(String.format("Exchanged %d exp for %d coins", availableExp-(availableExp%10), maxCoins)).queue();
			}
			case "customexp" -> //buying custom amount of coins
				event.getChannel().sendMessage("How much exp to exchange?\n1 Coin = 10 Exp\n(Type in amount)").queue(msg -> waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> e.getAuthor().equals(event.getUser()) && e.getMessageId().equals(event.getMessageId()),
					e -> {
						try {
							int expForExchange = Integer.parseInt(e.getMessage().getContentRaw());
							int availableExp = dbMember.getCoins();
							if (expForExchange > availableExp) {
								event.replyEmbeds(Embeds.getInsufficientExp()).queue();
								return;
							}
							dbMember.adjustExp(-1 * expForExchange);
							dbMember.adjustCoins(expForExchange/10);
							dbMember.update();
							event.reply(String.format("Exchanged %d exp for %d coins", expForExchange, expForExchange/10)).queue();
						} catch (NumberFormatException t) {
							event.reply("Invalid number entered; aborting").queue();
						}
					},
					1L, TimeUnit.MINUTES,
					() -> {
						msg.delete().queue();
						msg.getChannel().sendMessage("Timed out waiting").queue();
					}
				));
		}
	}


	private static int getPrice(DBMember dbMember) {
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
