package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
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

    public String cacheKey() {
        return offset + "_" + max + "_" + sortBy + "_" + order + "_" + filters;
    }
}
