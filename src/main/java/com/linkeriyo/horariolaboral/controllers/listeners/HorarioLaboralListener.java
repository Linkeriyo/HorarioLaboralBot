package com.linkeriyo.horariolaboral.controllers.listeners;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.linkeriyo.horariolaboral.controllers.MinebotFinal;
import edu.emory.mathcs.backport.java.util.Collections;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HorarioLaboralListener extends ListenerAdapter {

    SimpleDateFormat inputSDF = new SimpleDateFormat("d 'de' MMMM/yyyy", Locale.forLanguageTag("es-ES"));
    SimpleDateFormat outputSDF = new SimpleDateFormat("dd/MM/yyyy");

    private void appendRowToSheet(Date date, double hours, String comment) throws GeneralSecurityException, IOException {
        GoogleCredentials credentials;
        try (FileInputStream serviceAccountStream = new FileInputStream("files/client_secret.json")) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
        }
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String APPLICATION_NAME = "Linkeribot";
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
        final String spreadsheetId = "1nYzFvywe2NN9MxQmQCM9QajQpS-TvYfHa7qBF5l6wqc";
        final String range = "After1!A2:C";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();

        List<String> values = new ArrayList<>();
        values.add(outputSDF.format(date));
        values.add(String.valueOf(hours));
        values.add(comment);
        ValueRange requestBody = new ValueRange();
        requestBody.setValues(Collections.singletonList(values));

        service.spreadsheets().values()
                .append(spreadsheetId, range, requestBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }

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
                String[] splitMessage = msg.getContentRaw().split(":");

                String stringDate = splitMessage[0];
                String stringHoras = splitMessage[1].split(" ")[0];
                String stringComment = splitMessage[1].split("\"")[1];

                Date date = inputSDF.parse(stringDate + "/" + LocalDate.now().getYear());
                double hours = Double.parseDouble(splitMessage[1].split(" ")[1]);

                appendRowToSheet(date, hours, stringComment);

                MinebotFinal.getJda().retrieveUserById("154268434090164226").queue(
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}