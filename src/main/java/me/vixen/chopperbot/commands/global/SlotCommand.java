package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.Database.DBMember;
import me.vixen.chopperbot.Database.Database;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.ICommand;
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

public class SlotCommand implements ICommand {
	final int MAX_BET = 134217727;

	@Override
	public void handle(SlashCommandEvent event) {
		String id = event.getUser().getId();
		Guild guild = event.getGuild();
		Guild efox = Entry.jda.getGuildById("761703507546996786");
		if (efox == null) {
			event.reply("An error occurred; aborting with Code " + Errors.JDANULLRETURN).queue();
			return;
		}
		//noinspection ConstantConditions cant be null
		DBMember member = Database.getMember(guild, id);
		if (member == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		//noinspection ConstantConditions cant be null
		final int bet = (int) event.getOption("bet").getAsLong();
		if (member.getCoins() < bet) {
			event.replyEmbeds(Embeds.getInsufficientCoins()).queue();
			return;
		} else if (bet > MAX_BET) {
			event.reply("The maximum bet allowed is: " + MAX_BET).queue();
			return;
		} else if (bet <= 0) {
			event.reply("Your bet must be greater than 0").setEphemeral(true).queue();
			return;
		}

		final Emote slot = efox.getEmotesByName("slot", true).get(0);


		member.adjustCoins(-bet);
		event.replyFormat("%s%s%s", slot, slot, slot).queue(hook -> {
			Slot s1 = Slot.getRandom();
			Slot s2 = Slot.getRandom();
			Slot s3 = Slot.getRandom();

			Emote slotone = getEmote(efox, s1);
			Emote slottwo = getEmote(efox, s2);
			Emote slotthree = getEmote(efox, s3);

			hook.editOriginalFormat("%s%s%s", slotone, slot, slot).queueAfter(1L, TimeUnit.SECONDS, unused -> hook.editOriginalFormat("%s%s%s", slotone, slottwo, slot).queueAfter(1L, TimeUnit.SECONDS, unused1 -> {
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
				hook.editOriginalFormat("%s%s%s", slotone, slottwo, slotthree).queueAfter(1L, TimeUnit.SECONDS, unused2 -> {
					hook.editOriginalEmbeds(
						new EmbedBuilder()
							.setColor(finalPayout > 0 ? Color.GREEN : Color.RED)
							.setTitle(String.format("%s%s%s", slotone, slottwo, slotthree))
							.setDescription(String.format("Payout: %d\sBet: %d\sNet: %d", finalPayout, bet, net))
							.build()
					).setContent("").queueAfter(1L, TimeUnit.SECONDS);
					member.adjustCoins(finalPayout);
					member.update();
				});

			}));

		});
	}

	private Emote getEmote(Guild efox, Slot slot) {
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

	private boolean allMatch(Slot s1, Slot s2, Slot s3) {
		return s1.toString().equals(s2.toString()) && s2.toString().equals(s3.toString());
	}

	@SuppressWarnings("DuplicateExpressions")
	private boolean matchesWithWildcards(Slot s1, Slot s2, Slot s3) {
		if (s1.toString().equals(s2.toString()) && s3.toString().equals(Slot.slotdiamond.toString())) //1 and 2 with 3 diamond
			return true;
		else if (s1.toString().equals(s3.toString()) && s2.toString().equals(Slot.slotdiamond.toString())) //1 and 3 with 2 diamond
			return true;
		else if (s2.toString().equals(s3.toString()) && s1.toString().equals(Slot.slotdiamond.toString())) //2 and 3 with 1 diamond
			return true;
		else if (s1.toString().equals(s2.toString()) && s1.toString().equals(Slot.slotdiamond.toString())) //1 and 2 are both wild
			return true;
		else //2 and 3 are both wild
			if (s1.toString().equals(s3.toString()) && s1.toString().equals(Slot.slotdiamond.toString())) //1 and 3 are both wild
			return true;
		else return s2.toString().equals(s3.toString()) && s2.toString().equals(Slot.slotdiamond.toString());
	}

	private Slot getRank(Slot s1, Slot s2, Slot s3) {
		if (allMatch(s1, s2, s3)) return s1; //if they are all the same, return s1
		if (s1.toString().equals(Slot.slotdiamond.toString())) { //if s1 is a wildcard, check s2
			if (s2.toString().equals(Slot.slotdiamond.toString())) { //if s2 is a wildcard also, return s3
				return s3;
			} else return s2; //if s2 is not a wildcard
		} else return s1; //if s1 is not a wildcard
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("slot", "Play a slot machine!")
			.addOption(OptionType.INTEGER, "bet", "How much to bet?", true);
	}

	enum Slot {
		slotseven,
		slotmelon,
		slotcherry,
		slotdiamond,
		slothorseshoe,
		slotlemon,
		slotbell,
		slotheart,
		slotbar;

		public static Slot getRandom() {
			return values()[new Random().nextInt(values().length)];
		}
	}
}
