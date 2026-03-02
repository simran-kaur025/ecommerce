package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.CategoryMetadataFieldRequest;
import com.bootcamp.ecommerce.DTO.MetadataPageResponse;
import com.bootcamp.ecommerce.DTO.RequestParams;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import com.bootcamp.ecommerce.service.MetadataService;
import com.bootcamp.ecommerce.utils.RequestParamsExtractor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;

    private final RequestParamsExtractor extractor;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/metadata-fields")
    public ResponseEntity<ResponseDTO> createMetadataField(@Valid @RequestBody CategoryMetadataFieldRequest request) {

        CategoryMetadataField field = metadataService.createMetadataField(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .status("SUCCESS")
                        .message("Metadata field created successfully")
                        .data(field.getId())
                        .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get/metadata-fields")
    public ResponseEntity<ResponseDTO> getAllMetadataFields(@RequestParam Map<String, String> allParams) {

        RequestParams requestParams = extractor.extract(allParams);

        MetadataPageResponse result  = metadataService.getAllMetadataFields(requestParams);

        return ResponseEntity.ok(ResponseDTO.builder()
                .status("SUCCESS")
                .message("List of metadata fields")
                .data(result)
                .build());
    }
}
