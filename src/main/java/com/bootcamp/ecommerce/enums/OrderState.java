package com.bootcamp.ecommerce.enums;

import java.util.Map;
import java.util.Set;

public enum OrderState {

    ORDER_PLACED,
    ORDER_CONFIRMED,
    ORDER_REJECTED,
    ORDER_SHIPPED,
    DELIVERED,
    RETURN_REQUESTED,
    RETURN_REJECTED,
    RETURN_APPROVED,
    PICK_UP_INITIATED,
    PICK_UP_COMPLETED,
    REFUND_INITIATED,
    REFUND_COMPLETED,
    CANCELLED,
    CLOSED;

    private static final Map<OrderState, Set<OrderState>> transitions = Map.ofEntries(

            Map.entry(ORDER_PLACED, Set.of(ORDER_CONFIRMED, ORDER_REJECTED, CANCELLED)),

            Map.entry(ORDER_CONFIRMED, Set.of(ORDER_SHIPPED, CANCELLED)),

            Map.entry(ORDER_SHIPPED, Set.of(DELIVERED)),

            Map.entry(DELIVERED, Set.of(RETURN_REQUESTED, CLOSED)),

            Map.entry(RETURN_REQUESTED, Set.of(RETURN_APPROVED, RETURN_REJECTED)),

            Map.entry(RETURN_APPROVED, Set.of(PICK_UP_INITIATED)),

            Map.entry(PICK_UP_INITIATED, Set.of(PICK_UP_COMPLETED)),

            Map.entry(PICK_UP_COMPLETED, Set.of(REFUND_INITIATED)),

            Map.entry(REFUND_INITIATED, Set.of(REFUND_COMPLETED)),

            Map.entry(REFUND_COMPLETED, Set.of(CLOSED)),

            Map.entry(ORDER_REJECTED, Set.of(REFUND_INITIATED, CLOSED)),

            Map.entry(CANCELLED, Set.of(REFUND_INITIATED, CLOSED))
    );

    public static boolean isValidTransition(OrderState from, OrderState to) {

        if (from == null) {
            return to == ORDER_PLACED;
        }

        return transitions.containsKey(from) &&
                transitions.get(from).contains(to);
    }
}