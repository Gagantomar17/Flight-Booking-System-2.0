package com.flight.booking.Model;

public class FlightWrapper {
    private FlightDetail flightDetail ;
    private String requestBody ;

    public FlightWrapper(FlightDetail flightDetail , String requestBody){
        this.flightDetail = flightDetail ;
        this.requestBody = requestBody ;
    }

    public void setFlightDetail(FlightDetail flightDetail){
        this.flightDetail = flightDetail ;
    }

    public void setRequestBody(String requestBody){
        this.requestBody = requestBody ;

    }

    public FlightDetail getFlightDetail(){
        return flightDetail ;
    }

    public String getRequestBody(){
        return requestBody ;
    }

}
