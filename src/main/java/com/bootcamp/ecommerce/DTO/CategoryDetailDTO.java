package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDetailDTO {

    private Long id;
    private String name;
    private BasicCategoryDTO parentCategory;
    private List<MetadataDTO> metadata;
}

