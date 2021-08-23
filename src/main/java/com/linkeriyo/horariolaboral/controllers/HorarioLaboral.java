package com.linkeriyo.horariolaboral.controllers;

import com.linkeriyo.horariolaboral.controllers.listeners.ConsoleInputListener;
import com.linkeriyo.horariolaboral.controllers.listeners.HorarioLaboralListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HorarioLaboral {
    private static JDA jda;
    public static SimpleDateFormat inputSDF = new SimpleDateFormat("d 'de' MMMM/yyyy", Locale.forLanguageTag("es-ES"));
    public static SimpleDateFormat outputSDF = new SimpleDateFormat("dd/MM/yyyy");
    public static SimpleDateFormat monthSDF = new SimpleDateFormat("MMMM/yyyy");
    private static SheetsAccess sheetsAccess;

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public HorarioLaboral() throws LoginException, IOException {
        try {
            String token = readFile("files/token", StandardCharsets.UTF_8);

            sheetsAccess = new SheetsAccess();
            jda = JDABuilder.createDefault(token)
                    .addEventListeners(new HorarioLaboralListener())
                    .setActivity(Activity.listening("linkeriyo"))
                    .build();
            jda.awaitReady();

/*
            MessageEmbed embed = new EmbedBuilder().setDescription("1. respetacion\n"
                    + "2. no sé consiste en respetarse\n"
                    + "3. si te dicen que estás fuerte probablemente es mentira\n"
                    + "4. respeten a bad bunny (porque si no os banea vaya)\n"
                    + "5. no ligar con menores\n"
                    + "6. mete hellcase\n"
                    + "7. need for speed most wanted\n"
                    + "8. no nos gustan los chetos\n"
                    + "9. lo primero que tienen que hacer los ricos es invitar a los colegas\n"
                    + "10. si se te ocurre algo se lo dices a <@154268434090164226>")
                    .setFooter("gracias", "https://mui.today/__export/1583420503184/sites/mui/img/2019/12/02/bad-bunny.jpg_1899857922.jpg")
                    .setImage("https://cutewallpaper.org/21/minecraft-wallpaper-hd/Download-minecraft-wallpaper-hd.jpg")
                    .setAuthor("normas y consejos", "https://discordapp.com", null)
                    .setThumbnail("https://cdn.discordapp.com/attachments/674346221569310741/792041822561370122/doro.png")
                    .addField("che- check this out", "[@minecraftersooc](https://twitter.com/minecraftersooc)", false)
                    .addField("invitación permanente", "[https://discord.com/invite/PJuJMJ6](https://discord.com/invite/PJuJMJ6)", false)
                    .build();

            jda.getTextChannelById("713842133232254977").sendMessage(embed).queue();
*/
        } catch (InterruptedException | GeneralSecurityException ex) {
            Logger.getLogger(HorarioLaboral.class.getName()).log(Level.SEVERE, null, ex);
        }

        new Thread(new ConsoleInputListener(jda)).start();
    }

    public static JDA getJda() {
        return jda;
    }

    public static SheetsAccess getSheetsAccess() {
        return sheetsAccess;
    }


    public static void main(String[] args) throws InterruptedException {
        boolean exit = false;
        while (!exit) {
            try {
                new HorarioLaboral();
                exit = true;
            } catch (LoginException ex) {
                ex.printStackTrace();
                Thread.currentThread().wait(10000);
            } catch (FileNotFoundException ex) {
                System.err.println("No se ha encontrado el archivo files/config.json");
                exit = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}