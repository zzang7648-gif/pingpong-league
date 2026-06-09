package com.mg.pingpong.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.pingpong.entity.Weather;
import com.mg.pingpong.repository.WeatherRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class WeatherApiService {

    private final RestTemplate restTemplate;
    private final WeatherRepository weatherRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SERVICE_KEY = "LJSEJ%2Bb6wLcg3gSGE1om97v86nYvHrOqqkhsnyKZdONL0RUNJLaZVCUYYLAuUcBX2tnfSU8hHNQni04f%2BWkHPQ%3D%3D";

    public WeatherApiService(RestTemplate restTemplate, WeatherRepository weatherRepository) {
        this.restTemplate = restTemplate;
        this.weatherRepository = weatherRepository;
    }

    @PostConstruct
    public void init() {
        saveWeatherData(); // ✅ 이름 통일
    }

    public String fetchForecastData(String baseDate) {
        String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?" +
                "serviceKey=" + SERVICE_KEY +
                "&dataType=JSON&base_date=" + baseDate +
                "&base_time=0500&nx=60&ny=127&numOfRows=1000";
        try {
            return restTemplate.getForObject(new URI(url), String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String fetchForecastData(String baseDate, String baseTime) {
        String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?" +
                "serviceKey=" + SERVICE_KEY +
                "&dataType=JSON&base_date=" + baseDate +
                "&base_time=" + baseTime + "&nx=60&ny=127&numOfRows=1000";
        try {
            return restTemplate.getForObject(new URI(url), String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveWeatherData() { // ✅ init()과 이름 일치
        weatherRepository.deleteAll(); // ✅ 추가

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        String forecastJson = fetchForecastData(today.format(fmt));

        for (int i = -1; i <= 5; i++) {
            String targetDate = today.plusDays(i).format(fmt);
            String json = (i < 0) ? fetchForecastData(targetDate, "0500") : forecastJson;

            if (json == null) {
                System.out.println("[경고] " + targetDate + " JSON null → 스킵");
                continue;
            }

            try {
                JsonNode items = objectMapper.readTree(json)
                        .path("response").path("body").path("items").path("item");

                String tmn = null, tmx = null, pop = null, sky = null;

                for (JsonNode item : items) {
                    if (!item.path("fcstDate").asText().equals(targetDate)) continue;
                    String category = item.path("category").asText();
                    String value    = item.path("fcstValue").asText();
                    switch (category) {
                        case "TMN" -> { if (tmn == null) tmn = value; }
                        case "TMX" -> { if (tmx == null) tmx = value; }
                        case "POP" -> { if (pop == null) pop = value; }
                        case "SKY" -> { if (sky == null) sky = value; }
                    }
                }

                if (tmn == null || tmx == null) {
                    for (JsonNode item : items) {
                        if (!item.path("fcstDate").asText().equals(targetDate)) continue;
                        if ("TMP".equals(item.path("category").asText())) {
                            String v = item.path("fcstValue").asText();
                            if (tmn == null) tmn = v;
                            tmx = v;
                        }
                    }
                }

                if (tmn == null && tmx == null && pop == null) {
                    System.out.println("[경고] " + targetDate + " 데이터 없음 → 스킵");
                    continue;
                }

                Weather weather = Weather.builder()
                        .targetDate(targetDate)
                        .temp((tmn != null ? tmn : "?") + "/" + (tmx != null ? tmx : "?"))
                        .pop(pop != null ? pop : "0")
                        .skyStatus(sky != null ? sky : "1")
                        .build();

                weatherRepository.save(weather);
                System.out.println("[저장] " + targetDate);

            } catch (Exception e) {
                System.err.println("[에러] " + targetDate + ": " + e.getMessage());
            }
        }
    }
}