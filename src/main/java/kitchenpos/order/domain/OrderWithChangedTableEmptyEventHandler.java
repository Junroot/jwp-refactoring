package kitchenpos.order.domain;

import java.util.Arrays;
import java.util.List;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableEmptyChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderWithChangedTableEmptyEventHandler {

    private final OrderRepository orderRepository;

    public OrderWithChangedTableEmptyEventHandler(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Async
    @EventListener
    @Transactional
    public void handle(final OrderTableEmptyChangedEvent event) {
        validate(event);
    }

    public void validate(final OrderTableEmptyChangedEvent event) {
        if (existWithOrderTableAndContainsOrderStatusIn(event.getOrderTable(),
            Arrays.asList(OrderStatus.COOKING, OrderStatus.MEAL))) {
            throw new IllegalArgumentException();
        }
    }

    private boolean existWithOrderTableAndContainsOrderStatusIn(final OrderTable orderTable,
                                                                final List<OrderStatus> orderStatuses) {
        List<Order> orders = orderRepository.findAllByOrderTableId(orderTable.getId());

        return orders.stream()
            .anyMatch(order -> order.isOrderStatusIn(orderStatuses));
    }
}
