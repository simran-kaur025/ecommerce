package com.bootcamp.ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class MetadataDTO {

    private Long fieldId;
    private String fieldName;
    private String values;
}

