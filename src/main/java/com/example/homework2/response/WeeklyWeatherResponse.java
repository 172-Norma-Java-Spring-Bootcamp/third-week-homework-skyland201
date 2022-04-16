package com.example.homework2.response;

import java.util.ArrayList;
import java.util.List;

public class WeeklyWeatherResponse {
    private String startDate;
    private List<DailyWeatherResponse> days;

    public WeeklyWeatherResponse() {
        days=new ArrayList<>();
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public List<DailyWeatherResponse> getDays() {
        return days;
    }

    public void setDays(List<DailyWeatherResponse> days) {
        this.days = days;
    }
}
