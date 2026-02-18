package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CategoryMetadataFieldValueRequest {
    @NotBlank
    private Long metadataFieldId;

    @NotEmpty
    private String values;
}
