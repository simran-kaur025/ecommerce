package com.bootcamp.ecommerce.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeafCategoryResponse {

    private Long id;
    private String name;
    private List<String> parentChain;
    private List<CategoryMetadataFieldValueRequest> metadata;
}

