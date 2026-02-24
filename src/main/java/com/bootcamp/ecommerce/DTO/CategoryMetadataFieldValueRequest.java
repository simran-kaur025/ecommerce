package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMetadataFieldValueRequest {
    @NotBlank
    private Long metadataFieldId;

    @NotEmpty
    private String values;
}
