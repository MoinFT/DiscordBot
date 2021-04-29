package de.moinFT.main;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

public class RKICorona {
    public static int[] months;
    public static String r = "";

    public static double incidenceValue() {
        LocalDateTime dateTime = LocalDateTime.now();
        int dayToday = dateTime.getDayOfMonth();
        int monthToday = dateTime.getMonthValue();
        int yearToday = dateTime.getYear();

        int dayPast = dayToday - 7;
        int monthPast = monthToday;
        int yearPast = yearToday;

        if (yearToday % 4 == 0) {
            months = new int[]{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        } else {
            months = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        }

        if (dayPast < 1) {
            monthPast -= 1;

            if (monthPast < 1) {
                monthPast = 12;

                yearPast -= 1;
            }
            dayPast = months[monthPast - 1] - (7 - dayToday);
        }


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/Covid19_03_hubv/FeatureServer/0/query?where=Landkreis%20%3D%20%27LK%20NIENBURG%20(WESER)%27%20AND%20Meldedatum%20%3E%3D%20TIMESTAMP%20%27" + dayPast + "/" + monthPast + "/" + yearPast + "%27%20AND%20Meldedatum%20%3C%3D%20TIMESTAMP%20%27" + dayToday + "/" + monthToday + "/" + yearToday + "%27&outFields=Meldedatum,IstErkrankungsbeginn,NeuerFall,AnzahlFall&outSR=4326&f=json"))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(event -> r = event)
                .join();

        JSONObject allData = new JSONObject(r);

        JSONArray features = allData.getJSONArray("features");

        double einwohnerZahlen = 1.21390;
        int count = 0;

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);

            count += feature.getJSONObject("attributes").getInt("AnzahlFall");
        }

        return Math.round((count / einwohnerZahlen) * 100) / 100.;
    }
}
