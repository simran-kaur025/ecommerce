package com.bootcamp.ecommerce.DTO;


import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private List<BasicCategoryDTO> parentCategories;
    private List<BasicCategoryDTO> childCategories;
    private List<MetadataDTO> metadata;
}
