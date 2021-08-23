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