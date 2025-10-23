package com.colordiary;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;


public class WeatherWindow extends JFrame {
    private JTextField cityField;
    private JTextArea weatherArea;
    private HttpClient httpClient;

    public WeatherWindow() {
        setTitle("Weather");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        httpClient = HttpClient.newHttpClient();
        // top panel
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("City:"));
        cityField = new JTextField("Cochin", 15);
        JButton fetchBtn = new JButton("Fetch Weather");
        topPanel.add(cityField);
        topPanel.add(fetchBtn);

        add(topPanel, BorderLayout.NORTH);

        weatherArea = new JTextArea();
        weatherArea.setEditable(false);
        weatherArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        weatherArea.setMargin(new Insets(10,10,10,10)); // padding
        weatherArea.setBackground(new Color(245, 248, 255)); // light blue
        weatherArea.setLineWrap(true);
        weatherArea.setWrapStyleWord(true);
        weatherArea.setEditable(false);
        add(new JScrollPane(weatherArea), BorderLayout.CENTER);

        topPanel.setBackground(new Color(230, 240, 255));
        fetchBtn.setBackground(new Color(60, 120, 220));
        fetchBtn.setForeground(Color.WHITE);
        fetchBtn.setFocusPainted(false);
        fetchBtn.setFont(new Font("SansSerif", Font.BOLD, 13));


        fetchBtn.addActionListener(e -> fetchWeather(cityField.getText()));

        setVisible(true);
    }

    private void fetchWeather(String city) {
    weatherArea.setText("Fetching weather for " + city + "...\n");
    new Thread(() -> { // run network call off the EDT
        try {
            // 1. Geocode (only Indian locations)
            String cityParam = city;
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                    URLEncoder.encode(cityParam, StandardCharsets.UTF_8);

            HttpRequest geoReq = HttpRequest.newBuilder().uri(URI.create(geoUrl)).build();
            HttpResponse<String> geoResp = httpClient.send(geoReq, HttpResponse.BodyHandlers.ofString());
            JSONObject geoJson = new JSONObject(geoResp.body());
            JSONArray results = geoJson.optJSONArray("results");
            if (results == null || results.isEmpty()) {
                SwingUtilities.invokeLater(() ->
                    weatherArea.setText("City not found."));
                return;
            }
            JSONObject loc = results.getJSONObject(0);
            double lat = loc.getDouble("latitude");
            double lon = loc.getDouble("longitude");

            // 2. Weather forecast (unchanged)
            String wUrl = String.format(
                    "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current_weather=true" +
                    "&daily=temperature_2m_max,temperature_2m_min,windspeed_10m_max&timezone=auto",
                    lat, lon);
            HttpRequest wReq = HttpRequest.newBuilder().uri(URI.create(wUrl)).build();
            HttpResponse<String> wResp = httpClient.send(wReq, HttpResponse.BodyHandlers.ofString());
            JSONObject wJson = new JSONObject(wResp.body());

                JSONObject current = wJson.getJSONObject("current_weather");
                JSONObject daily = wJson.getJSONObject("daily");

                StringBuilder sb = new StringBuilder();
                sb.append("Current weather in ").append(city).append(":\n")
                  .append("Temperature: ").append(current.getDouble("temperature")).append(" °C\n")
                  .append("Wind Speed: ").append(current.getDouble("windspeed")).append(" km/h\n\n")
                  .append("Forecast (next days):\n");

                JSONArray dates = daily.getJSONArray("time");
                JSONArray maxT = daily.getJSONArray("temperature_2m_max");
                JSONArray minT = daily.getJSONArray("temperature_2m_min");
                JSONArray windMax = daily.getJSONArray("windspeed_10m_max");

                for (int i = 0; i < dates.length(); i++) {
                    sb.append(dates.getString(i))
                      .append(": Min ").append(minT.getDouble(i)).append("°C, Max ")
                      .append(maxT.getDouble(i)).append("°C, Wind Max ")
                      .append(windMax.getDouble(i)).append(" km/h\n");
                }

                SwingUtilities.invokeLater(() -> weatherArea.setText(sb.toString()));

            } catch (IOException | InterruptedException ex) {
                SwingUtilities.invokeLater(() -> weatherArea.setText("Error fetching weather: " + ex.getMessage()));
            }
        }).start();
    }
}

