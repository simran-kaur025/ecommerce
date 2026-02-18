package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddCategoryMetadataRequest {

    @NotNull(message = "Category Id is required")
    private Long categoryId;

    @NotEmpty(message = "At least one metadata field is required")
    private List<CategoryMetadataFieldValueRequest> fields;
}

