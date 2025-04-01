package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Scanner lässt User Eingaben machen
        Scanner scanner = new Scanner(System.in);

        // Nimmt Eingaben auf und Speichert sie in Variabeln
        System.out.println("Von welchem Bahnhof möchten Sie eine Verbindung suchen?");
        String abfahrtsort = scanner.nextLine();
        String abfahrtsortUrl = abfahrtsort.replace(" ", "%20");

        System.out.println("Zu welchem Bahnhof möchten Sie eine Verbindung suchen?");
        String zielort = scanner.nextLine();
        String zielortUrl = zielort.replace(" ", "%20");

        try {
            URL url = new URL("https://transport.opendata.ch/v1/connections?from="
                    + abfahrtsortUrl + "&to=" + zielortUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonResponse = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResponse.append(line);
            }
            reader.close();

            // JSON-String parsen
            JsonObject data = JsonParser.parseString(jsonResponse.toString()).getAsJsonObject();


            JsonArray connections = data.getAsJsonArray("connections");
            if (connections != null && connections.size() > 0) {
                int anzahlVerbindungen = 0;
                for (int i = 0; i < connections.size(); i++) {
                    JsonObject connectionObj = connections.get(i).getAsJsonObject();
                    JsonObject from = connectionObj.getAsJsonObject("from");
                    JsonObject from_station = from.getAsJsonObject("station");
                    JsonObject to = connectionObj.getAsJsonObject("to");
                    JsonObject to_station = to.getAsJsonObject("station");
                    if (from_station.get("name").getAsString().equals(abfahrtsort) &&
                            to_station.get("name").getAsString().equals(zielort)) {
                        System.out.println("=================================");
                        System.out.println(from_station.get("name").getAsString());
                        System.out.println(from.get("departure").getAsString());
                        System.out.println("Gleis: " + from.get("platform").getAsString());
                        System.out.println("-->");
                        System.out.println(to_station.get("name").getAsString());
                        System.out.println(to.get("arrival").getAsString());
                        System.out.println("Gleis: " + to.get("platform").getAsString());
                        anzahlVerbindungen++;
                        if (anzahlVerbindungen >= 3){
                            break;
                        }
                    }
                }
            } else {
                System.out.println("Keine Verbindungen gefunden.");
            }
        } catch (
                Exception e) {
            System.out.println("Fehler: " + e);
        }
    }
}