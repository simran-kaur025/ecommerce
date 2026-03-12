package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;

@Getter
public class RequestParams {

    private final Map<String, String> filters;

    @Min(value = 1, message = "Max must be at least 1")
    private final int max;

    @Min(value = 0, message = "Offset cannot be negative")
    private final int offset;

    @Pattern(regexp = "^(id|name|date_created)$",
            message = "Invalid sort field")
    private final String sortBy;

    @Pattern(regexp = "^(asc|desc)$",
            message = "Order must be asc or desc")
    private final String order;

    public RequestParams(Map<String, String> filters,
                         Integer max,
                         Integer offset,
                         String sortBy,
                         String order) {

        this.filters = (filters != null) ? filters : Collections.emptyMap();
        this.max = (max != null) ? max : 10;
        this.offset = (offset != null) ? offset : 0;
        this.sortBy = (sortBy != null) ? sortBy : "id";
        this.order = (order != null) ? order : "asc";
    }

    public String cacheKey() {
        return offset + "_" + max + "_" + sortBy + "_" + order + "_" + filters;
    }
}