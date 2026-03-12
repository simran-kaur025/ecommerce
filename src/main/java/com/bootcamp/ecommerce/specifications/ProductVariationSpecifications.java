package com.bootcamp.ecommerce.specifications;

import com.bootcamp.ecommerce.entity.Product;
import com.bootcamp.ecommerce.entity.ProductVariation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductVariationSpecifications {
    public static Specification<ProductVariation> extract(Product product,Map<String, String> filters) {

        return (root, query, cb) -> {

                List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("product"), product));

            if (filters.containsKey("minPrice")) {
                Double minPrice = Double.parseDouble(filters.get("minPrice"));
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (filters.containsKey("maxPrice")) {
                Double maxPrice = Double.parseDouble(filters.get("maxPrice"));
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (filters.containsKey("minQuantity")) {
                Integer minQuantity = Integer.parseInt(filters.get("minQuantity"));
                predicates.add(cb.greaterThanOrEqualTo(root.get("quantityAvailable"), minQuantity));
            }

            if (filters.containsKey("maxQuantity")) {
                Integer maxQuantity = Integer.parseInt(filters.get("maxQuantity"));
                predicates.add(cb.lessThanOrEqualTo(root.get("quantityAvailable"), maxQuantity));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
