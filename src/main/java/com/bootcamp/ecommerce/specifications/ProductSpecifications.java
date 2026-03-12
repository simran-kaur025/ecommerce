package com.bootcamp.ecommerce.specifications;

import com.bootcamp.ecommerce.entity.Product;
import com.bootcamp.ecommerce.entity.ProductVariation;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProductSpecifications {

    public static Specification<Product> extract(Map<String, String> filters) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.isTrue(root.get("isActive")));

            if (filters.containsKey("categoryIds")) {

                String ids = filters.get("categoryIds");

                List<Long> categoryIds = Arrays.stream(
                                ids.replace("[", "")
                                        .replace("]", "")
                                        .split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .toList();

                predicates.add(root.get("category").get("id").in(categoryIds));
            }
            if (filters.containsKey("categoryId")) {
                Long categoryId = Long.parseLong(filters.get("categoryId"));
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (filters.containsKey("sellerId")) {
                String sellerId = filters.get("sellerId");
                predicates.add(cb.equal(root.get("createdBy"), sellerId));
            }

            // NAME FILTER
            if (filters.containsKey("name")) {
                String name = filters.get("name").toLowerCase();
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name + "%"));
            }

            // BRAND FILTER
            if (filters.containsKey("brand")) {
                String brand = filters.get("brand").toLowerCase();
                predicates.add(cb.like(cb.lower(root.get("brand")), "%" + brand + "%"));
            }

            Join<Product, ProductVariation> variationJoin = null;

            if (filters.containsKey("minPrice") ||
                    filters.containsKey("maxPrice") ||
                    filters.containsKey("minQuantity") ||
                    filters.containsKey("maxQuantity")) {

                variationJoin = root.join("productVariations", JoinType.INNER);
            }

            if (variationJoin != null) {

                if (filters.containsKey("minPrice")) {
                    Double minPrice = Double.parseDouble(filters.get("minPrice"));
                    predicates.add(cb.greaterThanOrEqualTo(variationJoin.get("price"), minPrice));
                }

                if (filters.containsKey("maxPrice")) {
                    Double maxPrice = Double.parseDouble(filters.get("maxPrice"));
                    predicates.add(cb.lessThanOrEqualTo(variationJoin.get("price"), maxPrice));
                }

                if (filters.containsKey("minQuantity")) {
                    Integer minQuantity = Integer.parseInt(filters.get("minQuantity"));
                    predicates.add(cb.greaterThanOrEqualTo(variationJoin.get("quantityAvailable"), minQuantity));
                }

                if (filters.containsKey("maxQuantity")) {
                    Integer maxQuantity = Integer.parseInt(filters.get("maxQuantity"));
                    predicates.add(cb.lessThanOrEqualTo(variationJoin.get("quantityAvailable"), maxQuantity));
                }
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}