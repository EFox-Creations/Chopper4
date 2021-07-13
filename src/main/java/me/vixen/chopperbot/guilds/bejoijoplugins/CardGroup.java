package me.vixen.chopperbot.guilds.bejoijoplugins;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.tools.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CardGroup implements ICommand {

	private final int cardCost = 200;
	private final EventWaiter waiter;
	public CardGroup(EventWaiter waiter) {
		this.waiter = waiter;
	}

	@Override
	public void handle(SlashCommandEvent event) {
		final String subcommandName = event.getSubcommandName();
		//noinspection ConstantConditions cant be null
		switch (subcommandName) {
			case "buy" -> buyCard(event);
			case "sell_single" -> sellCard(event);
			case "view" -> showCollection(event);
			case "inspect" -> showCard(event);
			case "sell_set" -> sellSet(event);
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("card", "Card Commands").addSubcommands(
			new SubcommandData("view", "Show a card collection")
				.addOption(OptionType.USER, "user", "The owner of the collection", true),
			new SubcommandData("buy", "Buy a card for " + cardCost + " coins"),
			new SubcommandData("sell_single", "Sell a card")
				.addOption(OptionType.INTEGER, "cardid", "The id of the card you want to sell", true),
			new SubcommandData("inspect", "Views a card!")
				.addOption(OptionType.INTEGER, "cardid", "The id# of the card you want to view!", true),
			new SubcommandData("sell_set", "Sell a complete set of cards")
				.addOptions(new OptionData(OptionType.STRING, "facename", "Which set do you want to sell?", true)
					.addChoices(getChoices()))
		);
	}

	private List<Command.Choice> getChoices() {
		List<Command.Choice> choices = new ArrayList<>();
		for (Card.CardFace face : Card.CardFace.values()) {
			choices.add(new Command.Choice(String.valueOf(face), String.valueOf(face)));
		}
		return choices;
	}

	private Color getRarityColor(Card card) {
		switch (card.getRarity()) {
			case MYTHIC -> {
				return Color.YELLOW;
			}
			case LEGENDARY -> {
				return Color.MAGENTA;
			}
			case RARE -> {
				return Color.ORANGE;
			}
			case UNCOMMON -> {
				return Color.BLUE;
			}
			case COMMON -> {
				return Color.WHITE;
			}
			default -> {
				return Color.BLACK;
			}
		}
	}

	private void showCard(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		final int cardId = (int) event.getOption("cardid").getAsLong();
		final Card card = Database.getCardById(cardId);
		if (card == null) {
			event.reply("Couldn't find that card").setEphemeral(true).queue();
			return;
		}

		final String memberId = Card.CardFace.getId(card.getFace());

		//noinspection ConstantConditions cant be null
		event.getGuild().retrieveMemberById(memberId).queue(member -> {
			if (member == null) return;
			final File file = card.getGraphic(member.getUser());
			if (file == null) {
				event.reply("Forgot how to draw!").queue();
			} else {
				event.reply("Card Shown!").setEphemeral(true).queue();
				//noinspection ConstantConditions handled already
				event.getTextChannel()
					.sendFile(file)
					.append(event.getMember().getAsMention())
					.queue(onSuccess -> file.delete());
			}
		});

	}


	private void buyCard(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An unknown error occurred; aborting with Error Code BCC1").queue();
			return;
		}
		if (dbMember.getCoins() < cardCost) {
			event.replyEmbeds(Embeds.getInsufficientCoins()).setEphemeral(true).queue();
			return;
		}

		event.reply("Follow instructions below").setEphemeral(true).queue();
		event.getTextChannel().sendMessage("Confirm buy card for " + cardCost + " coins?").queue(message -> {
			message.addReaction("✅").queue();
			message.addReaction("⛔").queue();
			waiter.waitForEvent(
				GuildMessageReactionAddEvent.class,
				e -> isSameMessageAndUser(event, e, message),
				e -> confirmBuy(event, e, message)
			);
		});
	}

	private void confirmBuy(SlashCommandEvent event, GuildMessageReactionAddEvent e, Message msg) {
		//noinspection ConstantConditions cant be null
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An unknown error occurred; aborting with Error Code SCC2").queue();
			return;
		}
		if (e.getReactionEmote().getName().equalsIgnoreCase("⛔")) {
			msg.delete().queue();
			event.getHook().editOriginal("Buy card canceled").queue();
		} else if (e.getReactionEmote().getName().equalsIgnoreCase("✅")) {
			dbMember.adjustCoins(cardCost);
			dbMember.update();
			//noinspection ConstantConditions cant be null
			final String userId = event.getMember().getId();

			Card drawnCard = new Card();
			final boolean success = Database.addCard(userId, drawnCard);

			if (success) {
				MessageEmbed embed = new EmbedBuilder()
					.setColor(getRarityColor(drawnCard))
					.setTitle(drawnCard.getRarity() + " " + drawnCard.getFaceAsString())
					.build();
				msg.clearReactions().queue();
				msg.editMessageEmbeds(embed).queue();
			} else {
				msg.editMessage("An Error Occurred").queue();
			}
		}
	}

	private void sellCard(SlashCommandEvent event) {
		int mythicPrice = 750;
		int legendaryPrice = 400;
		int rarePrice = 200;
		int uncommonPrice = 100;
		int commonPrice = 50;

		//noinspection ConstantConditions cant be null
		final int cardid = (int) event.getOption("cardid").getAsLong();

		final Card card = Database.getCardById(cardid);
		if (card == null) {
			event.reply("An unknown error occurred; aborting with Error Code CCG0").queue();
			return;
		}
		final Card.Rarity rarity = card.getRarity();

		if (!Database.doesUserOwnCard(event.getUser().getId(), cardid)) {
			event.reply("You do not own this card").setEphemeral(true).queue();
			return;
		}

		event.reply("Follow instructions below").setEphemeral(true).queue();
		final TextChannel channel = event.getTextChannel();
		switch (rarity) {
			case MYTHIC ->
				channel.sendMessage("Sell card for " + mythicPrice + " coins?")
					.queue(msg -> waitForConfirm(event, msg, mythicPrice, cardid));
			case LEGENDARY ->
				channel.sendMessage("Sell card for " + legendaryPrice + " coins?")
					.queue(msg -> waitForConfirm(event, msg, legendaryPrice, cardid));
			case RARE ->
				channel.sendMessage("Sell card for " + rarePrice + " coins?")
					.queue(msg -> waitForConfirm(event, msg, rarePrice, cardid));
			case UNCOMMON ->
				channel.sendMessage("Sell card for " + uncommonPrice + " coins?")
					.queue(msg -> waitForConfirm(event, msg, uncommonPrice, cardid));
			case COMMON ->
				channel.sendMessage("Sell card for " + commonPrice + " coins?")
					.queue(msg -> waitForConfirm(event, msg, commonPrice, cardid));
		}
	}

	private void waitForConfirm(SlashCommandEvent event, Message msg, int price, int cardId) {
		msg.addReaction("✅").queue();
		msg.addReaction("❌").queue();
		waiter.waitForEvent(
			GuildMessageReactionAddEvent.class,
			e -> isSameMessageAndUser(event, e, msg),
			e -> confirmSale(msg, e, price, cardId)
		);
	}

	private boolean isSameMessageAndUser(SlashCommandEvent event, GuildMessageReactionAddEvent e, Message msg) {
		//noinspection ConstantConditions cant be null
		return e.getMessageId().equals(msg.getId())
			&& event.getMember().getId().equals(e.getMember().getId());
	}

	private void confirmSale(Message msg, GuildMessageReactionAddEvent e, int price, int cardId) {
		DBMember dbMember = Database.getMember(e.getGuild(), e.getUser().getId());
		if (dbMember == null) {
			e.getChannel().sendMessage("An unknown error occurred; aborting with Error Code CGC4").queue();
			return;
		}
		switch (e.getReactionEmote().getName()) {
			case "✅" -> {
				dbMember.adjustCoins(price);
				dbMember.update();
				final boolean success = Database.deleteCard(cardId);
				msg.clearReactions().queue();
				if (success) {
					msg.editMessage("Card sold for " + price + " coins").queue();
				}
				else {
					msg.editMessage("An error occurred").queue();
				}
			}
			case "🚫" -> msg.delete().queue();
		}
	}

	private void showCollection(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		final Member member = event.getOption("user").getAsMember();
		if (member == null) {
			event.reply("This member is not in this guild").setEphemeral(true).queue();
			return;
		}
		final List<Card> ownedCards = Database.getOwnedCards(member.getId());

		if (ownedCards.isEmpty()) {
			event.reply("This user owns no cards").queue();
			return;
		}
		String[] cardNames = new String[ownedCards.size()];
		for (int i=0; i < ownedCards.size(); i++) {
			cardNames[i] = ownedCards.get(i).toString();
		}
		//noinspection ConstantConditions cant be null
		Paginator pager = new Paginator.Builder()
			.setEventWaiter(waiter)
			.setTimeout(20L, TimeUnit.SECONDS)
			.setText(event.getMember().getAsMention())
			.setItems(cardNames)
			.setBulkSkipNumber(5)
			.allowTextInput(false)
			.setColor(Color.CYAN)
			.setColumns(1)
			.setItemsPerPage(10)
			.showPageNumbers(true)
			.waitOnSinglePage(true)
			.useNumberedItems(false)
			.wrapPageEnds(true)
			.addUsers(event.getUser())
			.build();
		pager.paginate(event.getTextChannel(), 1);
		event.reply("Collection Displayed").setEphemeral(true).queue();
	}

	private void sellSet(SlashCommandEvent event) {
		event.deferReply().queue();
		//noinspection ConstantConditions cant be null
		final String facename = event.getOption("facename").getAsString();
		final Card.CardFace cardFace = Card.CardFace.valueOf(facename);
		final List<Card> cards = Database.getCardsByFaceWithOwner(cardFace, event.getUser().getId());
		if (cards == null) {
			event.reply("An unknown error occurred; aborting with Error Code CGC5").queue();
			return;
		}

		boolean mythicFlag = false, legendaryFlag = false, rareFlag = false, uncommonFlag = false, commonFlag = false;
		int mythicId = -1, legendaryId = -1, rareId = -1, uncommonId = -1, commonId = -1;

		for (Card card : cards) {
			if (card.getRarity().equals(Card.Rarity.MYTHIC) && !mythicFlag) {
				mythicFlag = true;
				mythicId = card.getId();
			}
			else if (card.getRarity().equals(Card.Rarity.LEGENDARY) && !legendaryFlag) {
				legendaryFlag = true;
				legendaryId = card.getId();
			}
			else if (card.getRarity().equals(Card.Rarity.RARE) && !rareFlag) {
				rareFlag = true;
				rareId = card.getId();
			}
			else if (card.getRarity().equals(Card.Rarity.UNCOMMON) && !uncommonFlag) {
				uncommonFlag = true;
				uncommonId = card.getId();
			}
			else if (card.getRarity().equals(Card.Rarity.COMMON) && !commonFlag) {
				commonFlag = true;
				commonId = card.getId();
			}
		}
		if (mythicFlag && legendaryFlag && rareFlag && uncommonFlag && commonFlag) {
			event.getHook().editOriginal("Cashed out! Received 2000 coins!").queue();
			event.getTextChannel().sendFile(new File("CardBlanks/CashOut.png")).queue();
			//noinspection ConstantConditions cant be null
			DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
			if (dbMember == null) {
				event.reply("An unknown error occurred; aborting with Error Code SCC3").queue();
				return;
			}
			dbMember.adjustCoins(2000);
			dbMember.update();
			List<Integer> cardIds = List.of(mythicId, legendaryId, rareId, uncommonId, commonId);
			Database.deleteCards(cardIds);
		} else {
			event.getHook().editOriginal("You do not have a complete set!").queue();
		}
	}
}
