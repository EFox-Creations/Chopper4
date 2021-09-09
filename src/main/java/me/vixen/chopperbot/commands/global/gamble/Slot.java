package me.vixen.chopperbot.commands.global.gamble;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Slot {
	final int MAX_BET = 134217727;

	public void handle(SlashCommandEvent event, UserProfile profile) {
		String id = event.getUser().getId();
		Guild guild = event.getGuild();
		Guild efox = Entry.getJDA().getGuildById("882694324112994315");
		if (efox == null) {
			event.getHook().editOriginal("An error occurred; aborting with Code " + Errors.JDANULLRETURN).setActionRows().queue();
			return;
		}
		//noinspection ConstantConditions cant be null
		UserProfile member = Database.getMember(guild, id);
		if (member == null) {
			event.getHook().editOriginal("An error occurred; aborting with Code " + Errors.DBNULLRETURN).setActionRows().queue();
			return;
		}
		//noinspection ConstantConditions cant be null
		final int bet = (int) event.getOption("bet").getAsLong();
		if (member.getCoins() < bet) {
			event.getHook().editOriginalEmbeds(Embeds.getInsufficientCoins()).setActionRows().queue();
			return;
		} else if (bet > MAX_BET) {
			event.getHook().editOriginal("The maximum bet allowed is: " + MAX_BET).setActionRows().queue();
			return;
		} else if (bet <= 0) {
			event.getHook().editOriginal("Your bet must be greater than 0").setActionRows().queue();
			return;
		}

		final Emote slot = efox.getEmotesByName("slot", true).get(0);


		member.adjustCoins(-bet);
		event.getHook().editOriginalFormat("%s%s%s", slot, slot, slot).setActionRows().queue(msg -> {
			SlotResult s1 = SlotResult.getRandom();
			SlotResult s2 = SlotResult.getRandom();
			SlotResult s3 = SlotResult.getRandom();

			Emote slotone = getEmote(efox, s1);
			Emote slottwo = getEmote(efox, s2);
			Emote slotthree = getEmote(efox, s3);

			msg.editMessageFormat("%s%s%s", slotone, slot, slot)
				.queueAfter(1L, TimeUnit.SECONDS, unused -> msg.editMessageFormat("%s%s%s", slotone, slottwo, slot).queueAfter(1L, TimeUnit.SECONDS, unused1 -> {
				int payout = 0;
				if (allMatch(s1, s2, s3)) {
					switch (getRank(s1, s2, s3)) {
						case slotdiamond -> payout = bet * 16;
						case slotseven -> payout = bet * 14;
						case slotbar -> payout = bet * 12;
						case slotheart -> payout = bet * 10;
						case slotbell -> payout = bet * 8;
						case slothorseshoe -> payout = bet * 6;
						case slotcherry, slotmelon, slotlemon -> payout = bet * 2;
					}
				} else if (matchesWithWildcards(s1, s2, s3)) {
					payout = (int) Math.floor(bet * 1.5);
				}

				int net = payout - bet;
				int finalPayout = payout;
				msg.editMessageFormat("%s%s%s", slotone, slottwo, slotthree).queueAfter(1L, TimeUnit.SECONDS, unused2 -> {
					msg.editMessageEmbeds(
						new EmbedBuilder()
							.setColor(finalPayout > 0 ? Color.GREEN : Color.RED)
							.setTitle(String.format("%s%s%s", slotone, slottwo, slotthree))
							.setDescription(String.format("Payout: %d\sBet: %d\sNet: %d", finalPayout, bet, net))
							.build()
					).override(true).queueAfter(1L, TimeUnit.SECONDS);
					member.adjustCoins(finalPayout);
					member.update(null);

					if (finalPayout < 0) Database.addToPot(finalPayout*-1);
				});

			}));

		});
	}

	private Emote getEmote(Guild efox, SlotResult slot) {
		final Emote diamond = efox.getEmotesByName("slotdiamond", true).get(0);
		final Emote seven = efox.getEmotesByName("slotseven", true).get(0);
		final Emote bar = efox.getEmotesByName("slotbar", true).get(0);
		final Emote heart = efox.getEmotesByName("slotheart", true).get(0);
		final Emote bell = efox.getEmotesByName("slotbell", true).get(0);
		final Emote horseshoe = efox.getEmotesByName("slothorseshoe", true).get(0);
		final Emote cherry = efox.getEmotesByName("slotcherry", true).get(0);
		final Emote melon = efox.getEmotesByName("slotmelon", true).get(0);
		final Emote lemon = efox.getEmotesByName("slotlemon", true).get(0);

		switch (slot) {
			case slothorseshoe -> {
				return horseshoe;
			}
			case slotbell -> {
				return bell;
			}
			case slotbar -> {
				return bar;
			}
			case slotheart -> {
				return heart;
			}
			case slotlemon -> {
				return lemon;
			}
			case slotmelon -> {
				return melon;
			}
			case slotseven -> {
				return seven;
			}
			case slotcherry -> {
				return cherry;
			}
			case slotdiamond -> {
				return diamond;
			}
			default -> {
				return null;
			}
		}
	}

	private boolean allMatch(SlotResult s1, SlotResult s2, SlotResult s3) {
		return s1.toString().equals(s2.toString()) && s2.toString().equals(s3.toString());
	}

	@SuppressWarnings("DuplicateExpressions")
	private boolean matchesWithWildcards(SlotResult s1, SlotResult s2, SlotResult s3) {
		if (s1.toString().equals(s2.toString()) && s3.toString().equals(SlotResult.slotdiamond.toString())) //1 and 2 with 3 diamond
			return true;
		else if (s1.toString().equals(s3.toString()) && s2.toString().equals(SlotResult.slotdiamond.toString())) //1 and 3 with 2 diamond
			return true;
		else if (s2.toString().equals(s3.toString()) && s1.toString().equals(SlotResult.slotdiamond.toString())) //2 and 3 with 1 diamond
			return true;
		else if (s1.toString().equals(s2.toString()) && s1.toString().equals(SlotResult.slotdiamond.toString())) //1 and 2 are both wild
			return true;
		else //2 and 3 are both wild
			if (s1.toString().equals(s3.toString()) && s1.toString().equals(SlotResult.slotdiamond.toString())) //1 and 3 are both wild
			return true;
		else return s2.toString().equals(s3.toString()) && s2.toString().equals(SlotResult.slotdiamond.toString());
	}

	private SlotResult getRank(SlotResult s1, SlotResult s2, SlotResult s3) {
		if (allMatch(s1, s2, s3)) return s1; //if they are all the same, return s1
		if (s1.toString().equals(SlotResult.slotdiamond.toString())) { //if s1 is a wildcard, check s2
			if (s2.toString().equals(SlotResult.slotdiamond.toString())) { //if s2 is a wildcard also, return s3
				return s3;
			} else return s2; //if s2 is not a wildcard
		} else return s1; //if s1 is not a wildcard
	}

	enum SlotResult {
		slotseven,
		slotmelon,
		slotcherry,
		slotdiamond,
		slothorseshoe,
		slotlemon,
		slotbell,
		slotheart,
		slotbar;

		public static SlotResult getRandom() {
			return values()[new Random().nextInt(values().length)];
		}
	}
}
