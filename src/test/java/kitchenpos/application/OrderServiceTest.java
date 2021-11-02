package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderLineItemDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.domain.repository.MenuRepository;
import kitchenpos.domain.repository.OrderTableRepository;
import kitchenpos.generator.OrderGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class OrderServiceTest extends ServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderLineItemDao orderLineItemDao;

    @Mock
    private OrderTableRepository orderTableRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderTable orderTable1;
    private OrderTable orderTable2;
    private TableGroup tableGroup;

    @BeforeEach
    void setUp() {
        orderTable1 = new OrderTable(1L, null, 4, true);
        orderTable2 = new OrderTable(2L, null, 4, true);
        tableGroup = new TableGroup(1L, Arrays.asList(orderTable1, orderTable2));
    }

    @DisplayName("주문 등록")
    @Test
    void create() {
        when(menuRepository.countByIdIn(Collections.singletonList(1L))).thenReturn(1L);
        when(orderTableRepository.findById(1L)).thenReturn(Optional.of(orderTable1));
        when(orderDao.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return OrderGenerator.newInstance(1L, order.getOrderTableId(), order.getOrderStatus(),
                order.getOrderedTime());
        });
        when(orderLineItemDao.save(any(OrderLineItem.class))).thenAnswer(invocation -> {
            OrderLineItem orderLineItem = invocation.getArgument(0);
            return OrderGenerator.newOrderLineItem(1L, orderLineItem.getOrderId(),
                orderLineItem.getMenuId(), orderLineItem.getQuantity());
        });

        OrderLineItem orderLineItem = OrderGenerator.newOrderLineItem(1L, 1);
        Order order = OrderGenerator.newInstance(1L, Collections.singletonList(orderLineItem));
        Order actual = orderService.create(order);

        verify(orderDao, times(1)).save(order);
        verify(orderLineItemDao, times(1)).save(orderLineItem);
        assertThat(actual).usingRecursiveComparison()
            .ignoringFields("id", "orderLineItems.seq", "orderLineItems.orderId")
            .isEqualTo(order);
        assertThat(actual.getId()).isNotNull()
            .isEqualTo(actual.getOrderLineItems().get(0).getOrderId());
        assertThat(actual.getOrderLineItems()).hasSize(1);
        assertThat(actual.getOrderLineItems().get(0).getSeq()).isNotNull();
    }

    @DisplayName("주문 항목이 0개인 주문 등록할 경우 예외 처리")
    @Test
    void createWithoutOrderLineItems() {
        Order order = OrderGenerator.newInstance(1L, Collections.emptyList());

        assertThatThrownBy(() -> orderService.create(order)).isExactlyInstanceOf(
            IllegalArgumentException.class);
    }

    @DisplayName("등록되지 않은 메뉴로 주문 등록할 경우 예외 처리")
    @Test
    void createWithNotFoundMenu() {
        when(menuRepository.countByIdIn(Collections.singletonList(1L))).thenReturn(0L);

        OrderLineItem orderLineItem = OrderGenerator.newOrderLineItem(1L, 1);
        Order order = OrderGenerator.newInstance(1L, Collections.singletonList(orderLineItem));
        assertThatThrownBy(() -> orderService.create(order)).isExactlyInstanceOf(
            IllegalArgumentException.class);
    }

    @DisplayName("등록되지 않은 테이블에 주문 등록할 경우 예외 처리")
    @Test
    void createWithNotFoundOrderTable() {
        when(menuRepository.countByIdIn(Collections.singletonList(1L))).thenReturn(1L);
        when(orderTableRepository.findById(1L)).thenReturn(Optional.empty());

        OrderLineItem orderLineItem = OrderGenerator.newOrderLineItem(1L, 1);
        Order order = OrderGenerator.newInstance(1L, Collections.singletonList(orderLineItem));
        assertThatThrownBy(() -> orderService.create(order)).isExactlyInstanceOf(
            IllegalArgumentException.class);
    }

    @DisplayName("비어있는 테이블에 주문 등록할 경우 예외 처리")
    @Test
    void createWithEmptyOrderTable() {
        when(menuRepository.countByIdIn(Collections.singletonList(1L))).thenReturn(1L);
        when(orderTableRepository.findById(1L)).thenReturn(Optional.of(
            new OrderTable(3L, null, 4, true)
        ));

        OrderLineItem orderLineItem = OrderGenerator.newOrderLineItem(1L, 1);
        Order order = OrderGenerator.newInstance(1L, Collections.singletonList(orderLineItem));
        assertThatThrownBy(() -> orderService.create(order)).isExactlyInstanceOf(
            IllegalArgumentException.class);
    }

    @DisplayName("주문 조회")
    @Test
    void list() {
        List<Order> orders = Arrays.asList(
            OrderGenerator.newInstance(1L, 1L, OrderStatus.COOKING.name(), LocalDateTime.now()),
            OrderGenerator.newInstance(2L, 2L, OrderStatus.COOKING.name(), LocalDateTime.now())
        );
        when(orderDao.findAll()).thenReturn(orders);
        when(orderLineItemDao.findAllByOrderId(any(Long.class))).thenAnswer(
            invocation -> Arrays.asList(
                OrderGenerator.newOrderLineItem(1L, invocation.getArgument(0), 1L, 1),
                OrderGenerator.newOrderLineItem(2L, invocation.getArgument(0), 2L, 1)
            )
        );

        List<Order> actual = orderService.list();

        assertThat(actual).hasSameSizeAs(orders)
            .usingRecursiveFieldByFieldElementComparator()
            .usingElementComparatorIgnoringFields("orderLineItems")
            .hasSameElementsAs(orders);
        for (Order actualOrder : actual) {
            assertThat(actualOrder.getOrderLineItems()).hasSize(2);
        }
    }

    @DisplayName("주문 상태 수정")
    @Test
    void changeOrderStatus() {
        long idToChange = 1L;
        when(orderDao.findById(idToChange)).thenReturn(Optional.of(
            OrderGenerator.newInstance(1L, 1L, OrderStatus.COOKING.name(), LocalDateTime.now())
        ));

        String orderStatus = OrderStatus.MEAL.name();
        Order order = OrderGenerator.newInstance(orderStatus);
        Order actual = orderService.changeOrderStatus(idToChange, order);

        assertThat(actual.getOrderStatus()).isEqualTo(orderStatus);
    }

    @DisplayName("등록되지 않은 주문 상태 수정시 예외 처리")
    @Test
    void changeOrderStatusWithNotFoundOrder() {
        long idToChange = 1L;
        when(orderDao.findById(idToChange)).thenReturn(Optional.empty());

        String orderStatus = OrderStatus.MEAL.name();
        Order order = OrderGenerator.newInstance(orderStatus);
        assertThatThrownBy(
            () -> orderService.changeOrderStatus(idToChange, order)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("계산 완료 상태 주문의 상태 수정시 예외 처리")
    @Test
    void changeOrderStatusWith() {
        long idToChange = 1L;
        when(orderDao.findById(idToChange)).thenReturn(Optional.of(
            OrderGenerator.newInstance(
                1L,
                1L,
                OrderStatus.COMPLETION.name(),
                LocalDateTime.now()
            )
        ));

        String orderStatus = OrderStatus.MEAL.name();
        Order order = OrderGenerator.newInstance(orderStatus);
        assertThatThrownBy(
            () -> orderService.changeOrderStatus(idToChange, order)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
