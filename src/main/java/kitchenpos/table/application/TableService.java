package kitchenpos.table.application;

import java.util.List;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
import kitchenpos.table.dto.OrderTableEmptyRequest;
import kitchenpos.table.dto.OrderTableNumberOfGuestsRequest;
import kitchenpos.table.dto.OrderTableRequest;
import kitchenpos.table.dto.OrderTableResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TableService {

    private final OrderTableRepository orderTableRepository;

    public TableService(final OrderTableRepository orderTableRepository) {
        this.orderTableRepository = orderTableRepository;
    }

    @Transactional
    public OrderTableResponse create(final OrderTableRequest orderTableRequest) {
        OrderTable savedOrderTable = orderTableRepository.save(orderTableRequest.toEntity());
        return OrderTableResponse.from(savedOrderTable);
    }

    public List<OrderTableResponse> list() {
        return OrderTableResponse.listFrom(orderTableRepository.findAll());
    }

    @Transactional
    public OrderTableResponse changeEmpty(final Long orderTableId,
                                          final OrderTableEmptyRequest orderTableEmptyRequest) {
        final OrderTable savedOrderTable = findOrderTable(orderTableId);

        savedOrderTable.changeEmpty(orderTableEmptyRequest.isEmpty());
        return OrderTableResponse.from(savedOrderTable);
    }

    @Transactional
    public OrderTableResponse changeNumberOfGuests(final Long orderTableId,
                                                   final OrderTableNumberOfGuestsRequest orderTableNumberOfGuestsRequest) {
        final OrderTable savedOrderTable = findOrderTable(orderTableId);
        savedOrderTable.changeNumberOfGuests(orderTableNumberOfGuestsRequest.getNumberOfGuests());

        return OrderTableResponse.from(savedOrderTable);
    }

    private OrderTable findOrderTable(final Long orderTableId) {
        return orderTableRepository.findById(orderTableId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("존재 하지 않는 테이블입니다.(id: %d)", orderTableId)
            ));
    }
}
