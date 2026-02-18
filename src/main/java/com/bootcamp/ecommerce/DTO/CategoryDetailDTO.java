package com.bootcamp.ecommerce.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryDetailDTO {

    private Long id;
    private String name;
    private BasicCategoryDTO parentCategory;
    private List<MetadataDTO> metadata;
}

