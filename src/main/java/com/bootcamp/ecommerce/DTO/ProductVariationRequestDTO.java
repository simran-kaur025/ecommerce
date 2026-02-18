package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ProductVariationRequestDTO {
//
//    @NotNull(message = "Product Id is mandatory")
//    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or more")
    private Integer quantityAvailable;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be 0 or more")
    private Double price;

    @NotBlank
    private String primaryImageName;

    private List<String> secondaryImages;

    @NotEmpty
    private Map<String, List<String>> metadata;

   private Boolean isActive;
}

