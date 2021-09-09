package me.vixen.chopperbot;

import me.vixen.chopperbot.database.DatabaseHandler;
import net.dv8tion.jda.api.JDA;
import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;

public class Entry {
    private static ChopBot bot = null;
    private static ChopperConsole chopperConsole = null;

    public static void main(String[] args) {
        chopperConsole = new ChopperConsole();
        chopperConsole.frame.setVisible(true);
    }

    protected static void createBot(ChopBot.TOKEN token)
        throws IllegalArgumentException, FileNotFoundException, LoginException, InterruptedException {

        if (token == null) throw new IllegalArgumentException("Token not provided");
        bot = ChopBot.createNew(token);
    }

    public static JDA getJDA() {
        return bot.getJda();
    }
    public static void setJDA(JDA jda) {

    }

    public static String getCreatorId() {
        return bot.getCreatorId();
    }

    public static ChopperConsole getChopperConsole() {
        return chopperConsole;
    }

    public static int shutdown() {
        try {
            getJDA().shutdownNow();
            return 0;
        } catch (Exception e) {
            return -1;
        }

    }
}
