package com.bootcamp.ecommerce.specifications;

import com.bootcamp.ecommerce.enums.OrderState;
import jakarta.persistence.criteria.Predicate;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderSpecification {

    public static Specification<Order> filter(Long userId, Map<String, String> filters) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("customer").get("id"), userId));

            if (filters != null) {

                filters.forEach((key, value) -> {

                    if (value == null || value.isBlank()) return;

                    if (key.equalsIgnoreCase("id")) {
                        Long id = Long.valueOf(value);
                        predicates.add(cb.equal(root.get("id"), id));
                    }

                    if (key.equalsIgnoreCase("status")) {
                        predicates.add(
                                cb.equal(root.get("status"), OrderState.valueOf(value))
                        );
                    }

                    if (key.equalsIgnoreCase("paymentMethod")) {
                        predicates.add(
                                cb.like(
                                        cb.lower(root.get("paymentMethod")),
                                        "%" + value.toLowerCase() + "%"
                                )
                        );
                    }

                    if (key.equalsIgnoreCase("amount")) {
                        predicates.add(
                                cb.equal(root.get("amountPaid"), new Double(value))
                        );
                    }

                });
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
