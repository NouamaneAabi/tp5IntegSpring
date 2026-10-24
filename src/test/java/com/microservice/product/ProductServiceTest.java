package com.microservice.product;

import com.microservice.product.config.AppConfig;
import com.microservice.product.entity.Product;
import com.microservice.product.service.ProductService;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProductServiceTest.TestConfig.class)
public class ProductServiceTest {

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @Import(AppConfig.class)
    static class TestConfig {

        @Bean
        public DataSource dataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean
        public LocalSessionFactoryBean sessionFactory() {
            LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
            sessionFactory.setDataSource(dataSource());
            sessionFactory.setPackagesToScan("com.microservice.product.entity");

            Properties props = new Properties();
            props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            props.put("hibernate.show_sql", "true");
            props.put("hibernate.hbm2ddl.auto", "create-drop");
            sessionFactory.setHibernateProperties(props);

            return sessionFactory;
        }

        @Bean
        public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
            HibernateTransactionManager txManager = new HibernateTransactionManager();
            txManager.setSessionFactory(sessionFactory);
            return txManager;
        }
    }

    @Autowired
    private ProductService productService;

    @Test
    public void testAddAndGetProduct() {
        Product p = new Product("Laptop Pro", 1299.99);
        productService.addProduct(p);

        List<Product> products = productService.getAllProducts();
        Assertions.assertFalse(products.isEmpty());
        Assertions.assertEquals(1, products.size());
        Assertions.assertEquals("Laptop Pro", products.get(0).getName());
        Assertions.assertEquals(1299.99, products.get(0).getPrice());
    }

    @Test
    public void testNegativePriceThrowsException() {
        Product invalid = new Product("Free Item", -10.0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            productService.addProduct(invalid);
        });
    }
}
