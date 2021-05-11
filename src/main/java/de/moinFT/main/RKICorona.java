package de.moinFT.main;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RKICorona {
    private static double Incidence;

    public static double incidenceValue() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.corona-zahlen.org/districts/03256/history/incidence/2"))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept((event) -> {
                    JSONObject allData = new JSONObject(event);
                    JSONArray dataHistory = allData.getJSONObject("data").getJSONObject("03256").getJSONArray("history");
                    JSONObject data = dataHistory.getJSONObject(dataHistory.length() - 1);

                    Incidence = data.getDouble("weekIncidence");
                })
                .join();

        return Math.round(Incidence * 100) / 100.;
    }
}
