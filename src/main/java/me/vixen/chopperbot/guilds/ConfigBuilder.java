package me.vixen.chopperbot.guilds;

import java.util.ArrayList;
import java.util.List;

public class ConfigBuilder{

	private String modLogId;
	private boolean lvlMsgOverride;
	private boolean onlyStaffPolls;
	private boolean enableJoinLeaveMessges;
	private String joinLeaveMsgsChannelId;
	private Config.Punishment punishment;
	private List<String> domains = new ArrayList<>();

	public ConfigBuilder setModLogId(String modLogId) {
		this.modLogId = modLogId;
		return this;
	}

	public ConfigBuilder setLvlMsgOverride(boolean lvlMsgOverride) {
		this.lvlMsgOverride = lvlMsgOverride;
		return this;
	}

	public ConfigBuilder setIsOnlyStaffPolls(boolean onlyStaffPolls) {
		this.onlyStaffPolls = onlyStaffPolls;
		return this;
	}

	public ConfigBuilder setEnableJoinLeaveMessges(boolean enableJoinLeaveMessges) {
		this.enableJoinLeaveMessges = enableJoinLeaveMessges;
		return this;
	}

	public ConfigBuilder setJoinLeaveMsgsChannelId(String joinLeaveMsgsChannelId) {
		this.joinLeaveMsgsChannelId = joinLeaveMsgsChannelId;
		return this;
	}

	public ConfigBuilder setPunishment(Config.Punishment punishment) {
		this.punishment = punishment;
		return this;
	}

	public ConfigBuilder setBlacklistDomains(List<String> domains) {
		this.domains = domains;
		return this;
	}

	public Config build() {
		return new Config(modLogId, lvlMsgOverride, onlyStaffPolls, enableJoinLeaveMessges, joinLeaveMsgsChannelId,
			punishment, domains);
	}
}
