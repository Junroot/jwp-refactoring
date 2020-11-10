package kitchenpos.dao;

import static kitchenpos.domain.DomainCreator.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;

import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;

@JdbcTest
class JdbcTemplateMenuProductDaoTest {
    private JdbcTemplateMenuProductDao menuProductDao;
    private JdbcTemplateMenuDao menuDao;
    private JdbcTemplateProductDao productDao;
    private JdbcTemplateMenuGroupDao menuGroupDao;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = DataSourceBuilder.initializeDataSource();
        menuProductDao = new JdbcTemplateMenuProductDao(dataSource);
        menuDao = new JdbcTemplateMenuDao(dataSource);
        productDao = new JdbcTemplateProductDao(dataSource);
        menuGroupDao = new JdbcTemplateMenuGroupDao(dataSource);

        Product product = createProduct("product", BigDecimal.valueOf(1000));
        productDao.save(product);

        MenuGroup menuGroup = createMenuGroup("menuGroup");
        Long savedMenuGroupId = menuGroupDao.save(menuGroup).getId();

        menuDao.save(createMenu("menu1", savedMenuGroupId, BigDecimal.valueOf(2000))); //1L
        menuDao.save(createMenu("menu2", savedMenuGroupId, BigDecimal.valueOf(2000))); //2L
    }

    @AfterEach
    void cleanUp() {
        dataSource = DataSourceBuilder.deleteDataSource();
    }

    @Test
    void save() {
        MenuProduct menuProduct = createMenuProduct(1L, 1L, 1);

        MenuProduct savedMenuProduct = menuProductDao.save(menuProduct);

        assertAll(
            () -> assertThat(savedMenuProduct.getMenuId()).isEqualTo(menuProduct.getMenuId()),
            () -> assertThat(savedMenuProduct.getProductId()).isEqualTo(menuProduct.getProductId()),
            () -> assertThat(savedMenuProduct.getQuantity()).isEqualTo(menuProduct.getQuantity())
        );
    }

    @Test
    void findById() {
        MenuProduct menuProduct = createMenuProduct(1L, 1L, 1);
        MenuProduct savedMenuProduct = menuProductDao.save(menuProduct);

        MenuProduct expectedMenuProduct = menuProductDao.findById(savedMenuProduct.getSeq()).get();

        assertAll(
            () -> assertThat(expectedMenuProduct.getSeq()).isNotNull(),
            () -> assertThat(expectedMenuProduct.getMenuId()).isEqualTo(1L),
            () -> assertThat(expectedMenuProduct.getProductId()).isEqualTo(1L),
            () -> assertThat(expectedMenuProduct.getQuantity()).isEqualTo(1L)
        );
    }

    @Test
    void findAll() {
        MenuProduct menuProduct1 = createMenuProduct(1L, 1L, 1);
        MenuProduct menuProduct2 = createMenuProduct(1L, 1L, 1);

        menuProductDao.save(menuProduct1);
        menuProductDao.save(menuProduct2);
        List<MenuProduct> savedMenuProducts = menuProductDao.findAll();

        assertThat(savedMenuProducts.size()).isEqualTo(2);
    }

    @Test
    void findAllByMenuId() {
        MenuProduct menuProduct1 = createMenuProduct(1L, 1L, 1);
        MenuProduct menuProduct2 = createMenuProduct(1L, 1L, 1);
        MenuProduct menuProduct3 = createMenuProduct(2L, 1L, 1);

        menuProductDao.save(menuProduct1);
        menuProductDao.save(menuProduct2);
        menuProductDao.save(menuProduct3);

        List<MenuProduct> menuProducts = menuProductDao.findAllByMenuId(1L);

        assertThat(menuProducts.size()).isEqualTo(2);
    }
}