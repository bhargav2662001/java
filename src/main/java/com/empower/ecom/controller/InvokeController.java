package com.empower.ecom.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/entries")
public class InvokeController {

    private final RestTemplate restTemplate;

    public InvokeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate; // Which will be used to make HTTP requests
    }

    @PostMapping
    public ResponseEntity<Object> fetchObjectData(@RequestBody Map<String, Object> requestBody) { // HTTP request body
        String url = (String) requestBody.get("url"); // Object which contains URL field
        String httpMethod = (String) requestBody.get("httpmethod");

        try {
            if (url == null || url.isEmpty()) {
                return ResponseEntity.badRequest().body("URL is required in the request body");//if empty or null it will show error
            }

            if ("post".equalsIgnoreCase(httpMethod)) {
                return performPost(url, requestBody);//Depending on the httpmethod, it calls different private methods (performPost, performPut, fetchFromUrl) 
            } else if ("put".equalsIgnoreCase(httpMethod)) {//to handle POST, PUT, or GET requests, respectively.
                return performPut(url, requestBody);
            }
            
            else {
                // Default behavior: Fetch data from the specified URL
                return fetchFromUrl(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error processing request");
        }
    }

    private ResponseEntity<Object> fetchFromUrl(String url) {
        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error fetching data from the provided URL");
        }
    }

    private ResponseEntity<Object> performPost(String url, Map<String, Object> requestBody) {
        try {
        	HttpHeaders headers = extractHeaders(requestBody);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody,headers);
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error performing POST request");
        }
    }//performPost(String url, Map<String, Object> requestBody): Uses restTemplate.exchange to perform a 
    //POST request to the specified URL with the provided request body.

    private ResponseEntity<Object> performPut(String url, Map<String, Object> requestBody) {
        try {
            String id = (String) requestBody.get("id"); // Assuming 'id' is passed as a string in the request body
            requestBody.remove("url");
            requestBody.remove("httpmethod");
            requestBody.remove("id");
            HttpHeaders headers = extractHeaders(requestBody);
            
            
            // Prepare the PUT request with the request body as-is
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody,headers);
            String putUrl = url + "/" + id;
            
            ResponseEntity<Object> response = restTemplate.exchange(putUrl, HttpMethod.PUT, requestEntity, Object.class);
            
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error performing PUT request: " + e.getMessage());
        }
    }
//    performPut(String url, Map<String, Object> requestBody): Extracts an id from the request body, constructs the full 
//    URL by appending the id, and uses restTemplate.exchange to perform a PUT request with the modified request body

    
   //passing headers in the request body in the post method and put method 
    
    private HttpHeaders extractHeaders(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        if (requestBody.containsKey("headers")) {
            @SuppressWarnings("unchecked")
			Map<String, String> headersMap = (Map<String, String>) requestBody.get("headers");
            headersMap.forEach(headers::add);
            requestBody.remove("headers"); // Remove headers from the request body to avoid sending them as part of the payload
        }
        return headers; 
    }
}