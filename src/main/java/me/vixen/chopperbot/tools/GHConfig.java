package me.vixen.chopperbot.tools;

import com.google.gson.annotations.SerializedName;

public class GHConfig {

    @SerializedName("RepositoryName")
    private String resName;
    @SerializedName("OAuthToken")
    private String oauth;
    @SerializedName("DefaultAssigneeUsername")
    private String defaultAssignee;

    public String getResName() {
        return resName;
    }

    public String getOauth() {
        return oauth;
    }

    public String getDefaultAssignee() {
        return defaultAssignee;
    }
}
