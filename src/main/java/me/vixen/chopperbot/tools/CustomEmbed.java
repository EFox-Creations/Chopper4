package me.vixen.chopperbot.tools;

import com.google.gson.annotations.SerializedName;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;

public class CustomEmbed {

    @SerializedName("authorName")
    private String authorName;
    @SerializedName("authorUrl")
    private String authorUrl;
    @SerializedName("authorAvatarUrl")
    private String authorIconUrl;
    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;
    @SerializedName("color")
    private ColorWrapper color;
    @SerializedName("title")
    private String title;
    @SerializedName("titleUrl")
    private String titleUrl;
    @SerializedName("description")
    private String description;
    @SerializedName("field1")
    private MessageEmbed.Field field1;
    @SerializedName("field2")
    private MessageEmbed.Field field2;
    @SerializedName("field3")
    private MessageEmbed.Field field3;
    @SerializedName("field4")
    private MessageEmbed.Field field4;
    @SerializedName("imageUrl")
    private String imageUrl;
    @SerializedName("footerIcon")
    private String footerIcon;
    @SerializedName("footerText")
    private String footerText;
    @SerializedName("includeTimestamp")
    private boolean timestamp;
    @SerializedName("channelId")
    private String channelId;


    public MessageEmbed toMessageEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(authorName, authorUrl, authorIconUrl);
        eb.setThumbnail(thumbnailUrl);
        eb.setColor(color == null ? new Color(153,170,181) : color.toAwtColor());
        eb.setTitle(title, titleUrl);
        eb.setDescription(description);
        eb.addField(field1);
        eb.addField(field2);
        eb.addField(field3);
        eb.addField(field4);
        eb.setImage(imageUrl);
        eb.setFooter(footerText, footerIcon);
        if (timestamp)
            eb.setTimestamp(Instant.now());

        return eb.build();
    }

    public String getChannelId() {
        return channelId;
    }
}
