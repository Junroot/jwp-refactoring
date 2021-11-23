package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import kitchenpos.table.application.TableService;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
import kitchenpos.table.domain.TableGroup;
import kitchenpos.table.dto.OrderTableEmptyRequest;
import kitchenpos.table.dto.OrderTableNumberOfGuestsRequest;
import kitchenpos.table.dto.OrderTableRequest;
import kitchenpos.table.dto.OrderTableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class TableServiceTest extends ServiceTest {

    @Mock
    private OrderTableRepository orderTableRepository;

    @InjectMocks
    private TableService tableService;

    private OrderTable orderTable1;

    @BeforeEach
    void setUp() {
        orderTable1 = new OrderTable(1L, null, 4, true);
        OrderTable orderTable2 = new OrderTable(2L, null, 4, true);
        new TableGroup(1L, Arrays.asList(orderTable1, orderTable2));
    }

    @DisplayName("주문 테이블 등록")
    @Test
    void create() {
        long newOrderTableId = 1L;
        when(orderTableRepository.save(any(OrderTable.class))).thenAnswer(invocation -> {
            OrderTable orderTable = invocation.getArgument(0);
            return new OrderTable(
                newOrderTableId,
                orderTable.getTableGroup(),
                orderTable.getNumberOfGuests(),
                orderTable.isEmpty()
            );
        });
        OrderTableRequest request = new OrderTableRequest(0, true);

        OrderTableResponse actual = tableService.create(request);
        OrderTableResponse expected = new OrderTableResponse(
            newOrderTableId,
            null,
            request.getNumberOfGuests(),
            request.isEmpty()
        );

        verify(orderTableRepository, times(1)).save(any(OrderTable.class));
        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @DisplayName("주문 테이블들 조회")
    @Test
    void list() {
        List<OrderTable> orderTables = Arrays.asList(
            new OrderTable(1L, null, 0, true),
            new OrderTable(2L, null, 0, true)
        );
        when(orderTableRepository.findAll()).thenReturn(orderTables);

        List<OrderTableResponse> actual = tableService.list();
        List<OrderTableResponse> expected = OrderTableResponse.listFrom(orderTables);

        assertThat(actual).hasSameSizeAs(orderTables)
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(expected);
    }

    @DisplayName("주문 테이블 빈 상태 수정")
    @Test
    void changeEmpty() {
        OrderTable savedOrderTable = new OrderTable(1L, null, 0, true);
        when(orderTableRepository.findById(1L)).thenReturn(Optional.of(savedOrderTable));

        OrderTableEmptyRequest request = new OrderTableEmptyRequest(false);
        OrderTableResponse actual = tableService.changeEmpty(
            savedOrderTable.getId(),
            request
        );
        OrderTableResponse expected = OrderTableResponse.from(savedOrderTable);

        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @DisplayName("등록되지 않은 주문 테이블의 빈 상태를 수정할 경우 예외 처리")
    @Test
    void changeEmptyWithNotFoundOrderTable() {
        long idToChange = 1L;
        when(orderTableRepository.findById(idToChange)).thenReturn(Optional.empty());

        OrderTableEmptyRequest request = new OrderTableEmptyRequest(false);
        assertThatThrownBy(() -> tableService.changeEmpty(1L, request)).isExactlyInstanceOf(
            IllegalArgumentException.class
        );
    }

    @DisplayName("단체로 등록된 주문 테이블의 빈 상태를 수정할 경우 예외 처리")
    @Test
    void changeEmptyWithTableDesignatedAsGroup() {
        when(orderTableRepository.findById(1L)).thenReturn(Optional.of(orderTable1));

        OrderTableEmptyRequest request = new OrderTableEmptyRequest(false);
        assertThatThrownBy(() -> tableService.changeEmpty(1L, request)).isExactlyInstanceOf(
            IllegalArgumentException.class
        );
    }

    @DisplayName("주문 테이블 방문한 손님 수 수정")
    @Test
    void changeNumberOfGuests() {
        OrderTable savedOrderTable = new OrderTable(1L, null, 0, false);
        when(orderTableRepository.findById(1L)).thenReturn(Optional.of(savedOrderTable));

        OrderTableNumberOfGuestsRequest request = new OrderTableNumberOfGuestsRequest(4);
        OrderTableResponse actual = tableService.changeNumberOfGuests(1L, request);

        OrderTableResponse expected = new OrderTableResponse(
            savedOrderTable.getId(),
            null,
            request.getNumberOfGuests(),
            savedOrderTable.isEmpty()
        );

        assertThat(actual).usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @DisplayName("방문한 손님 수가 음수일 경우 예외 처리")
    @Test
    void changeNumberOfGuestsWithNegativeNumber() {
        OrderTableNumberOfGuestsRequest request = new OrderTableNumberOfGuestsRequest(-1);
        assertThatThrownBy(
            () -> tableService.changeNumberOfGuests(1L, request)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("등록되지 않은 주문 테이블의 방문한 손님 수를 수정할 경우 예외 처리")
    @Test
    void changeNumberOfGuestsWithNotFoundOrderTable() {
        when(orderTableRepository.findById(1L)).thenReturn(Optional.empty());

        OrderTableNumberOfGuestsRequest request = new OrderTableNumberOfGuestsRequest(4);
        assertThatThrownBy(
            () -> tableService.changeNumberOfGuests(1L, request)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("비어있는 주문 테이블의 방문한 손님 수를 수정할 경우 예외 처리")
    @Test
    void changeNumberOfGuestsWithEmptyOrderTable() {
        OrderTable savedOrderTable = new OrderTable(1L, null, 0, true);
        when(orderTableRepository.findById(1L)).thenReturn(Optional.of(savedOrderTable));

        OrderTableNumberOfGuestsRequest request = new OrderTableNumberOfGuestsRequest(4);
        assertThatThrownBy(
            () -> tableService.changeNumberOfGuests(1L, request)
        ).isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
