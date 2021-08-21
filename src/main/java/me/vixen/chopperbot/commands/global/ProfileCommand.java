package me.vixen.chopperbot.commands.global;

import me.vixen.chopperbot.database.Database;
import me.vixen.chopperbot.commands.ICommand;
import me.vixen.chopperbot.database.UserProfile;
import me.vixen.chopperbot.tools.Embeds;
import me.vixen.chopperbot.tools.Errors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class ProfileCommand implements ICommand {
	@Override
	public void handle(SlashCommandEvent event) {
		event.deferReply(false).queue();
		OptionMapping userOpt = event.getOption("user");
		if (userOpt == null) { //retrieve self
			Member member = event.getMember();
			try {
				//noinspection ConstantConditions cant be null
				File draw = draw(event.getGuild(), member);
				if (draw == null) {
					event.reply("An error occurred; aborting with Code " + Errors.PROFILE1).queue();
					return;
				}
				//noinspection ResultOfMethodCallIgnored
				event.getHook().editOriginal(draw).queue(unused -> draw.delete());
			} catch (IOException e) {
				//noinspection ConstantConditions cant be null
				UserProfile dbMember = Database.getMember(event.getGuild(), member.getId());
				if (dbMember == null) {
					event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
					return;
				}
				event.getHook().editOriginalEmbeds(
					new EmbedBuilder()
						.setColor(Embeds.Colors.FOXORANGE.get())
						.setTitle(member.getEffectiveName())
						.setThumbnail(member.getUser().getAvatarUrl())
						.setDescription(String.format("""
								Alias: %s
								Time in Server: %s
								Exp: %d
								Level: %d
								Locks: %d
								Skill: %s
								Currency: %d
								Progress: %s
								""",
							member.getEffectiveName(),
							ChronoUnit.MONTHS.between(member.getTimeJoined(), OffsetDateTime.now()),
							dbMember.getExp(),
							dbMember.getLevel(),
							dbMember.getLockCount(),
							dbMember.getSkill() + "%",
							dbMember.getCoins(),
							calcProgress(dbMember.getExp(), dbMember.getLevel())
						))
						.build()
				).queue();
			}
		} else { //retrieve user
			Member member = userOpt.getAsMember();
			if (member == null) {
				event.getHook().editOriginalEmbeds(Embeds.getUnknownMember()).queue();
			} else {
				try {
					File draw = draw(event.getGuild(), member);
					if (draw == null) {
						event.reply("An error occurred; aborting with Code " + Errors.PROFILE1).queue();
						return;
					}
					//noinspection ResultOfMethodCallIgnored
					event.getHook().editOriginal(draw).queue(unused -> draw.delete());
				} catch (IOException e) {
					//noinspection ConstantConditions cant be null
					UserProfile dbMember = Database.getMember(event.getGuild(), member.getId());
					if (dbMember == null) {
						event.reply("An error occurred; aborting with Code " + Errors.DBNULLRETURN).queue();
						return;
					}
					event.getHook().editOriginalEmbeds(
						new EmbedBuilder()
							.setColor(Embeds.Colors.FOXORANGE.get())
							.setTitle(member.getEffectiveName())
							.setThumbnail(member.getUser().getAvatarUrl())
							.setDescription(String.format("""
								Alias: %s
								Time in Server: %s
								Exp: %d
								Level: %d
								Locks: %d
								Skill: %s
								Currency: %d
								Progress: %s
								""",
								member.getEffectiveName(),
								ChronoUnit.MONTHS.between(member.getTimeJoined(), OffsetDateTime.now()),
								dbMember.getExp(),
								dbMember.getLevel(),
								dbMember.getLockCount(),
								dbMember.getSkill() + "%",
								dbMember.getCoins(),
								calcProgress(dbMember.getExp(), dbMember.getLevel())
								))
							.build()
					).queue();
				}
			}
		}
	}

	@Override
	public CommandData getCommandData() {
		return new CommandData("profile", "Get the profile of a user")
			.addOption(OptionType.USER, "user", "The user to get (leave blank for yours)", false);
	}

	private static File draw(Guild g, Member m) throws IOException {
		UserProfile dbMember = Database.getMember(g, m.getId());
		if (dbMember == null) {
			return null;
		}
		String name = m.getUser().getName();
		if (name.length() > 15) name = name.substring(0,11) + "...";
		final long months = ChronoUnit.MONTHS.between(m.getTimeJoined(), OffsetDateTime.now());
		String nickname = m.getNickname();
		String alias = nickname == null ? "None" : nickname;
		if (alias.length() > 15) alias = alias.substring(0,11) + "...";
		final int exp = dbMember.getExp();
		final int level = dbMember.getLevel();
		final int locks = dbMember.getLockCount();
		final int skill = dbMember.getSkill();
		final int currency = dbMember.getCoins();

		//The card
		BufferedImage card = new BufferedImage(1000,550, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) card.getGraphics();

		//Copy blank
		File blank = new File("CardBlanks/blankProfile.png");
		BufferedImage background = ImageIO.read(blank);

		g2.drawImage(background, 0, 0, card.getWidth(), card.getHeight(), 0, 0, background.getWidth(), background.getHeight(), null);

		//Download and copy PF
		File pfpSrc = download(m.getUser().getAvatarUrl());
		BufferedImage pfp = ImageIO.read(pfpSrc);
		g2.setColor(Color.BLACK);
		g2.drawImage(pfp, 744,249, 944,449, 0,0, pfp.getWidth(), pfp.getHeight(), null);
		//noinspection ResultOfMethodCallIgnored
		pfpSrc.delete();
		g2.setFont(new Font("SANS_SERIF", Font.PLAIN, 30));
		g2.setColor(Color.BLACK);

		//Draw info
		//Name
		g2.drawString(name, 175, 70);
		//Alias
		g2.drawString(alias, 165, 112);
		//Months in server
		g2.drawString(months + " months", 245, 152);
		//Exp
		g2.drawString(String.valueOf(exp), 170, 195);
		//Level
		g2.drawString(String.valueOf(level), 180, 240);
		//Locks
		g2.drawString(String.valueOf(locks), 175, 280);
		//Skill
		g2.drawString(skill + "%", 240, 320);
		//Coins
		g2.drawString(String.valueOf(currency), 200, 365);


		//Draw whole bar
		g2.setColor(Embeds.Colors.FOXORANGE.get());
		int rectWidth = 450;
		int rectHeight = 40;
		g2.fillRoundRect(220, 450, rectWidth, rectHeight, 10, 10);
		//draw progress
		double calcedProgress = calcProgress(exp, level);
		int progress = (int) Math.round(rectWidth*calcedProgress);
		g2.setColor(Color.GREEN);
		g2.fillRoundRect(220, 450, progress, rectHeight,10,10);


		//save and return
		File output = new File("card.png");
		ImageIO.write(card, "png", output);
		return output;
	}

	private static double calcProgress(int exp, int currentLevel) {
		//  p | exp |  x
		//  W | lvl | 100

		int currentlvlexp = ((currentLevel*150) + ((currentLevel*150)/2));
		int nextLevel = currentLevel+1;
		int nextlvlexp = ((nextLevel*150) + ((nextLevel*150)/2));

		//Math?... IDK.. I wrote it and it works so...
		return Math.max(.01, (int) ((((exp-currentlvlexp)*100)/nextlvlexp)*.01));
	}

	private static File download(String avatarUrl) throws IOException {
		System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
		URL url = new URL(avatarUrl);
		InputStream in = new BufferedInputStream(url.openStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n;
		while (-1!=(n=in.read(buf)))
		{
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();
		FileOutputStream fos = new FileOutputStream("download.png");
		fos.write(response);
		fos.close();
		System.clearProperty("http.agent");
		return new File("download.png");
	}
}
