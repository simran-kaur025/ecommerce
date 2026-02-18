package com.bootcamp.ecommerce.service;

import com.bootcamp.ecommerce.DTO.CategoryRequestDTO;
import com.bootcamp.ecommerce.DTO.UserValidationDTO;

import java.util.List;

public interface CategoryValidationService {

    void validate(CategoryRequestDTO dto, List<UserValidationDTO> errors);
}