package me.vixen.chopperbot.guilds.bejoijoplugins;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EUP2UB {

	private static final Map<String, String> translations = new HashMap<>();

	static {
		translations.put("Hat", "prop_hats=\"%\" tex_hats=\"%\"");
		translations.put("Glasses", "prop_glasses=\"%\"");
		translations.put("Ear", "prop_ears=\"%\"");
		translations.put("Watch", "prop_watches=\"%\"");
		translations.put("Mask", "comp_beard=\"%\"");
		translations.put("Top", "comp_shirtoverlay=\"%\" tex_shirtoverlay=\"%\"");
		translations.put("UpperSkin", "comp_shirt=\"%\" tex_shirt=\"%\"");
		translations.put("Decal", "comp_decals=\"%\" tex_decals=\"%\"");
		translations.put("UnderCoat", "comp_accessories=\"%\" tex_accessories=\"%\"");
		translations.put("Pants", "comp_pants=\"%\" tex_pants=\"%\"");
		translations.put("Shoes", "comp_shoes=\"%\" tex_shoes=\"%\"");
		translations.put("Accessories", "comp_eyes=\"%\" tex_eyes=\"%\"");
		translations.put("Armor", "comp_tasks=\"%\" tex_tasks=\"%\"");
		translations.put("Parachute", "comp_hands=\"%\" tex_hands=\"%\"");
	}

	public static String convert(GuildMessageReceivedEvent event) throws IllegalArgumentException{
		final String input = event.getMessage().getContentRaw();
		final String ub = convertToUB(input);
		return "UB:\n```" + ub + "```\n";
	}

	private static String convertToUB(String input) throws IllegalArgumentException {
		try {
			Map<String, String> components = new HashMap<>();

			//Populate the component dictionary
			for(String str : input.split("\n")) {
				if (!str.contains("=")) continue;
				String[] parts = str.split("=");
				components.put(parts[0], parts[1]);
				components.remove("Category");
				components.remove("Category2");
			}

			//Find and save gender, then remove it as it is not a component
			String gender = components.get("Gender");
			components.remove("Gender");

			//Init string builder
			StringBuilder builder = new StringBuilder();

			//Include a comment in the output containing the name of the outfit
			Matcher matcher = Pattern.compile("(\\[.+\\])").matcher(input.split("\n")[0]);
			if (matcher.find())
				builder.append("<!-- ").append(matcher.group(0)).append(" --> ");

			//Open the XML tag
			builder.append("<Ped ");

			//modify the tag with appropriate components
			/*
			 * This works by utilizing the dictionaries.
			 * It cycles through all components listed in the EUP config
			 * For each config, it pulls the model and texture number
			 * Then, utilizing the translation dictionary, it pulls the corresponding string to modify the tag
			 * the % placeholders are replaced by the appropriate model and texture number
			 */
			for(String key : components.keySet()) {
				String model = components.get(key).split(":")[0].trim();
				String texture = components.get(key).split(":")[1].trim();
				String type = translations.get(key).replaceFirst("%", model).replaceFirst("%", texture);
				builder.append(type).append(" ");
			}

			//Build the string and trim it to remove the trailing whitespace before assigning the ped model and closing the tag
			String allComps = builder.toString().trim();
			String model = gender.equalsIgnoreCase("female") ? "MP_F_FREEMODE_01" : "MP_M_FREEMODE_01";
			return (allComps + (">" + model + "</Ped>"));
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("Invalid EUP Form detected! Please check your input!");
		}
	}
}
