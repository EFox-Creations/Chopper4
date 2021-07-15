package me.vixen.chopperbot.Database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.vixen.chopperbot.Entry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DBMember {
	private final String userId;
	private final String guildId;
	private final String nickname;
	private boolean authorized;
	private boolean lvlMsgsEnabled;
	private OffsetDateTime lstMsgTime;
	private OffsetDateTime unmuteTime;
	private int galleryImgsLeft;
	private int dailyChests;
	private int skill;
	private int lockCount;
	private long exp;
	private int level;
	private int coins;
	private int lottoPlaysLeft;
	private boolean successOnRobToday;
	private List<Warning> warnings;

	public DBMember(String userId, String guildId, String nickname, boolean authorized, boolean lvlMsgsEnabled,
					OffsetDateTime lstMsgTime, OffsetDateTime unmuteTime, int galleryImgsLeft,
					int dailyChests, int skill, int lockCount, int exp, int level, int coins, int lottoPlaysLeft,
					boolean successOnRobToday) {
		this.userId = userId;
		this.guildId = guildId;
		this.nickname = nickname;
		this.authorized = authorized;
		this.lvlMsgsEnabled = lvlMsgsEnabled;
		this.lstMsgTime = lstMsgTime;
		this.unmuteTime = unmuteTime;
		this.galleryImgsLeft = galleryImgsLeft;
		this.dailyChests = dailyChests;
		this.skill = skill;
		this.lockCount = lockCount;
		this.exp = exp;
		this.level = level;
		this.coins = coins;
		this.lottoPlaysLeft = lottoPlaysLeft;
		this.successOnRobToday = successOnRobToday;
		loadWarnings();
	}

	public DBMember(Member m, Guild g, boolean authorized) {
		this(m.getUser().getId(), g.getId(), m.getEffectiveName(), authorized, true,
			OffsetDateTime.now().minus(5L, ChronoUnit.MINUTES), null, 10, 1, 1, 0,0,0,0,
			3, false);
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
		if (lstMsgTime == null)
			lstMsgTime = OffsetDateTime.now().minus(5L, ChronoUnit.MINUTES);
		return lstMsgTime;
	}

	public void updateLstMsgTime() {
		this.lstMsgTime = OffsetDateTime.now();
	}

	/**
	 * Checks if this member is muted and unmutes them if their unmute time has passed
	 * @return True, if unmute time has not passed -
	 * False, if the unmute time is null or is in the past
	 */
	public boolean isMuted() {
		boolean actuallyMuted = (unmuteTime != null && unmuteTime.isAfter(OffsetDateTime.now()));
		if (actuallyMuted) return true;
		unmuteTime = null;
		return false;
	}

	public void setMuted(OffsetDateTime unmuteTime) {
		this.unmuteTime = unmuteTime;
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

	public int getDailyChests() {
		return dailyChests;
	}

	public void adjustNumOfDailies(int adjustAmount) {
		dailyChests += adjustAmount;
	}

	public void setDailyChests(int setInt) {
		dailyChests = setInt;
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
		boolean after = OffsetDateTime.now().plus(1L, ChronoUnit.SECONDS)
			.isAfter(lstMsgTime.plus(5L, ChronoUnit.MINUTES));

		if (after || override) {
			adjustExp(new Random().nextInt(15)+1);
			updateLstMsgTime();
			if (hasLeveledUp((int) exp)) {
				level++;
				update();
				return true;
			} else {
				update();
				return false;
			}
		} return false;
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
		warnings.sort(Comparator.comparing(Warning::getWarningNumber));
		return warnings;
	}

	public void loadWarnings() {
		this.warnings = Database.getWarnings(guildId, userId);
		if (warnings == null)
			this.warnings = new ArrayList<>();
	}

	public String warningsAsJSON() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(warnings);
	}

	public void addWarning(String targetTag, User moderator, String reason) {
		warnings.add(new Warning(getNextWarnNumber(), targetTag, moderator.getAsTag(), reason));
		Database.setWarnings(this);
	}

	public void removeWarning(Warning warning) {
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

	public void update() {
		Guild guild = Entry.jda.getGuildById(guildId);
		if (guild != null)
			Database.upsertMember(guild, this);
	}

	@Override
	public String toString() {
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		return gson.toJson(this);
	}
}

