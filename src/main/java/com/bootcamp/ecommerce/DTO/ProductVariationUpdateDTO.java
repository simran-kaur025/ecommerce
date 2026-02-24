package com.bootcamp.ecommerce.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariationUpdateDTO {
    @NotNull(message = "Variation id is required")
    private Long variationId;

    @Min(value = 0, message = "Quantity must be 0 or more")
    private Integer quantityAvailable;

    @Min(value = 0, message = "Price must be 0 or more")
    private Double price;

    private String primaryImageName;

    private List<String> secondaryImages;

    private Map<String, List<String>> metadata;

    private Boolean isActive;
}
