package kitchenpos.order.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByTableIdInAndOrderStatusIn(List<Long> tableIds, List<OrderStatus> orderStatuses);

    boolean existsByTableIdAndOrderStatusIn(Long orderTableId, List<OrderStatus> orderStatuses);

    List<Order> findAllByTableIdIn(List<Long> tableIds);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderLineItems JOIN FETCH o.table")
    List<Order> findAllWithOrderLineItemsAndTable();
}