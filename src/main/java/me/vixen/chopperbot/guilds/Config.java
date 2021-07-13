package me.vixen.chopperbot.guilds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

public class Config {

	@SerializedName("Modlog ID")
	private final String modlogId;
	@SerializedName("Lvl Msg Override")
	private final boolean lvlMsgOverride;
	@SerializedName("Only Staff Polls")
	private final boolean onlyStaffPolls;

	protected Config(String modlogId, boolean lvlMsgOverride, boolean onlyStaffPolls) {
		this.modlogId = modlogId;
		this.lvlMsgOverride = lvlMsgOverride;
		this.onlyStaffPolls = onlyStaffPolls;
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

}

