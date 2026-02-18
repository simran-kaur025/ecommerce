package com.bootcamp.ecommerce.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResponseDTO {

    private List<ProductDetailResponseDTO> products;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}

