package com.bootcamp.ecommerce.DTO;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilterResponse {

    private Map<String, List<String>> metadataFields;
    private List<String> brands;
    private Double minPrice;
    private Double maxPrice;


}
