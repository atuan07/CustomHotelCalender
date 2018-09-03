package com.atuan.datepickerlibrary;

import java.util.List;

/**
 * Created by atuan on 2018/9/1.
 */
public class DateInfo {
    private String date;
    private List<DayInfo> list;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<DayInfo> getList() {
        return list;
    }

    public void setList(List<DayInfo> list) {
        this.list = list;
    }
}
