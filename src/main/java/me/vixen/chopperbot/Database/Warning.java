package me.vixen.chopperbot.Database;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Warning {
	@SerializedName("Warn #") private final int warningNumber;
	@SerializedName("Warned User") private final String usertag;
	@SerializedName("Moderator") private final String moderator;
	@SerializedName("Reason") private final String reason;

	protected Warning(int warningNumber, String targetTag, String mod, String reason) {
		this.warningNumber = warningNumber;
		this.usertag =  targetTag;
		this.moderator = mod;
		this.reason = reason;
	}

	public int getWarningNumber() {
		return warningNumber;
	}

	@Override
	public String toString() {
		return "Warning{" +
			"warningNumber='" + warningNumber + '\'' +
			", username='" + usertag + '\'' +
			", moderator='" + moderator + '\'' +
			", reason='" + reason + '\'' +
			'}';
	}

	public String toPrettyString() {
		return "-Warning#: " + warningNumber
			+ " Username: " + usertag
			+ " Mod: " + moderator
			+ " Reason: " + reason;
	}

	public static List<Warning> deserializeList(String jsonPayload) {
		Type listOfWarnings = new TypeToken<ArrayList<Warning>>() {}.getType();
		Gson gson = new Gson();
		return gson.fromJson(jsonPayload, listOfWarnings);
	}
}
