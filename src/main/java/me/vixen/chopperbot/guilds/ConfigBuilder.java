package me.vixen.chopperbot.guilds;

public class ConfigBuilder{

	private String modLogId;
	private boolean lvlMsgOverride;
	private boolean onlyStaffPolls;

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

	public Config build() {
		return new Config(modLogId, lvlMsgOverride, onlyStaffPolls);
	}
}
