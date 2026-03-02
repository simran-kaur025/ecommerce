package com.bootcamp.ecommerce.utils;


import com.bootcamp.ecommerce.DTO.RequestParams;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RequestParamsExtractor {

    public RequestParams extract(Map<String, String> allParams) {

        Map<String, String> filters = extractFilters(allParams.get("query"));

        int max = Integer.parseInt(allParams.getOrDefault("max", "10"));
        int offset = Integer.parseInt(allParams.getOrDefault("offset", "0"));
        String sortBy = allParams.getOrDefault("sort", "id");
        String order = allParams.getOrDefault("order", "asc");

        return new RequestParams(filters, max, offset, sortBy, order);
    }

    private Map<String, String> extractFilters(String query) {

        Map<String, String> filters = new HashMap<>();

        if (query == null || query.isBlank()) {
            return filters;
        }

        String[] queryParams = query.split(",");

        for (String param : queryParams) {

            String[] keyValue = param.split(":");

            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Invalid query format. Use key:value");
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.equalsIgnoreCase("name")) {

                if (value.isBlank()) {
                    throw new IllegalArgumentException("Name must not be blank.");
                }

                if (!value.matches("^[A-Za-z ]+$")) {
                    throw new IllegalArgumentException(
                            "Name must contain only alphabets and spaces.");
                }
            }

            filters.put(key, value);
        }

        return filters;
    }
}