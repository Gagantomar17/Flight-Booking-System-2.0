package com.flight.booking.Controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.flight.booking.Model.*;
import com.flight.booking.Service.AmadeusAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tomartravels")
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
    @ResponseBody
    public ResponseEntity<?> bookFlight(@RequestBody Map<String, Object> request) {
        String response = request.get("responseBody").toString();
        String price = request.get("beforePrice").toString();

        if (response == null || price == null) {
            return ResponseEntity.badRequest().body("Error: Missing parameters");
        }
        String repriceResponse;
        
        try {
            repriceResponse = api.repriceFlights(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }

        boolean reprice = api.isPriceUnchanged(repriceResponse, price);
        
        if (!reprice) {
            return ResponseEntity.ok(Collections.singletonMap("status", "repriceFlights"));  // AJAX will redirect to reprice page
        } else {
            return ResponseEntity.ok(Map.of(
                    "status", "bookFlights",
                    "repriceResponse", repriceResponse
            ));
        }
    }

    @PostMapping("/bookTraveller")
    public ResponseEntity<String> createOrder(@RequestBody String requestData) {
        String reqBody = "null";
        String resBody = "null";
        System.out.println(("RequestData parsed : " + requestData));

        try {
            JSONObject jsonObject = new JSONObject(requestData);
            reqBody = api.orderReqBody(requestData.toString());
            System.out.println("create order request: " + reqBody);

            try {
                resBody = api.bookFlight(reqBody);
                System.out.println("create order response body: " + resBody);

            } catch (Exception e) {
                resBody = e.getMessage();
                System.out.println("create order response error: " + resBody);
            }
        } catch (Exception e) {
            resBody = e.getMessage();
            System.out.println("create order response error: " + resBody);
        }

        if (!new JSONObject(resBody).has("data")) {
            System.out.println("create order response error: " + resBody);
            return ResponseEntity.badRequest().body(resBody);
        }

        return ResponseEntity.ok(resBody);
    }

}
