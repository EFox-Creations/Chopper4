package me.vixen.chopperbot.guilds.bejoijoplugins;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.vixen.chopperbot.database.DBMember;
import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.Entry;
import me.vixen.chopperbot.commands.GlobalCommandManager;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.guilds.IGuild;
import me.vixen.chopperbot.listener.DefaultEventHandler;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BejoIjoPlugins implements IGuild {

	private final String guildId;
	protected static final int COLOR_COST = 200;
	protected static final int ROLE_COST = 150;
	private static List<ICommand> localCommands;
	public BejoIjoPlugins(String guildId, EventWaiter waiter) {
		this.guildId = guildId;
		setLocalCommands(waiter);
	}

	@Override
	public void setLocalCommands(EventWaiter waiter) {
		localCommands = List.of(
			new CardGroup(waiter),
			new ConvertCommand(waiter),
			new FindCommand(waiter),
			new PatreonCommand(),
			new UpdateTimeCommand(),
			new WhoIsCommand(waiter),
			new BuyGroup(waiter),
			new SuggestCommand()
		);
	}

	@Override
	public TextChannel getLottoChannel() {
		return getGuild().getTextChannelById("784226194664456202");
	}

	@Override
	public List<ICommand> getLocalCommands() {
		return localCommands;
	}

	@Override
	public String getId() {
		return guildId;
	}

	@Override
	public List<TextChannel> getTreasureChannels() {
		List<String> whitelist = List.of(
			"663796410130628641", "692962949295243297", "671729684693778483",
			"696220432592011336", "663796646878117908", "698985553986584686"
		);

		return getGuild().getTextChannels().stream()
			.filter(it -> whitelist.contains(it.getId()))
			.collect(Collectors.toList()
			);
	}

	@Override
	public void handleSlashCommand(SlashCommandEvent event, EventWaiter waiter, GlobalCommandManager cManager) {
		boolean found = false;
		for (ICommand c : getLocalCommands()) {
			if (c.getName().equals(event.getName())) {
				c.handle(event);
				found = true;
			}
		}
		if (!found) DefaultEventHandler.handleSlashCommand(event, cManager);
		//This guild wants slash command logs
		//noinspection ConstantConditions cant be null
		TextChannel logs = event.getGuild().getTextChannelById("672112948776534049");
		if (logs != null)
			logs.sendMessageEmbeds(Embeds.getSlashCommandLogEmbed(event)).queue();
	}

	@Override
	public void handleGMsgReceived(GuildMessageReceivedEvent event, EventWaiter waiter) {
		final Message message = event.getMessage();
		if (event.getChannel().getId().equals("671729684693778483")) { //If in gallery
			final DBMember member = Database.getMember(event.getGuild(), event.getAuthor().getId());
			Role ss = event.getGuild().getRoleById("781608704633471036");
			if (member != null && !event.getMember().getRoles().contains(ss)
				&& !event.getAuthor().getId().equals(Entry.CREATOR_ID)) {
				//Delete message if it is not a link, attachment, or the user has exceeded the limit
				if (member.getGalleryImgsLeft() <= 0 ||
					(message.getAttachments().isEmpty() && !message.getContentRaw().contains("https://"))) {
					message.delete().queue(unused ->
						event.getChannel().sendMessageEmbeds(Embeds.getGalleryRestrict()).append(event.getAuthor().getAsMention()).queue(msg ->
							msg.delete().queueAfter(5L, TimeUnit.SECONDS))
					);
				} else {
					member.adjustGalleryImgsLeft(message.getAttachments().isEmpty() ? -1 : -1*message.getAttachments().size());
					member.update();
				}
			}
		}
		DefaultEventHandler.handleGMsgReceived(event); //update stickies and award exp
	}

	@SuppressWarnings("SwitchStatementWithTooFewBranches") //may add more in future
	@Override
	public void handleGMsgReactAdd(GuildMessageReactionAddEvent event, EventWaiter waiter) {
		//IGNORE BOTS
		if (event.getUser().isBot()) return;

		List<String> adminOnlyEmotes = List.of("heisenberg", "choppereyes", "🐛");

		final MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();
		switch (event.getChannel().getId()) {
			case "663796720173580317" -> { //#idea-dump
				switch (reactionEmote.getName()) {
					case "⬆" -> checkVotes(event, reactionEmote.getName());
					case "🐛" -> { //only staff may use these
						if (isStaff(getGuild(), event.getMember()))
							sendToBugChannel(event);
						else {
							event.getReaction().removeReaction(event.getUser()).queue();
						}
					}
					case "🔘" -> { //only staff may use these
						if (isStaff(getGuild(), event.getMember()))
							promote(event.getChannel(), event.getMessageId());
						else {
							event.getReaction().removeReaction(event.getUser()).queue();
						}
					}
					case "🚫" -> { //only staff may use these
						if (isStaff(getGuild(), event.getMember()))
							event.retrieveMessage().queue(msg-> msg.delete().queue());
						else {
							event.getReaction().removeReaction(event.getUser()).queue();
						}
					}
					case "choppereyes", "heisenberg" -> { //only staff may use these
						if (!isStaff(getGuild(), event.getMember()) && !event.getUser().isBot())
							event.getReaction().removeReaction(event.getUser()).queue();
					}
					default -> event.getReaction().removeReaction(event.getUser()).queue(); //disallow all other emotes in this channel
				}
			}
			default -> { //global
				//If user is NOT staff and used an admin only emote, remove it
				if (!isStaff(event.getGuild(), event.getMember()) && adminOnlyEmotes.contains(reactionEmote.getName()))
					event.getReaction().removeReaction(event.getUser()).queue();
					//if a staff member marked something as a bug, send it to bugs channel
				else if (isStaff(event.getGuild(), event.getMember()) && reactionEmote.getName().equals("🐛"))
					sendToBugChannel(event);
			}
		}
		DefaultEventHandler.updateStickies(event.getChannel());
	}

	@Override
	public void handleGMemRemove(GuildMemberRemoveEvent event, EventWaiter waiter) {
		TextChannel welcome = event.getGuild().getTextChannelById("678667071437144151");
		if (welcome != null)
			welcome.sendMessageEmbeds(Embeds.getLeaveEmbed(event.getUser())).queue();
	}

	@Override
	public boolean hasCustomClaims() {
		return true;
	}

	@Override
	public void getCustomClaim(SlashCommandEvent event) {
		//noinspection ConstantConditions cant be null
		DBMember dbMember = Database.getMember(event.getGuild(), event.getUser().getId());
		if (dbMember == null) {
			event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
			return;
		}
		event.deferReply().queue();
		final String userId = event.getUser().getId();
		final int dailyChestCount = dbMember.getDailyChests();
		if (dailyChestCount <= 0) {
			event.getHook().editOriginalEmbeds(Embeds.getAlreadyClaimed()).queue();
			return;
		}

		EmbedBuilder eb = new EmbedBuilder().setColor(Color.GREEN).setTitle("Daily Claims! : " + dailyChestCount);
		for (int i = 1; i <= dailyChestCount; i++) {
			final MessageEmbed.Field reward = getReward(dbMember);
			eb.addField(reward);
			//noinspection ConstantConditions
			if (reward.getName().equals("Another Chest!")) i--;
		}

		final MessageEmbed dailyChests = eb.build();
		dbMember.setDailyChests(0);

		Card drawnCard = new Card();
		final boolean success = Database.addCard(userId, drawnCard);
		final String memberId = Card.CardFace.getId(drawnCard.getFace());

		if (success && memberId != null) {
			event.getGuild().retrieveMemberById(memberId).queue(member -> {
				final File file = drawnCard.getGraphic(member.getUser());
				if (file == null) {
					MessageEmbed embed = new EmbedBuilder()
						.setColor(REWARDS.getRarityColor(drawnCard))
						.setTitle(drawnCard.toString())
						.build();
					event.getHook().editOriginalEmbeds(dailyChests, embed).queue();
				} else {
					event.getHook().editOriginal("Claimed all Dailies!").queue();
					//noinspection ResultOfMethodCallIgnored,ConstantConditions
					event.getTextChannel()
						.sendMessageEmbeds(dailyChests)
						.addFile(file)
						.append(event.getMember().getAsMention())
						.queue(onSuccess -> file.delete());
				}
				dbMember.update();
			});
		} else event.getHook().editOriginal("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
	}

	private boolean isStaff(Guild g, Member m) {
		final Role staff = g.getRoleById("761056641860763668");
		final Role helper = g.getRoleById("663801966194851860");

		return m.getRoles().stream().anyMatch(it -> it.equals(staff) || it.equals(helper))
			|| m.getId().equals(Entry.CREATOR_ID);
	}

	private void sendToBugChannel(GuildMessageReactionAddEvent event) {
		event.retrieveMessage().queue(msg -> {
			final TextChannel bugchannel = event.getGuild().getTextChannelById("672884335447113769");
			if (bugchannel != null)
				bugchannel.sendMessageEmbeds(Embeds.getBugEmbed(msg)).queue();
		});
	}

	private void checkVotes(GuildMessageReactionAddEvent event, String upvoteEmote) {
		final int NEEDED_VOTES = 25;

		event.retrieveMessage().queue(message -> {
			if (event.getMember().getId().equals(message.getAuthor().getId())) {
				event.getReaction().removeReaction(event.getUser()).queue();
				event.getChannel().sendMessageEmbeds(Embeds.getSelfVote()).queue(msg ->
					msg.delete().queueAfter(5L, TimeUnit.SECONDS)
				);
			} else {
				MessageReaction checkmark = message.getReactions().stream()
					.filter(it -> it.getReactionEmote().getName().equals(upvoteEmote))
					.findFirst()
					.orElse(null);
				if (checkmark == null) return;
				if (checkmark.getCount() >= NEEDED_VOTES)
					promote(message);
			}
		});
	}

	private void promote(Message message) {
		final Guild guild = message.getGuild();
		final TextChannel candidates = guild.getTextChannelById("683070989319667829");
		if (candidates != null)
			candidates.sendMessageEmbeds(Embeds.getCandidatesEmbed(message)).queue();
	}

	private void promote(TextChannel channel, String messageId) {
		channel.retrieveMessageById(messageId).queue(this::promote);
	}

	private MessageEmbed.Field getReward(DBMember dbMember) {
		final REWARDS random = REWARDS.getRandom();

		String title = "";
		String description = "";
		switch (random) {
			case COINS -> {
				int coins = new Random().nextInt(20) + 1;
				title = "Coins!";
				description = "You won " + coins + " coins!";
				dbMember.adjustCoins(coins);
			}
			case EXP -> {
				int exp = new Random().nextInt(190) + 10;
				title = "EXP!";
				description = "You won " + exp + " exp!";
				dbMember.adjustExp(exp);
			}
			case LOCK -> {
				title = "Practice Lock!";
				description = "You won a practice lock! Use /practice to use it";
				dbMember.adjustLockCount(1);
			}
			case CHEST -> {
				title = "Another Chest!";
				description = "You won a reroll! A chest has been added to your inventory";
				dbMember.adjustNumOfDailies(1);
			}
			case ROLE_VOUCHER -> {
				title = "Role Voucher!";
				description = "You won a Vanity Role voucher! " + ROLE_COST +
					" coins have been added to purchase a vanity role";
				dbMember.adjustCoins(ROLE_COST);
			}
			case COLOR_VOUCHER -> {
				title = "Color Voucher!";
				description = "You won a Color Role Voucher! " + COLOR_COST +
					" coins have been added to purchase a color role";
				dbMember.adjustCoins(COLOR_COST);
			}
		}
		return new MessageEmbed.Field(title, description, false);
	}

	private enum REWARDS {
	COINS, // 40%
	EXP,   // 40%
	LOCK,  // 10%
	CHEST, // 8%
	ROLE_VOUCHER, // 1%
	COLOR_VOUCHER; // 1%

		public static REWARDS getRandom() {
			int rand = new Random().nextInt(100)+1; //1-100
			if (isBetween(rand, 1, 40)) return COINS;
			else if (isBetween(rand, 41, 80)) return EXP;
			else if (isBetween(rand, 81,90)) return LOCK;
			else if (isBetween(rand, 91, 98)) return CHEST;
			else if (isBetween(rand, 99, 99)) return ROLE_VOUCHER;
			else return COLOR_VOUCHER; //(isBetween(rand, 100, 100))
		}

		private static boolean isBetween(int x, int lower, int upper) {
		return x >= lower && x <= upper;
	}

		private static Color getRarityColor(Card card) {
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
	}

	@Override
	public void doNightlyReset() {
		Role patreonBasic = null;
		Role patreonPremium = null;
		Role suppChop = null;
		final List<Role> roles = getGuild().getRoles();
		for (Role r : roles) {
			if (r.getName().equalsIgnoreCase("patreon basic")) patreonBasic = r;
			else if (r.getName().equalsIgnoreCase("patreon premium")) patreonPremium = r;
			else if (r.getName().equalsIgnoreCase("Supporting Chopper")) suppChop = r;
		}
		Role finalPatreonBasic = patreonBasic;
		Role finalPatreonPremium = patreonPremium;
		Role finalSuppChop = suppChop;

		getGuild().loadMembers().onSuccess(members -> {
			List<String> basicPatreonIds = new ArrayList<>();
			List<String> premiumPatreonIds = new ArrayList<>();
			List<String> chopAndBasic = new ArrayList<>();
			List<String> chopAndPremium = new ArrayList<>();

			for (Member m : members) {
				if (finalPatreonBasic == null || finalPatreonPremium == null) break;

				if (m.getRoles().contains(finalPatreonBasic) && !m.getRoles().contains(finalSuppChop)) {
					basicPatreonIds.add(m.getId());
				} else if (m.getRoles().contains(finalPatreonPremium) && !m.getRoles().contains(finalSuppChop))  {
					premiumPatreonIds.add(m.getId());
				} else if (m.getRoles().contains(finalSuppChop) && m.getRoles().contains(finalPatreonBasic)) {
					chopAndBasic.add(m.getId());
				} else if (m.getRoles().contains(finalSuppChop) && m.getRoles().contains(finalPatreonPremium)) {
					chopAndPremium.add(m.getId());
				}


				//Get all of member roles and all the color reward roles
				List<Role> mRoles = new ArrayList<>(m.getRoles());
				List<Role> colorRoles = getGuild().getRoles().stream()
					.filter(role -> BuyGroup.colorRoles.contains(role.getName()))
					.collect(Collectors.toList());
				//Remove all roles that aren't colorRoles from memberRoles
				mRoles.retainAll(colorRoles);
				//Remove all roles left in member roles (These should only be colorRoles the member has
				mRoles.forEach(role -> getGuild().removeRoleFromMember(m, role).queue());

			}
			Database.resetDailyCounts(getGuild(), basicPatreonIds, premiumPatreonIds, chopAndBasic, chopAndPremium);
		});
	}
}


