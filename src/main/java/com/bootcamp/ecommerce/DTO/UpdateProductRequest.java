package com.bootcamp.ecommerce.DTO;

import lombok.Data;

@Data
public class UpdateProductRequest {

    private String name;
    private String description;
    private Boolean isCancellable;
    private Boolean isReturnable;
}
