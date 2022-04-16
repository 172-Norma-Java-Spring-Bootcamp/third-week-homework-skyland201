package com.example.homework2.controller;

import com.example.homework2.response.DailyWeatherResponse;
import com.example.homework2.response.WeeklyWeatherResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController()
@RequestMapping("/weather")
public class WeatherController {

    private final String API_KEY = "e627cb07150a408ea3c205652221504";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/daily/{location}")
    public ResponseEntity getCurrentWeatherData(@PathVariable String location) {
        if (ObjectUtils.isEmpty(location)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("location cannot be empty");
        }

        RestTemplate restTemplate = new RestTemplate();
        String liveWeatherUrl = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY + "&q=" + location + "&aqi=no&days=1";
        ResponseEntity<String> response = restTemplate.getForEntity(liveWeatherUrl, String.class);
        ObjectNode responseObj = null;
        try {
            responseObj = (ObjectNode) objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something is not right");
        }
        DailyWeatherResponse dailyWeatherResponse = convertToDailyWeatherResponse((ObjectNode) responseObj.get("forecast").get("forecastday").get(0));
        dailyWeatherResponse.setName(location);
        return ResponseEntity.ok(dailyWeatherResponse);
    }

    @GetMapping("/weekly/{location}")
    public ResponseEntity getWeeklyWeatherData(@PathVariable String location, @RequestParam(required = false) String days) {
        try {
            validate(location, days);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        // assign 7 if days is empty to get weekly data
        days = ObjectUtils.isEmpty(days) ? "7" : days;
        RestTemplate restTemplate = new RestTemplate();
        String liveWeatherUrl = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY + "&q=" + location + "&aqi=no&days=" + days;
        ResponseEntity<String> response = restTemplate.getForEntity(liveWeatherUrl, String.class);
        ObjectNode responseObj = null;
        try {
            responseObj = (ObjectNode) objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something is not right");
        }

        WeeklyWeatherResponse weeklyWeatherResponse = convertToWeeklyWeatherResponse((ArrayNode) responseObj.get("forecast").get("forecastday"), location);
        weeklyWeatherResponse.setStartDate(weeklyWeatherResponse.getDays().get(0).getDate());
        return ResponseEntity.ok(weeklyWeatherResponse);
    }

    @GetMapping("/monthly/{location}")
    public ResponseEntity getMonthlyWeatherData(@PathVariable String location) {
        if (ObjectUtils.isEmpty(location)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("location cannot be empty");
        }

        RestTemplate restTemplate = new RestTemplate();
        String liveWeatherUrl = "http://api.weatherapi.com/v1/forecast.json?key=" + API_KEY + "&q=" + location + "&aqi=no&days=30";
        ResponseEntity<String> response = restTemplate.getForEntity(liveWeatherUrl, String.class);
        ObjectNode responseObj = null;
        try {
            responseObj = (ObjectNode) objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something is not right");
        }

        WeeklyWeatherResponse weeklyWeatherResponse = convertToWeeklyWeatherResponse((ArrayNode) responseObj.get("forecast").get("forecastday"), location);
        weeklyWeatherResponse.setStartDate(weeklyWeatherResponse.getDays().get(0).getDate());
        return ResponseEntity.ok(weeklyWeatherResponse);
    }

    private void validate(String location, String days) throws Exception {
        if (ObjectUtils.isEmpty(location)) {
            throw new Exception("location cannot be empty");
        }
        if (!ObjectUtils.isEmpty(days)) {
            int daysInt;
            try {
                daysInt = Integer.parseInt(days);
            } catch (Exception e) {
                throw new Exception("days should be integer");
            }
            if (daysInt < 1 || daysInt > 10) {
                throw new Exception("days should be in range 1 to 10");
            }
        }
    }

    private WeeklyWeatherResponse convertToWeeklyWeatherResponse(ArrayNode objectNode, String location) {
        WeeklyWeatherResponse weeklyWeatherResponse = new WeeklyWeatherResponse();
        for (int i = 0; i < objectNode.size(); i++) {
            DailyWeatherResponse dailyWeatherResponse = convertToDailyWeatherResponse((ObjectNode) objectNode.get(i));
            dailyWeatherResponse.setName(location);
            weeklyWeatherResponse.getDays().add(dailyWeatherResponse);
        }
        return weeklyWeatherResponse;
    }

    private DailyWeatherResponse convertToDailyWeatherResponse(ObjectNode objectNode) {
        DailyWeatherResponse dailyWeatherResponse = new DailyWeatherResponse();
        dailyWeatherResponse.setCondition(objectNode.get("day").get("condition").get("text").asText());
        dailyWeatherResponse.setDate(objectNode.get("date").asText());
        dailyWeatherResponse.setMaxtemp_c(objectNode.get("day").get("maxtemp_c").asText());
        dailyWeatherResponse.setMaxtemp_f(objectNode.get("day").get("maxtemp_f").asText());
        dailyWeatherResponse.setMintemp_c(objectNode.get("day").get("mintemp_c").asText());
        dailyWeatherResponse.setMintemp_f(objectNode.get("day").get("mintemp_f").asText());
        dailyWeatherResponse.setAvgtemp_c(objectNode.get("day").get("avgtemp_c").asText());
        dailyWeatherResponse.setAvgtemp_f(objectNode.get("day").get("avgtemp_f").asText());
        dailyWeatherResponse.setMaxwind_mph(objectNode.get("day").get("maxwind_mph").asText());
        dailyWeatherResponse.setMaxwind_kph(objectNode.get("day").get("maxwind_kph").asText());
        dailyWeatherResponse.setTotalprecip_mm(objectNode.get("day").get("totalprecip_mm").asText());
        dailyWeatherResponse.setTotalprecip_in(objectNode.get("day").get("totalprecip_in").asText());
        dailyWeatherResponse.setAvgvis_km(objectNode.get("day").get("avgvis_km").asText());
        dailyWeatherResponse.setAvgvis_miles(objectNode.get("day").get("avgvis_miles").asText());
        dailyWeatherResponse.setAvghumidity(objectNode.get("day").get("avghumidity").asText());
        dailyWeatherResponse.setDaily_will_it_rain(objectNode.get("day").get("daily_will_it_rain").asText());
        dailyWeatherResponse.setDaily_chance_of_rain(objectNode.get("day").get("daily_chance_of_rain").asText());
        dailyWeatherResponse.setDaily_will_it_snow(objectNode.get("day").get("daily_will_it_snow").asText());
        dailyWeatherResponse.setDaily_chance_of_snow(objectNode.get("day").get("daily_chance_of_snow").asText());
        return dailyWeatherResponse;
    }
}
