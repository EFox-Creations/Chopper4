package me.vixen.chopperbot.commands.global.gamble;

import me.vixen.chopperbot.database.UserProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;

public class Odds {
	public void handle(SlashCommandEvent event, UserProfile profile) {
		MessageEmbed embed = new EmbedBuilder()
			.setTitle("🎲 Feeling Lucky? 🎲")
			.setDescription("""
                    __Daily Chest:__
                    -Coins 40%
                    -Exp 40%
                    -Practice Lock 10%
                    -Chest 8%
                    -Role Voucher 1%
                    -Color Voucher 1%
                        
                    __Bet:__
                    -Lose 50%
                    -Payout = Bet 25%
                    -2x Bet 13%
                    -4x Bet 7%
                    -8x Bet 3%
                    -16x Bet 2%
                    
                    __Dice:__
                    - 5 in 12
                    
                    __Scratch Off:__
                    - 40 in 91
                    
                    __Lotto:__
                    1 in $
                        
                    __Rob Command:__
                    -Caught and Fined 25%
                    -Success 25%
                    -Failure 50%
                    (Robs 10% of a random target)
                        
                    __Cards:__
                    Mythic 5%
                    Legendary 9%
                    Rare 20%
                    Uncommon 29%
                    Common 37%
                    """.replace("$", String.valueOf(factorial(LottoGroup.UPPER) / (factorial(5) * factorial(LottoGroup.UPPER-5))))
			)
			.setColor(Color.BLUE)
			.build();
		event.getHook().editOriginalEmbeds(embed).setContent("").setActionRows().queue();
	}

	static int factorial(int n)
	{
		if (n == 0)
			return 1;

		return n*factorial(n-1);
	}
}
