package com.bootcamp.ecommerce.DTO;

import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataPageResponse {

    private List<MetadataFieldDTO> content;
    private int page;
    private int size;
    private long totalElements;
}
