package me.vixen.chopperbot.guilds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.List;

public class Config {

	public enum Punishment {
		NONE,
		WARN,
		KICK,
		BAN
	}

	public enum TreasureMode {
		BLACKLIST,
		WHITELIST;

		public String capitalize() {
			return Character.toString(this.toString().charAt(0)).toUpperCase() + this.toString().toLowerCase().substring(1);
		}
	}

	@SerializedName("Modlog ID")
	private final String modlogId;
	@SerializedName("Lvl Msg Override")
	private final boolean lvlMsgOverride;
	@SerializedName("Only Staff Polls")
	private final boolean onlyStaffPolls;
	@SerializedName("Enable Join/Leave")
	private boolean enableJoinLeaveMessges;
	@SerializedName("Join/Leave Channel")
	private String joinLeaveMsgsChannelId;
	@SerializedName("Domain Punishment")
	private Punishment punishment;
	@SerializedName("Blacklist Domains")
	private final List<String> domains;
	@SerializedName("Treasure Mode")
	private final TreasureMode mode;
	@SerializedName("Treasure Channels")
	private final List<String> channels;

	protected Config(String modlogId, boolean lvlMsgOverride, boolean onlyStaffPolls, boolean enableJoinLeaveMsgs,
					 String joinLeaveMsgsChannelId, Punishment punishment, List<String> domains, TreasureMode mode,
					 List<String> channels) {
		this.modlogId = modlogId;
		this.lvlMsgOverride = lvlMsgOverride;
		this.onlyStaffPolls = onlyStaffPolls;
		this.enableJoinLeaveMessges = enableJoinLeaveMsgs;
		this.joinLeaveMsgsChannelId = joinLeaveMsgsChannelId;
		this.punishment = punishment;
		this.domains = domains;
		this.mode = mode;
		this.channels = channels;
	}

	public static Config deserializeConfig(String Json) {
		Gson gson = new Gson();
		return gson.fromJson(Json, Config.class);
	}

	public String serialize() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(this);
	}

	@Nullable
	@CheckReturnValue
	public String getModlogId() {
		return modlogId;
	}

	public boolean arelvlMsgOverridden() {
		return lvlMsgOverride;
	}

	public boolean isOnlyStaffPolls() {
		return onlyStaffPolls;
	}

	public boolean areJoinLeaveMsgsDisabled() {
		return !enableJoinLeaveMessges;
	}

	public String getJoinLeaveMsgsChannelId() {
		return joinLeaveMsgsChannelId;
	}

	public Punishment getPunishment() {
		return punishment;
	}

	public void addDomain(String domain) {
		domains.add(domain);
	}

	public List<String> getDomains() {
		return domains;
	}

	public boolean deleteDomain(String domain) {
		return domains.removeIf(it -> it.equals(domain));
	}

	public void clearDomains() {
		domains.clear();
	}

	public TreasureMode getMode() {
		return mode;
	}

	public List<String> getChannels() {
		return channels;
	}

	public void addChannel(String channelId) {
		this.channels.add(channelId);
	}

	public void removeChannel(String channelId) {
		this.channels.removeIf(it -> it.equals(channelId));
	}

	public void clearChannels() {
		this.channels.clear();
	}
}

