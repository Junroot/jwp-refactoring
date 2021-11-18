package kitchenpos.order.application;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import kitchenpos.menu.domain.Menu;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.menu.domain.MenuRepository;
import kitchenpos.order.domain.OrderLineItemRepository;
import kitchenpos.order.domain.OrderRepository;
import kitchenpos.table.domain.OrderTableRepository;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderStatusRequest;
import kitchenpos.order.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class OrderService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final OrderTableRepository orderTableRepository;

    public OrderService(final MenuRepository menuRepository,
                        final OrderRepository orderRepository,
                        final OrderLineItemRepository orderLineItemRepository,
                        final OrderTableRepository orderTableRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        this.orderTableRepository = orderTableRepository;
    }

    @Transactional
    public OrderResponse create(final OrderRequest orderRequest) {
        OrderTable orderTable = orderTableRepository.findById(orderRequest.getOrderTableId())
            .orElseThrow(IllegalArgumentException::new);

        Order savedOrder = orderRepository.save(new Order(orderTable));
        List<OrderLineItem> orderLineItems = orderLineItemRepository.saveAll(
            newOrderLineItems(savedOrder, orderRequest)
        );

        return OrderResponse.from(savedOrder, orderLineItems);
    }

    private List<OrderLineItem> newOrderLineItems(final Order order,
                                                  final OrderRequest orderRequest) {
        if (Objects.isNull(orderRequest.getOrderLineItems())) {
            throw new IllegalArgumentException();
        }

        final Map<Long, Long> menuIdAndQuantity = orderRequest.toMap();
        List<Menu> menus = menuRepository.findAllById(menuIdAndQuantity.keySet());
        validateMenusSize(orderRequest, menus);

        return orderLineItemsFromMenus(order, menuIdAndQuantity, menus);
    }

    private List<OrderLineItem> orderLineItemsFromMenus(final Order order,
                                                        final Map<Long, Long> menuIdAndQuantity,
                                                        final List<Menu> menus) {
        return menus.stream()
            .map(menu -> new OrderLineItem(order, menu, menuIdAndQuantity.get(menu.getId())))
            .collect(Collectors.toList());
    }

    private void validateMenusSize(final OrderRequest orderRequest, final List<Menu> menus) {
        if (menus.isEmpty() || orderRequest.getOrderLineItems().size() != menus.size()) {
            throw new IllegalArgumentException();
        }
    }

    public List<OrderResponse> list() {
        List<OrderLineItem> orderLineItems = orderLineItemRepository.findAll();
        return OrderResponse.listFrom(orderLineItems);
    }

    @Transactional
    public OrderResponse changeOrderStatus(final Long orderId,
                                           final OrderStatusRequest orderStatusRequest) {
        Order savedOrder = orderRepository.findById(orderId)
            .orElseThrow(IllegalArgumentException::new);
        OrderStatus orderStatus = OrderStatus.valueOf(orderStatusRequest.getOrderStatus());
        List<OrderLineItem> orderLineItems = orderLineItemRepository.findAllByOrder(savedOrder);

        savedOrder.changeOrder(orderStatus);
        return OrderResponse.from(savedOrder, orderLineItems);
    }
}