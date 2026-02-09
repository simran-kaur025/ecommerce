package com.bootcamp.ecommerce.DTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerListResponseDTO {
    private List<CustomerResponse> customers;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
