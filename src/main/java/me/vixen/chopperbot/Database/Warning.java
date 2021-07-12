package me.vixen.chopperbot.Database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.Member;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Warning {
	@SerializedName("Warn #") private int warningNumber;
	@SerializedName("Warned User") private String usertag;
	@SerializedName("Moderator") private String moderator;
	@SerializedName("Reason") private String reason;

	protected Warning(int warningNumber, String targetTag, String mod, String reason) {
		this.warningNumber = warningNumber;
		this.usertag =  targetTag;
		this.moderator = mod;
		this.reason = reason;
	}

	public int getWarningNumber() {
		return warningNumber;
	}

	public String getUsertag() {
		return usertag;
	}

	public String getModerator() {
		return moderator;
	}

	public String getReason() {
		return reason;
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

	public static List<Warning> deserializeList(String jsonPayload) {
		Type listOfWarnings = new TypeToken<ArrayList<Warning>>() {}.getType();
		Gson gson = new Gson();
		return gson.fromJson(jsonPayload, listOfWarnings);
	}
}
