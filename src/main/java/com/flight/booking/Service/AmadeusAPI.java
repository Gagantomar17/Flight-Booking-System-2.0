package com.flight.booking.Service;

import com.flight.booking.Model.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class AmadeusAPI {

    private static String repriceResponse = "null";
    private static String accessToken = "null";

    @Value("${amadeus.oath.url}")
    private String oath_url ;


    @Value("${amadeus.client.id}")
    private String client_id ;


    @Value("${amadeus.client.secret}")
    private String client_secret ;


    @Value("${amadeus.reprice.url}")
    private String reprice_url ;


    @Value("${amadeus.booking.url}")
    private String booking_url ;

    public JSONObject getFlights(String source, String destination, String date, int adult, int child, int infant){


        System.out.print(oath_url);


        String responseBody = null ;
        JSONObject responseObj = new JSONObject() ;
       
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(oath_url);
            post.addHeader("Content-Type", "application/x-www-form-urlencoded");
            StringEntity entity = new StringEntity("grant_type=client_credentials&client_id="+client_id+"&client_secret="+client_secret);
            post.setEntity(entity);


            try (CloseableHttpResponse tokenResponse = httpClient.execute(post)) {
                String tokenJsonResponse = EntityUtils.toString(tokenResponse.getEntity());
                JSONObject tokenJsonObject = new JSONObject(tokenJsonResponse);
                if (!tokenJsonObject.has("access_token")) {
                    throw new RuntimeException("Access token not found in response: " + tokenJsonResponse);
                }

                accessToken = tokenJsonObject.getString("access_token");
                System.out.println("Here is access token " + accessToken) ;

                HttpGet flightRequest = new HttpGet("https://test.api.amadeus.com/v2/shopping/flight-offers?originLocationCode="+source+"&destinationLocationCode="+destination+"&departureDate="+date+"&adults="+adult+"&children="+child+"&infants="+infant+"&nonStop=true&max=250");
                flightRequest.addHeader("Authorization", "Bearer " + accessToken);
                flightRequest.addHeader("Accept", "application/vnd.amadeus+json");


                try (CloseableHttpResponse flightResponse = httpClient.execute(flightRequest)) {
                    responseBody = EntityUtils.toString(flightResponse.getEntity());
                    responseObj = new JSONObject(responseBody);
                    System.out.println("Flight Offers Response: " + responseBody);
                }
            } catch (Exception e){
                e.printStackTrace();
                responseBody = "Error occurred: " + e.getMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
            responseBody = "Error occurred: " + e.getMessage();
        }
        return responseObj;
    };

    public List<FlightWrapper> responseParsing(String responseBody){
        ObjectMapper objectMapper = new ObjectMapper();
        List<FlightWrapper> flights = new ArrayList<>();
        try{
            JsonNode rootNode = objectMapper.readTree(responseBody); // complete api response
            JsonNode dataArray = rootNode.get("data"); // it contains the flights data 
        
            if(dataArray !=null && dataArray.isArray() ){
                for(JsonNode flightNode : dataArray){

                    JSONObject flightObject = new JSONObject(flightNode.toString());
                    FlightDetail flightModel = new FlightDetail();

                    JsonNode itinerariesNode = flightNode.path("itineraries");
                    JsonNode travelerPricingsNode = flightNode.path("travelerPricings");
                    boolean oneWay = flightNode.path("oneWay").asBoolean();
                    int avalSeats = flightNode.path("numberOfBookableSeats").asInt(0);
                    String price = flightNode.get("price").path("total").asText("");
                    String currency = flightNode.get("price").path("currency").asText("");
                    price = price + " " + currency ;

                    flightModel.setAvalSeats(avalSeats);
                    flightModel.setOneWay(oneWay);
                    flightModel.setPrice(price);

                    if (itinerariesNode != null && itinerariesNode.isArray()) {

                        for (JsonNode itineraryNode : itinerariesNode) {
                            String carrierCode = itineraryNode.path("carrierCode").asText("");
                            String duration = itineraryNode.path("duration").asText("").substring(2);

                            flightModel.setCarrierCode(carrierCode);
                            flightModel.setDuration(duration);

                            JsonNode segmentsNode = itineraryNode.path("segments");
                            if (segmentsNode != null && segmentsNode.isArray()) {   
                                for(JsonNode segmentNode : segmentsNode){
                                    String flightNo = segmentNode.path("number").asText("");
                                    String src = segmentNode.path("departure").path("iataCode").asText(""); // Get source IATA code
                                    String dest = segmentNode.path("arrival").path("iataCode").asText(""); // Get destination IATA code
                                    String depTime = segmentNode.path("departure").path("at").asText("").substring(11, 19); 
                                    String arrTime = segmentNode.path("arrival").path("at").asText("").substring(11, 19); 
                                    int stops = segmentNode.path("numberOfStops").asInt();

                                    flightModel.setSrc(src);
                                    flightModel.setDest(dest);
                                    flightModel.setDepTime(depTime);
                                    flightModel.setArrTime(arrTime);
                                    flightModel.setStops(stops);
                                    flightModel.setFlightNo(flightNo);
                                }
                            }
                        }
                    }

                    if(travelerPricingsNode != null && travelerPricingsNode.isArray()){

                        for(JsonNode travelerPricingNode : travelerPricingsNode){
                            JsonNode fareDetailsBySegment = travelerPricingNode.path("fareDetailsBySegment");

                            if(fareDetailsBySegment !=null && fareDetailsBySegment.isArray()){
                                for(JsonNode fareDetailBySegment : fareDetailsBySegment){
                                    String cabin = fareDetailBySegment.path("cabin").asText("");
                                    String classs = fareDetailBySegment.path("class").asText("");
                                    String bag = fareDetailBySegment.path("includedCheckedBags").path("weight").asText("0");
                                    String baggageUnit = fareDetailBySegment.path("weightUnit").asText("KG");
                                    String baggage = bag + " " + baggageUnit ; 

                                    flightModel.setBaggage(baggage);
                                    flightModel.setCabin(cabin);
                                    flightModel.setClasss(classs);
                                }
                                
                            }

                        }
                        
                    }

                    flights.add(new FlightWrapper(flightModel, flightObject.toString())); 
                    
                }
                
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return flights ;
    }

    public String repriceFlights(String response){
        System.out.println("Response pass : " + response);
        System.out.flush();

        JSONObject jsonObject = new JSONObject();
        JSONObject responseObj = new JSONObject(response);
        JSONArray responseArray = new JSONArray();
        responseArray.put(responseObj);
        jsonObject.put("flightOffers", responseArray);
        jsonObject.put("type", "flight-offers-pricing");
        
        JSONObject responseBody = new JSONObject();
        responseBody.put("data", jsonObject);
        System.out.println("Request Body : " + responseBody.toString());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpPost post = new HttpPost(reprice_url);
            post.addHeader("Authorization", "Bearer " + accessToken);
            post.addHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(responseBody.toString()));

            try(CloseableHttpResponse httpResponse = httpClient.execute(post)) {
                repriceResponse = EntityUtils.toString(httpResponse.getEntity()); 
                System.out.println("Reprice Reprice Response : " + repriceResponse);
            } catch (Exception e) {
                e.printStackTrace();
                repriceResponse = "Error : " + e.getMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            repriceResponse = "Error Error : " + e.getMessage() ;
        }

        return repriceResponse ;
    }

    public boolean isPriceUnchanged(String repriceResponse , String price){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(repriceResponse);
            String reprice = rootNode.path("data").path("flightOffers").get(0).path("price").path("total").asText()  ;
            System.out.println("Fetched reprice value : " + reprice);
            System.out.flush();

            price = price.replaceAll("[^0-9.]", "");
            System.out.println("Fetched price value : " + price);
            System.out.flush();

            float beforePrice = Float.parseFloat(price);
            float afterPrice = Float.parseFloat(reprice);
            if(beforePrice == afterPrice){
                System.out.println("Price is unchanged ");
                return true ;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false ;
    }

    public String orderReqBody(String requestData) {
        JSONObject requestBody = new JSONObject();

        JSONObject requestDataJson = new JSONObject(requestData);
        JSONArray travellersArray = requestDataJson.getJSONArray("travellerDetails");
        JSONArray contactsArray = requestDataJson.getJSONArray("agentDetails");
        JSONArray flightOfferArray = requestDataJson.getJSONObject("repriceDetails").getJSONObject("data").getJSONArray("flightOffers");

        // Create data object
        JSONObject data = new JSONObject();
        data.put("type", "flight-order");
        data.put("flightOffers", flightOfferArray);
        data.put("travelers", travellersArray);
        data.put("contacts", contactsArray);

        // Adding remarks
        JSONObject remarks = new JSONObject();
        JSONObject general = new JSONObject();
        general.put("subType", "GENERAL_MISCELLANEOUS");
        general.put("text", "ONLINE BOOKING FROM Tomar Travel Pvt Ltd");
        JSONArray generalArray = new JSONArray();
        generalArray.put(general);
        remarks.put("general", generalArray);
        data.put("remarks", remarks);

        // Adding ticketingAgreement
        JSONObject ticketingAgreement = new JSONObject();
        ticketingAgreement.put("option", "DELAY_TO_CANCEL");
        ticketingAgreement.put("delay", "6D");
        data.put("ticketingAgreement", ticketingAgreement);

        // Final request body
        requestBody.put("data", data);

        return requestBody.toString();
    }

    public String bookFlight(String reqBody){
        String bookingResponse = "null" ;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(booking_url);
            post.addHeader("Authorization", "Bearer " + accessToken);
            post.addHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(reqBody));

            try (CloseableHttpResponse httpResponse = httpClient.execute(post) ) {
                bookingResponse = EntityUtils.toString(httpResponse.getEntity()) ;
            } catch (Exception e) {
                e.printStackTrace();
                bookingResponse = "Booking error : " + e.getMessage() ;
            }
        } catch (Exception e) {
            e.printStackTrace();
            bookingResponse = "booking order error : " + e.getLocalizedMessage();
        }
        return bookingResponse ;
    }
}

