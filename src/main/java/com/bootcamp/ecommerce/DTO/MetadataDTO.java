package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class MetadataDTO {

    private Long fieldId;
    private String fieldName;
    private String values;
}

