package com.flight.booking.Model;

public class FlightSearch {


    private String source;
    private String destination;
    private String date;
    private int adult;
    private int child;
    private int infant;


    // Getters and Setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }


    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }


    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }


    public int getAdult() { return adult; }
    public void setAdult(int adult) { this.adult = adult; }


    public int getChild() { return child; }
    public void setChild(int child) { this.child = child; }


    public int getInfant() { return infant; }
    public void setInfant(int infant) { this.infant = infant; }


}
