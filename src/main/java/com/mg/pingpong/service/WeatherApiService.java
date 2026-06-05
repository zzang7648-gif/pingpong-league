package com.mg.pingpong.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mg.pingpong.entity.Weather;
import com.mg.pingpong.repository.WeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URI;

@Service
public class WeatherApiService {

    private final RestTemplate restTemplate;
    private final WeatherRepository weatherRepository;

    // 생성자 주입 방식: 스프링이 restTemplate와 weatherRepository를 알아서 연결해줍니다.
    public WeatherApiService(RestTemplate restTemplate, WeatherRepository weatherRepository) {
        this.restTemplate = restTemplate;
        this.weatherRepository = weatherRepository;
    }

    public String fetchWeatherData() {
        String serviceKey = "LJSEJ%2Bb6wLcg3gSGE1om97v86nYvHrOqqkhsnyKZdONL0RUNJLaZVCUYYLAuUcBX2tnfSU8hHNQni04f%2BWkHPQ%3D%3D";
        
        String urlString = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?" +
                           "serviceKey=" + serviceKey +
                           "&dataType=JSON" +
                           "&base_date=20260604" +
                           "&base_time=0500" +
                           "&nx=60&ny=127";

        try {
            return restTemplate.getForObject(new URI(urlString), String.class);
        } catch (Exception e) {
            return "에러 발생: " + e.getMessage();
        }
    }

    public void saveWeatherToDb() {
        String json = fetchWeatherData();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            String temp = "0";
            String pop = "0";

            for (JsonNode item : items) {
                String category = item.path("category").asText();
                String value = item.path("fcstValue").asText();
                
                if ("TMP".equals(category)) temp = value;
                if ("POP".equals(category)) pop = value;
            }

            // DB 저장
            Weather weather = Weather.builder()
                                    .temp(temp)
                                    .pop(pop)
                                    .skyStatus("3")
                                    .build();
            weatherRepository.save(weather);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}