package me.vixen.chopperbot.guilds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Config {

	public enum Punishment {
		NONE,
		WARN,
		KICK,
		BAN
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

	protected Config(String modlogId, boolean lvlMsgOverride, boolean onlyStaffPolls, boolean enableJoinLeaveMsgs,
					 String joinLeaveMsgsChannelId, Punishment punishment, List<String> domains) {
		this.modlogId = modlogId;
		this.lvlMsgOverride = lvlMsgOverride;
		this.onlyStaffPolls = onlyStaffPolls;
		this.enableJoinLeaveMessges = enableJoinLeaveMsgs;
		this.joinLeaveMsgsChannelId = joinLeaveMsgsChannelId;
		this.punishment = punishment;
		this.domains = domains;
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

	public boolean areJoinLeaveMsgsEnabled() {
		return enableJoinLeaveMessges;
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
		return domains.remove(domain);
	}

	public void clearDomains() {
		domains.clear();
	}
}

