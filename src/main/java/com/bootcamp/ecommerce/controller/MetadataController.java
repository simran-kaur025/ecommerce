package com.bootcamp.ecommerce.controller;

import com.bootcamp.ecommerce.DTO.CategoryMetadataFieldRequest;
import com.bootcamp.ecommerce.DTO.MetadataPageResponse;
import com.bootcamp.ecommerce.DTO.ResponseDTO;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import com.bootcamp.ecommerce.service.MetadataService;
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

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService metadataService;


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
    public ResponseEntity<ResponseDTO> getAllMetadataFields(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query
    ) {

        MetadataPageResponse result  = metadataService.getAllMetadataFields(offset, max, sortBy, order, query);

        return ResponseEntity.ok(ResponseDTO.builder()
                .status("SUCCESS")
                .message("List of metadata fields")
                .data(result)
                .build());
    }
}
