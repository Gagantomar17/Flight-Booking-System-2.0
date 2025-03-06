package com.flight.booking.Controller;


import com.flight.booking.Model.FlightSearch;
import com.flight.booking.Model.FlightWrapper;
import com.flight.booking.Service.AmadeusAPI;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController  // Converts it into a REST API
@RequestMapping("/tomartravels")  // Base URL for all endpoints
public class FlightController {


    private final AmadeusAPI api;


    @Autowired
    public FlightController(AmadeusAPI api) {
        this.api = api;
    }


    @PostMapping("/flights")
    public List<FlightWrapper> searchResult(@RequestBody FlightSearch fSearch) {
        JSONObject apiResponseObj = new JSONObject();
        String apiResponse ;
        try {
            apiResponseObj = api.getFlights(
                fSearch.getSource(),
                fSearch.getDestination(),
                fSearch.getDate(),
                fSearch.getAdult(),
                fSearch.getChild(),
                fSearch.getInfant()
            );
            apiResponse = apiResponseObj.toString();
            
        } catch (Exception e) {
            apiResponse = "{\"error\": \"Error occurred: " + e.getMessage() + "\"}";
            return List.of();
        }
        return api.responseParsing(apiResponse);  
    }

    @PostMapping("/booking")
    @ResponseBody  // Ensures response is sent as plain text
    public String bookFlight(@RequestBody Map<String, Object> request) {
        String response = request.get("responseBody").toString();
        String price = request.get("beforePrice").toString();

        if (response == null || price == null) {
            return "Error: Missing parameters";
        }
        String repriceResponse;
        
        try {
            repriceResponse = api.repriceFlights(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

        boolean reprice = api.isPriceUnchanged(repriceResponse, price);
        
        if (!reprice) {
            return "repriceFlights";  // AJAX will redirect to reprice page
        } else {
            return "bookFlights";  // AJAX will proceed to booking
        }
    }

}
