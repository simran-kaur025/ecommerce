package com.bootcamp.ecommerce.service.impl;

import com.bootcamp.ecommerce.DTO.CategoryMetadataFieldRequest;
import com.bootcamp.ecommerce.DTO.MetadataFieldDTO;
import com.bootcamp.ecommerce.DTO.MetadataPageResponse;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import com.bootcamp.ecommerce.exceptionalHandler.BadRequestException;
import com.bootcamp.ecommerce.repository.CategoryMetadataFieldRepository;
import com.bootcamp.ecommerce.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {
    private final CategoryMetadataFieldRepository categoryMetadataFieldRepository;

    @CacheEvict(value = "metadataFields", allEntries = true)    @Override
    public CategoryMetadataField createMetadataField(CategoryMetadataFieldRequest request) {

        if (categoryMetadataFieldRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Metadata field name must be unique");
        }

        CategoryMetadataField field = new CategoryMetadataField();
        field.setName(request.getName().trim());

        return categoryMetadataFieldRepository.save(field);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "metadataFields",
            key = "#offset + '_' + #max + '_' + #sortBy + '_' + #order + '_' + #query"
    )
    public MetadataPageResponse getAllMetadataFields(int offset, int max, String sortBy, String order, String query) {

        Sort.Direction direction =
                Sort.Direction.fromOptionalString(order).orElse(Sort.Direction.ASC);

        Pageable pageable = PageRequest.of(offset, max, Sort.by(direction, sortBy));

        Page<CategoryMetadataField> page;

        if (query != null && !query.isBlank()) {
            page = categoryMetadataFieldRepository.findByNameContainingIgnoreCase(query, pageable);
        } else {
            page = categoryMetadataFieldRepository.findAll(pageable);
        }

        List<MetadataFieldDTO> content = page.getContent()
                .stream()
                .map(field -> new MetadataFieldDTO(field.getId(), field.getName()))
                .toList();

        return new MetadataPageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
