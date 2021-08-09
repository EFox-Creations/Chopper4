package me.vixen.chopperbot;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import sun.jvm.hotspot.ui.tree.FloatTreeNodeAdapter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ChopConfig {

    @SerializedName("Token")
    private String token;
    @SerializedName("Creator Id")
    private String creatorId;

    public static ChopConfig load() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("Config/ChopConfig.json"));
            ChopConfig chopConfig = new GsonBuilder().create().fromJson(reader, ChopConfig.class);
            reader.close();
            if (chopConfig == null) throw new NullPointerException("JSON not provided or malformed");
            else return chopConfig;
        } catch (IOException e) {
            Logger.log("Failed to load Config: ", e);
        } catch (NullPointerException e) {
            Logger.log("Failed to create config", e);
        } catch (JsonSyntaxException e) {
            Logger.log("JSON malformed: ", e);
        }
        return null;
    }

    public String getToken() {
        return token;
    }

    public String getCreatorId() {
        return creatorId;
    }
}
