package com.linkeriyo.horariolaboral.controllers;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

import static com.linkeriyo.horariolaboral.controllers.HorarioLaboral.monthSDF;
import static com.linkeriyo.horariolaboral.controllers.HorarioLaboral.outputSDF;

public class SheetsAccess {

    Sheets service;
    final String sheetId = "1nYzFvywe2NN9MxQmQCM9QajQpS-TvYfHa7qBF5l6wqc";

    public SheetsAccess() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials;
        try (FileInputStream serviceAccountStream = new FileInputStream("files/client_secret.json")) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
        }
        final HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String APPLICATION_NAME = "Linkeribot";
        final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private List<List<Object>> getGridFromRange(String range) {
        try {
            return service.spreadsheets().values().get(sheetId, range).execute().getValues();
        } catch (IOException e) {
            return null;
        }
    }

    public Map<String, String> getGeneralResults() {
        List<List<Object>> grid = getGridFromRange("StatsGenerales");
        Map<String, String> result = new HashMap<>();

        for (List<Object> row : grid) {
            if (!row.isEmpty()) {
                result.put((String) row.get(0), (String) row.get(1));
            }
        }

        return result;
    }

    public boolean appendRow(Date date, double hours, String comment) {
        try {
            final String month = monthSDF.format(date);
            final String range = StringUtils.capitalize(month) + "!A2:C";

            List<String> row = new ArrayList<>();
            row.add(outputSDF.format(date));
            row.add(String.valueOf(hours));
            row.add(comment);
            ValueRange requestBody = new ValueRange();
            requestBody.setValues(Collections.singletonList(row));

            service.spreadsheets()
                    .values()
                    .append(sheetId, range, requestBody)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute();
            System.out.println("Row appended to " + range + " " + row);
            return true;
        } catch (IOException e) {
            //If sheet does not exist it creates it
            try {
                String sheetTitle = StringUtils.capitalize(monthSDF.format(date));
                AddSheetRequest addSheetRequest = new AddSheetRequest()
                        .setProperties(new SheetProperties().setTitle(sheetTitle));
                Request request = new Request().setAddSheet(addSheetRequest);
                BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                        .setRequests(Collections.singletonList(request));
                service.spreadsheets()
                        .batchUpdate(sheetId, batchUpdateRequest)
                        .execute();

                addNewSheetHeader(sheetTitle);
                return appendRow(date, hours, comment);
            } catch (IOException exception) {
                exception.printStackTrace();
                return false;
            }
        }
    }

    private List<Object> newRow(String[] args) {
        return new ArrayList<>(Arrays.asList(args));
    }

    private void addNewSheetHeader(String sheetTitle) throws IOException {
        List<String> row = new ArrayList<>();
        row.add("Fecha");
        row.add("Horas extras");
        row.add("Comentario");
        ValueRange requestBody = new ValueRange();
        requestBody.setValues(Collections.singletonList(row));
        service.spreadsheets()
                .values()
                .append(sheetId, sheetTitle + "!A1:C1", requestBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }
}
