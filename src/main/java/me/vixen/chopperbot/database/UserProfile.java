package me.vixen.chopperbot.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.vixen.chopperbot.Entry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class UserProfile {
	@Expose
	@SerializedName("UserId")
	private final String userId;
	@Expose
	@SerializedName("GuildId")
	private final String guildId;
	@Expose
	@SerializedName("Nickname")
	private String nickname;
	@Expose
	@SerializedName("IsAuthorized")
	private boolean authorized;
	@Expose
	@SerializedName("Has Level Msgs On")
	private boolean lvlMsgsEnabled = true;
	@Expose
	@SerializedName("Last Msg Time")
	private String lstMsgTime = OffsetDateTime.now().minus(5L, ChronoUnit.MINUTES).toString();
	@Expose
	@SerializedName("Unmute Time")
	private String unmuteTime = null;
	@Expose
	@SerializedName("Skill")
	private int skill = 1;
	@Expose
	@SerializedName("Lock Count")
	private int lockCount = 0;
	@Expose
	@SerializedName("Exp")
	private long exp = 0;
	@Expose
	@SerializedName("Level")
	private int level = 0;
	@Expose
	@SerializedName("Coins")
	private int coins = 0;

	private int galleryImgsLeft = 10;
	private int lottoPlaysLeft = 3;
	private boolean successOnRobToday = false;
	private int chestCount = 1;

	private List<Warning> warnings = new ArrayList<>();

	private UserProfile(String userId, String guildId, String nickname, boolean authorized) {
		this.userId = userId;
		this.guildId = guildId;
		this.nickname = nickname;
		this.authorized = authorized;
	}

	public static UserProfile createNewProfile(String userId, String guildId, String nickname) {
		return new UserProfile(userId, guildId, nickname, false);
	}

	public static UserProfile createNewAuthorizedProfile(String userId, String guildId, String nickname) {
		return new UserProfile(userId, guildId, nickname, true);
	}

	// *********************************************************************
	// *                     Getters/Setters/Adjusters                     *
	// *********************************************************************

	public boolean isAuthorized() {
		return authorized || userId.equals(Entry.CREATOR_ID);
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	public String getUserId() {
		return userId;
	}

	public String getGuildId() {
		return guildId;
	}

	public String getNickname() {
		return nickname;
	}

	public boolean areLvlMsgsEnabled() {
		return lvlMsgsEnabled;
	}

	/**
	 * Toggles messages on or off (True or False)
	 * <br>Returns the new value
	 *
	 * @return boolean - The new value of the toggle
	 */
	public boolean toggleLvlMsgs() {
		lvlMsgsEnabled = !lvlMsgsEnabled;
		return lvlMsgsEnabled; //Return new value
	}

	public OffsetDateTime getLstMsgTime() {
		try {
			OffsetDateTime parse = OffsetDateTime.parse(lstMsgTime);
			return parse;
		} catch (DateTimeParseException e) {
			OffsetDateTime fiveAgo = OffsetDateTime.now().minus(5L, ChronoUnit.MINUTES);
			lstMsgTime = fiveAgo.toString();
			return fiveAgo;
		}
	}

	public void updateLstMsgTime() {
		this.lstMsgTime = OffsetDateTime.now().toString();
	}

	/**
	 * Checks if this member is muted and unmutes them if their unmute time has passed
	 * @return True, if unmute time has not passed -
	 * False, if the unmute time is null or is in the past
	 */
	public boolean isMuted() {
		if (unmuteTime == null) return false;

		try {
			OffsetDateTime parse = OffsetDateTime.parse(unmuteTime);
			boolean actuallyMuted = (parse != null && parse.isAfter(OffsetDateTime.now()));
			if (actuallyMuted) return true;
			unmuteTime = null;
			return false;
		} catch (DateTimeParseException e) {
			unmuteTime = null;
			return false;
		}
	}

	public void setMuted(OffsetDateTime unmuteTime) {
		this.unmuteTime = unmuteTime.toString();
	}

	public void unmute() {
		this.unmuteTime = null;
	}

	public String getUnmuteTime() {
		return unmuteTime == null ? null : unmuteTime.toString();
	}

	public int getGalleryImgsLeft() {
		return galleryImgsLeft;
	}

	public void adjustGalleryImgsLeft(int adjustAmount) {
		galleryImgsLeft += adjustAmount;
	}

	public int getChestCount() {
		return chestCount;
	}

	public void setChestCount(int set) {
		chestCount = set;
	}

	public void decrementChestCount() {
		chestCount -= 1;
	}

	public void incrementChestCount() {
		chestCount += 1;
	}

	public int getSkill() {
		return skill;
	}

	public void adjustSkill(int adjustAmount) {
		skill += adjustAmount;
	}

	public int getLockCount() {
		return lockCount;
	}

	public void adjustLockCount(int adjustAmount) {
		lockCount += adjustAmount;
	}

	public int getExp() {
		return (int) exp;
	}

	public void adjustExp(int adjustAmount) {
		exp += adjustAmount;
		exp = Math.max(exp, 0); //No exp less than 0
		exp = Math.min(exp, Integer.MAX_VALUE); //No exp greater than Integer.MAX_VALUE
		level = getLevelFromXp();
	}

	public boolean awardExp(boolean override) {
		try {
			OffsetDateTime parse = OffsetDateTime.parse(lstMsgTime);
			boolean after = OffsetDateTime.now().plus(1L, ChronoUnit.SECONDS)
				.isAfter(parse.plus(5L, ChronoUnit.MINUTES));
			if (after || override) {
				adjustExp(new Random().nextInt(15)+1);
				updateLstMsgTime();
				if (hasLeveledUp((int) exp)) {
					level++;
					update(null);
					return areLvlMsgsEnabled();
				} else {
					update(null);
					return false;
				}
			} return false;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	public int getLevel() {
		return level;
	}

	public int getCoins() {
		return coins;
	}

	public void adjustCoins(int adjustAmount) {
		coins += adjustAmount;
		coins = Math.max(coins, 0);
	}

	public int getLottoPlaysLeft() {
		return lottoPlaysLeft;
	}

	public void playLotto() {
		lottoPlaysLeft--;
	}

	public boolean hasRobbed() {
		return successOnRobToday;
	}

	public void rob() {
		successOnRobToday = true;
	}

	public List<Warning> getWarnings() {
		loadWarnings();
		warnings.sort(Comparator.comparing(Warning::getWarningNumber));
		return warnings;
	}

	private void loadWarnings() {
		this.warnings = Database.getWarnings(guildId, userId);
		if (warnings == null)
			this.warnings = new ArrayList<>();
	}

	public String warningsAsJSON() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(warnings);
	}

	public void addWarning(String targetTag, User moderator, String reason) {
		loadWarnings();
		warnings.add(new Warning(getNextWarnNumber(), targetTag, moderator.getAsTag(), reason));
		Database.setWarnings(this);
	}

	public void removeWarning(Warning warning) {
		loadWarnings();
		warnings.remove(warning);
		Database.setWarnings(this);
	}

	public int getNextWarnNumber() {
		return warnings.size()+1;
	}

	// *********************************************************************
	// *                      Private Methods/Helpers                      *
	// *********************************************************************

	private boolean hasLeveledUp(int currentExp) {
		//   formula n=Level
		//   exp = (n*150) + ((n*150)/2)

		if (level == 9544371) return false; //This is the max level

		int nextLevel = level+1;
		int levelUpExp = ((nextLevel*150) + ((nextLevel*150)/2));

		return currentExp >= levelUpExp;
	}

	//I used to know how this works... its just the opposite of the exp formula above
	private int getLevelFromXp() {
		return (int) (((exp*2)/3)/150); //reversed exp formula
	}

	// *********************************************************************
	// *                          Database Helpers                         *
	// *********************************************************************

	public void update(Member m) {
		if (m == null) {
			Guild guild = Entry.jda.getGuildById(guildId);
			if (guild != null)
				Database.upsertMember(guild, this);
		} else {
			nickname = m.getEffectiveName();
			Database.upsertMember(m.getGuild(), this);
		}

	}

	@Override
	public String toString() {
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		return gson.toJson(this);
	}
}

