package com.bootcamp.ecommerce.DTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerListResponseDTO {
    private List<SellerResponse> sellers;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
