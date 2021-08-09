package com.linkeriyo.horariolaboral.controllers.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public class ConsoleInputListener implements Runnable {

    JDA jda;

    public ConsoleInputListener(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void run() {
        Scanner scan = new Scanner(System.in).useDelimiter("\n").useLocale(new Locale("es", "ES"));

        while (true) {
            String input = scan.next();

            if (input.equals("quit")) {
                System.err.println("Closing the bot...");
                jda.shutdown();
                System.exit(0);
            }

            if (input.equals("ping")) {
                System.out.println(jda.getGatewayPing() + "ms");
            } else {
                String[] args = input.split(" ");

                List<TextChannel> textChannels = Objects.requireNonNull(jda.getGuildById("580421667336224769")).getTextChannels();

                boolean channelFound = false;
                for (TextChannel channel : textChannels) {
                    if (args[0].equals(channel.getName())) {
                        channelFound = true;
                        input = input.substring(args[0].length());
                        channel.sendMessage(input).queue();
                        System.out.println("#" + args[0] + " -> " + input);
                    }
                }

                if (!channelFound) {
                    System.out.println("No se ha encontrado el canal " + args[0] + ".");
                }
            }
        }
    }
}