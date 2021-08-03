package me.vixen.chopperbot.guilds.bejoijoplugins;

import me.vixen.chopperbot.database.Database;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Random;

public class Card {
	public enum Rarity {
		COMMON, // 37%
		UNCOMMON, // 29%
		RARE, // 20%
		LEGENDARY, // 9%
		MYTHIC; //5%

		public static Rarity getRandom() {
			int rand = new Random().nextInt(100)+1; //1-100
			if (isBetween(rand, 1, 37)) return COMMON;
			else if (isBetween(rand, 38, 66)) return UNCOMMON;
			else if (isBetween(rand, 67, 86)) return RARE;
			else if (isBetween(rand, 87, 95)) return LEGENDARY;
			else return MYTHIC; // if (isbetween(rand, 106, 100))
		}

		private static boolean isBetween(int x, int lower, int upper) {
			return x >= lower && x <= upper;
		}
	}
	public enum CardFace {
		BEJOIJO,
		REDDINGTON,
		VIXEN,
		LENNY,
		LEOPMESP86,
		LUJEREX,
		DONGAS;

		public static String getId(CardFace face) {
			switch (face) {
				case BEJOIJO -> {
					return "345011900549038080";
				}
				case LENNY -> {
					return "473272676593434626";
				}
				case REDDINGTON -> {
					return "258102752717045761";
				}
				case VIXEN -> {
					return "354682693918785549";
				}
				case LEOPMESP86 -> {
					return "249662563762044929";
				}
				case LUJEREX -> {
					return "383868018201198592";
				}
				case DONGAS -> {
					return "510079643139833856";
				}
				default -> {
					return null;
				}
			}
		}

		public static CardFace getRandom() {
			return values()[new Random().nextInt(values().length)];
		}
	}

	Rarity rarity;
	CardFace face;
	int id;

	public Card() {
		rarity = Rarity.getRandom();
		face = CardFace.getRandom();
		id = Database.getNextCardId();
	}

	private Card(Rarity rarity, CardFace face, int id) {
		this.rarity = rarity;
		this.face = face;
		this.id = id;
	}

	public String getFaceAsString() {
		return face.toString();
	}

	public CardFace getFace() { return face; }

	public String getRarityAsString() {
		return rarity.toString();
	}

	public Rarity getRarity() { return rarity; }

	public int getId() { return id; }

	@Override
	public String toString() {
		return rarity.toString() + " " + face.toString() + " Id=" + id;
	}

	public static class Builder {
		Rarity rarity;
		CardFace face;
		int id;

		public Builder setFace(String face) {
			this.face = CardFace.valueOf(face);
			return this;
		}

		public Builder setRarity(String rarity) {
			this.rarity = Rarity.valueOf(rarity);
			return this;
		}

		public Builder setId(int id) {
			this.id = id;
			return this;
		}

		public Card build() {
			return new Card(rarity, face, id);
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public File getGraphic(User u) {
		try {
			BufferedImage bg = ImageIO.read(new File("CardBlanks/" + capitalize(rarity.toString()) + ".png"));

			File pfpSrc = download(u.getAvatarUrl());
			BufferedImage pfp = ImageIO.read(pfpSrc);
			pfpSrc.delete();
			pfp = resize(pfp);

			Graphics2D g2 = (Graphics2D) bg.getGraphics();

			//Draw PFP
			g2.drawImage(pfp, 167, 290, 415, 550, 0,0, pfp.getWidth(), pfp.getHeight(), null);

			//Draw name
			String name = capitalize(face.toString());
			Font font = new Font("SANS_SERIF", Font.PLAIN, 60);
			printCentered(g2, font, name, bg.getWidth());

			File out = new File("out.png");
			ImageIO.write(bg, "png", out);
			return out;
		} catch (IOException e) {
			return null;
		}

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

	private static BufferedImage resize(BufferedImage img) {
		Image tmp = img.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	private String capitalize(String str) {
		return String.valueOf(str.charAt(0)).toUpperCase() + str.toLowerCase().substring(1);
	}

	/**
	 * Horizontally centers text in a given graphics space
	 * @param g2d The graphics object
	 * @param font The desired font
	 * @param s The desired text
	 * @param width The width of the space to center in
	 */
	private void printCentered(Graphics g2d, Font font, String s, int width){
		g2d.setFont(font);
		g2d.setColor(Color.BLACK);
		int stringLen = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();
		int start = width/2 - stringLen/2;
		g2d.drawString(s, start, 650);
	}
}
