package com.example.demo1;

class Weather {
    private String date;
    private String weather;
    private String high;
    private String low;
    private String wind;
    private String createTime;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public Weather() {
        super();
    }

    @Override
    public String toString() {
        return "概况 [日期=" + date + ", 天气=" + weather + ", 最高温度=" + high + ", 最低温度=" + low + ", 风向=" + wind + ", 更新时间=" + createTime + "]";
    }

}