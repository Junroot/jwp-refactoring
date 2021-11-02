package kitchenpos.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;

public class OrderResponse {

    private Long id;
    private String orderStatus;
    private LocalDateTime orderedTime;
    private List<OrderLineItemResponse> orderLineItems;

    protected OrderResponse() {
    }

    public OrderResponse(final Long id, final String orderStatus, final LocalDateTime orderedTime,
                         final List<OrderLineItemResponse> orderLineItems) {
        this.id = id;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
        this.orderLineItems = orderLineItems;
    }

    public static List<OrderResponse> listFrom(final List<Order> orders) {
        return orders.stream()
            .map(OrderResponse::from)
            .collect(Collectors.toList());
    }

    public static OrderResponse from(final Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderStatus().name(),
            order.getOrderedTime(),
            OrderLineItemResponse.listFrom(order.getOrderLineItems())
        );
    }

    public Long getId() {
        return id;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }

    public List<OrderLineItemResponse> getOrderLineItems() {
        return orderLineItems;
    }

    public static class OrderLineItemResponse {

        private Long seq;
        private Long orderId;
        private Long menuId;
        private Long quantity;

        protected OrderLineItemResponse() {
        }

        public OrderLineItemResponse(final Long seq, final Long orderId, final Long menuId,
                                     final Long quantity) {
            this.seq = seq;
            this.orderId = orderId;
            this.menuId = menuId;
            this.quantity = quantity;
        }

        public static List<OrderLineItemResponse> listFrom(
            final List<OrderLineItem> orderLineItems) {
            return orderLineItems.stream()
                .map(OrderLineItemResponse::from)
                .collect(Collectors.toList());
        }

        public static OrderLineItemResponse from(final OrderLineItem orderLineItem) {
            return new OrderLineItemResponse(
                orderLineItem.getSeq(),
                orderLineItem.getOrder().getId(),
                orderLineItem.getMenu().getId(),
                orderLineItem.getQuantity()
            );
        }

        public Long getSeq() {
            return seq;
        }

        public Long getOrderId() {
            return orderId;
        }

        public Long getMenuId() {
            return menuId;
        }

        public Long getQuantity() {
            return quantity;
        }
    }
}
