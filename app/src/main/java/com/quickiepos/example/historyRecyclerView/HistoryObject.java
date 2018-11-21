package com.quickiepos.example.historyRecyclerView;

public  class HistoryObject {
    private String consultId;
    private String time;

    public HistoryObject(String rideId, String time){
        this.consultId = rideId;
        this.time = time;
    }

    public String getConsultId(){return consultId;}
    public void setConsultId(String consultId) {
        this.consultId = consultId;
    }

    public String getTime(){return time;}
    public void setTime(String time) {
        this.time = time;
    }
}