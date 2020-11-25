package kitchenpos.domain;

import java.time.LocalDateTime;

import kitchenpos.exception.OrderStatusCannotChangeException;

public class Order {
    private Long id;
    private Long tableId;
    private OrderStatus orderStatus;
    private LocalDateTime orderedTime;

    public Order() {
    }

    public Order(Long tableId) {
        this.tableId = tableId;
        this.orderStatus = OrderStatus.COOKING;
        this.orderedTime = LocalDateTime.now();
    }

    public Order(Long id, Long tableId, OrderStatus orderStatus, LocalDateTime orderedTime) {
        this.id = id;
        this.tableId = tableId;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
    }

    public void changeOrderStatus(OrderStatus orderStatus) {
        if (this.orderStatus.isCompletion()) {
            throw new OrderStatusCannotChangeException();
        }
        this.orderStatus = orderStatus;
    }

    public boolean hasInProgressStatus() {
        return !orderStatus.isCompletion();
    }

    public Long getId() {
        return id;
    }

    public Long getTableId() {
        return tableId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }
}