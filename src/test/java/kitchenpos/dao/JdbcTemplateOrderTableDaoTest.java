package kitchenpos.dao;

import static kitchenpos.domain.DomainCreator.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;

import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;

@JdbcTest
class JdbcTemplateOrderTableDaoTest {
    private JdbcTemplateOrderTableDao orderTableDao;
    private JdbcTemplateTableGroupDao tableGroupDao;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = DataSourceBuilder.initializeDataSource();
        orderTableDao = new JdbcTemplateOrderTableDao(dataSource);
        tableGroupDao = new JdbcTemplateTableGroupDao(dataSource);
    }

    @AfterEach
    void cleanUp() {
        dataSource = DataSourceBuilder.deleteDataSource();
    }

    @Test
    @DisplayName("생성하는 경우")
    void create() {
        OrderTable orderTable = createOrderTable(true);
        orderTable.setNumberOfGuests(1);
        OrderTable savedOrderTable = orderTableDao.save(orderTable);

        assertAll(
            () -> assertThat(savedOrderTable.getId()).isNotNull(),
            () -> assertThat(savedOrderTable.getNumberOfGuests()).isEqualTo(orderTable.getNumberOfGuests()),
            () -> assertThat(savedOrderTable.isEmpty()).isEqualTo(orderTable.isEmpty())
        );
    }

    @Test
    void findById() {
        OrderTable savedOrderTable = orderTableDao.save(createOrderTable(true));
        OrderTable expectedOrderTable = orderTableDao.findById(savedOrderTable.getId()).get();

        assertThat(expectedOrderTable.getId()).isEqualTo(savedOrderTable.getId());
    }

    @Test
    @DisplayName("업데이트하는 경우")
    void update() {
        OrderTable savedOrderTable = orderTableDao.save(createOrderTable(true));
        OrderTable foundOrderTable = orderTableDao.findById(savedOrderTable.getId()).get();

        OrderTable orderTable = createOrderTable(false);
        orderTable.setId(foundOrderTable.getId());

        OrderTable expectedOrderTable = orderTableDao.save(orderTable);

        assertThat(expectedOrderTable.getId()).isEqualTo(foundOrderTable.getId());
        assertThat(expectedOrderTable.isEmpty()).isNotEqualTo(foundOrderTable.isEmpty());
    }

    @Test
    void findAll() {
        orderTableDao.save(createOrderTable(true));
        orderTableDao.save(createOrderTable(true));

        assertThat(orderTableDao.findAll().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("주어진 id리스트에 orderTable의 아이디가 포함되어 있는 모든 orderTable 반환")
    void findAllByIdIn() {
        OrderTable orderTable1 = orderTableDao.save(createOrderTable(true));
        OrderTable orderTable2 = orderTableDao.save(createOrderTable(true));
        OrderTable orderTable3 = orderTableDao.save(createOrderTable(true));

        List<Long> ids = Arrays.asList(orderTable1.getId(), orderTable2.getId(), orderTable3.getId());
        List<OrderTable> orderTables = orderTableDao.findAllByIdIn(ids);

        assertThat(orderTables.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("주어진 table Group의 아이디가 동일한 orderTable 리스트 반환")
    void findAllByTableGroupId() {
        Long orderTable1Id = orderTableDao.save(createOrderTable(false)).getId();
        Long orderTable2Id = orderTableDao.save(createOrderTable(false)).getId();

        OrderTable orderTable1 = orderTableDao.findById(orderTable1Id).get();
        OrderTable orderTable2 = orderTableDao.findById(orderTable2Id).get();

        TableGroup tableGroup = tableGroupDao.save(createTableGroup(Arrays.asList(orderTable1, orderTable2)));

        orderTable1.setTableGroupId(tableGroup.getId());
        orderTable2.setTableGroupId(tableGroup.getId());
        orderTableDao.save(orderTable1);
        orderTableDao.save(orderTable2);

        List<OrderTable> orderTables = orderTableDao.findAllByTableGroupId(tableGroup.getId());

        assertThat(orderTables.size()).isEqualTo(2);
    }
}