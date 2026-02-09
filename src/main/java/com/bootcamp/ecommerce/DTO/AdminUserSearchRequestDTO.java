package com.bootcamp.ecommerce.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserSearchRequestDTO {
    private Integer pageOffset = 0;
    private Integer pageSize = 10;
    private String sort = "id";
    private String email;
}
