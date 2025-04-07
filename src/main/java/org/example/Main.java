package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Scanner lässt User Eingaben machen
        Scanner scanner = new Scanner(System.in);
        System.out.println("Von welchem Bahnhof möchten Sie eine Verbindung suchen?");
        String abfahrtsort = bahnhofFinden(scanner.nextLine());
        String abfahrtsortUrl = abfahrtsort.replace(" ", "%20");

        System.out.println("Zu welchem Bahnhof möchten Sie eine Verbindung suchen?");
        String zielort = bahnhofFinden(scanner.nextLine());
        String zielortUrl = zielort.replace(" ", "%20");

        try {
            JsonObject data = datenBereitstellen("https://transport.opendata.ch/v1/connections?from="
                    + abfahrtsortUrl + "&to=" + zielortUrl);
            JsonArray connections = data.getAsJsonArray("connections");
            if (connections != null && connections.size() > 0) {
                int anzahlVerbindungenAusgeben = 0;
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
                        anzahlVerbindungenAusgeben++;
                        if (anzahlVerbindungenAusgeben >= 3) {
                            break;
                        }
                    }
                }
            } else {
                System.out.println("Keine Verbindungen gefunden.");
                System.out.println("https://transport.opendata.ch/v1/connections?from="
                        + abfahrtsortUrl + "&to=" + zielortUrl);
            }
        } catch (
                Exception e) {
            System.out.println("Fehler: " + e);
        }
    }

    public static void vorschlaegeAusgeben(ArrayList<String> vorschlaege) {
        System.out.println("Wählen sie einen dieser Bahnhöfe aus:");
        for (String s : vorschlaege) {
            System.out.println(s);
        }
    }

    public static JsonObject datenBereitstellen(String urlFuerDaten) {
        try {
            URL url = new URL(urlFuerDaten);
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
            return data;
        } catch (Exception e) {
            System.out.println("Fehler: " + e);
            return null;
        }
    }

    public static String bahnhofFinden(String s) {
        String ort = s;
        String ortUrl = ort.replace(" ", "%20");

        try {
            JsonObject data = datenBereitstellen("http://transport.opendata.ch/v1/locations?query=" + ortUrl);
            JsonArray stations = data.getAsJsonArray("stations");
            if (stations != null && stations.size() > 0) {
                ArrayList<String> vorschlaege = new ArrayList<>();
                for (int i = 0; i < stations.size(); i++) {
                    JsonObject station = stations.get(i).getAsJsonObject();
                    if (station.get("name").getAsString().equals(ort)) {
                        break;
                    } else {
                        if (vorschlaege.size() < 3) {
                            vorschlaege.add(station.get("name").getAsString());
                        }
                    }
                }
                if (!vorschlaege.isEmpty()) {
                    vorschlaegeAusgeben(vorschlaege);
                    Scanner scanner = new Scanner(System.in);
                    bahnhofFinden(scanner.nextLine());
                }

                return ort;

            } else {
                System.out.println("Keine Bahnhöfe gefunden.");
                return ort;
            }
        } catch (Exception e) {
            System.out.println("Fehler: " + e);
            return ort;
        }
    }

}