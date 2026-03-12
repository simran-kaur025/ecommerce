package com.bootcamp.ecommerce.specifications;

import com.bootcamp.ecommerce.entity.Order;
import com.bootcamp.ecommerce.entity.OrderProduct;
import com.bootcamp.ecommerce.enums.OrderState;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderSpecifications {

    public static Specification<Order> extract(Map<String, String> filters) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filters.containsKey("customerEmail")) {
                String email = filters.get("customerEmail");

                predicates.add(
                        cb.equal(root.get("customer").get("email"), email)
                );
            }

            if (filters.containsKey("sellerEmail")) {

                Join<Order, OrderProduct> orderProductJoin =
                        root.join("orderProducts", JoinType.INNER);

                predicates.add(
                        cb.equal(
                                orderProductJoin
                                        .get("productVariation")
                                        .get("product")
                                        .get("seller")
                                        .get("user")
                                        .get("email"),
                                filters.get("sellerEmail")
                        )
                );
            }

            // ORDER ID FILTER
            if (filters.containsKey("orderId")) {
                Long orderId = Long.parseLong(filters.get("orderId"));
                predicates.add(cb.equal(root.get("id"), orderId));
            }

            // STATUS FILTER
            if (filters.containsKey("status")) {
                try {
                    OrderState status = OrderState.valueOf(filters.get("status").toUpperCase());
                    Join<Order, OrderProduct> orderProductJoin = root.join("orderProducts", JoinType.INNER);

                    predicates.add(cb.equal(orderProductJoin.get("currentStatus"), status));

                } catch (IllegalArgumentException e) {
                }
            }

            // SEARCH QUERY (product name)
            if (filters.containsKey("query")) {

                Join<Order, OrderProduct> orderProductJoin = root.join("orderProducts", JoinType.INNER);

                predicates.add(
                        cb.like(
                                cb.lower(
                                        orderProductJoin
                                                .get("productVariation")
                                                .get("product")
                                                .get("name")
                                ),
                                "%" + filters.get("query").toLowerCase() + "%"
                        )
                );
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
