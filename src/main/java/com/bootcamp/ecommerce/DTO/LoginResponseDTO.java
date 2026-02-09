package com.bootcamp.ecommerce.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
}

