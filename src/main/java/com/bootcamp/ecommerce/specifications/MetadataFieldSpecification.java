package com.bootcamp.ecommerce.specifications;
import jakarta.persistence.criteria.Predicate;
import com.bootcamp.ecommerce.entity.CategoryMetadataField;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetadataFieldSpecification {

    public static Specification<CategoryMetadataField> filter(Map<String, String> filters) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            filters.forEach((key, value) -> {

                if (key.equalsIgnoreCase("name")) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + value.toLowerCase() + "%"));
                }
                if (key.equalsIgnoreCase("id")) {
                    Long id =Long.valueOf(value);
                    predicates.add(cb.equal(root.get("id"), id));
                }
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
