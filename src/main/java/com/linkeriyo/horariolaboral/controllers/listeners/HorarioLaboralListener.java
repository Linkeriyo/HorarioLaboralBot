package com.linkeriyo.horariolaboral.controllers.listeners;

import com.linkeriyo.horariolaboral.controllers.HorarioLaboral;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.*;

import static com.linkeriyo.horariolaboral.controllers.HorarioLaboral.*;

public class HorarioLaboralListener extends ListenerAdapter {

    private MessageEmbed generateEmbed(Date date, double hours, String comment) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Horas de Juan Carlos", null);
        eb.setDescription(valueOf(hours) + " horas extra: " + comment);
        eb.setFooter(outputSDF.format(date));
        return eb.build();
    }

    private String valueOf(double hours) {
        String toReturn = String.valueOf(hours);
        if (toReturn.endsWith(".0")) {
            toReturn = toReturn.substring(0, toReturn.length() - 2);
        }
        return toReturn;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message msg = event.getMessage();
        try {
            if (!msg.isFromGuild() && msg.getAuthor().getId().equals("154268434090164226")) {
                if (msg.getContentRaw().toLowerCase().startsWith("general")) {
                    Map<String, String> generalResults = getSheetsAccess().getGeneralResults();
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Resultados generales");
                    generalResults.forEach((key, value) -> {
                        builder.addField(key, value, false);
                    });
                    msg.replyEmbeds(builder.build()).queue();
                } else {
                    String[] splitMessage = msg.getContentRaw().split(":");

                    String stringDate = splitMessage[0];
                    String stringHoras = splitMessage[1].split(" ")[0];
                    String stringComment = splitMessage[1].split("\"")[1];

                    Date date;

                    if (stringDate.toLowerCase().startsWith("hoy")) {
                        date = new Date();
                    } else {
                        date = inputSDF.parse(stringDate + "/" + LocalDate.now().getYear());
                    }
                    double hours = Double.parseDouble(splitMessage[1].split(" ")[1]);

                    if (HorarioLaboral.getSheetsAccess().appendRow(date, hours, stringComment)) {
                        HorarioLaboral.getJda().retrieveUserById("154268434090164226").queue(
                                linkeriyo -> linkeriyo.openPrivateChannel().queue(
                                        privateChannel -> {
                                            privateChannel.sendMessageEmbeds(
                                                    generateEmbed(date, hours, stringComment)
                                            ).queue();
                                            privateChannel.sendMessage(
                                                    "Mensaje para Andrés:\n" +
                                                            outputSDF.format(date) + "   " + valueOf(hours + 4) + " horas   (" + stringComment + ")"
                                            ).queue();
                                        })
                        );
                    } else {
                        HorarioLaboral.getJda().retrieveUserById("154268434090164226").queue(
                                linkeriyo -> linkeriyo.openPrivateChannel().queue(
                                        privateChannel -> {
                                            privateChannel.sendMessage(
                                                    "Ha ocurrido un error al añadir la línea a Sheets."
                                            ).queue();
                                        })
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}