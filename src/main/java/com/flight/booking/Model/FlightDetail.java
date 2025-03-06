package com.flight.booking.Model;

public class FlightDetail {

    private boolean oneWay ;
    private int avalSeats ;
    private int stops ;
    private String src ;
    private String dest ;
    private String depTime ;
    private String arrTime ;
    private String carrierCode ;
    private String flightNo ;
    private String duration ;
    private String price ;
    private String cabin ;
    private String classs ;
    private String baggage ;

    public void setOneWay(boolean oneWay){
        this.oneWay = oneWay ;
    }

    public void setStops(int stops){
        this.stops = stops ;
    }

    public void setAvalSeats(int avalSeats){
        this.avalSeats = avalSeats ;
    }

    public void setSrc(String src){
        this.src = src ;
    }

    public void setDest(String dest){
        this.dest = dest ;
    }

    public void setDepTime(String depTime){
        this.depTime = depTime ;
    }

    public void setArrTime(String arrTime){
        this.arrTime = arrTime ;
    }

    public void setCarrierCode(String carrierCode){
        this.carrierCode = carrierCode ;
    }

    public void setFlightNo(String flightNo){
        this.flightNo = flightNo ;
    }

    public void setDuration(String duration){
        this.duration = duration ;
    }

    public void setPrice(String price){
        this.price = price ;
    }

    public void setCabin(String cabin){
        this.cabin = cabin ;
    }

    public void setClasss(String classs){
        this.classs = classs ;
    }

    public void setBaggage(String baggage){
        this.baggage = baggage ;
    }

    public boolean getOneWay() {
        return oneWay;
    }
    
    public int getAvalSeats() {
        return avalSeats;
    }
    
    public int getStops() {
        return stops;
    }
    
    public String getSrc() {
        return src;
    }
    
    public String getDest() {
        return dest;
    }
    
    public String getDepTime() {
        return depTime;
    }
    
    public String getArrTime() {
        return arrTime;
    }
    
    public String getCarrierCode() {
        return carrierCode;
    }
    
    public String getFlightNo() {
        return flightNo;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public String getPrice() {
        return price;
    }
    
    public String getCabin() {
        return cabin;
    }
    
    public String getClasss() {
        return classs;
    }
    
    public String getBaggage() {
        return baggage;
    }
    



}

