package com.microservice.product;
import com.microservice.product.config.AppConfig;
import com.microservice.product.entity.Product;
import com.microservice.product.service.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.aop.support.AopUtils;
import org.junit.*;
import java.util.*;
import static org.junit.Assert.*;

public class ProductIntegrationTest{
    private AnnotationConfigApplicationContext context;
    private ProductService service;
    @Before public void setup(){
        context=new AnnotationConfigApplicationContext();
        Map<String,Object> properties=new HashMap<>();
        properties.put("lab.db","h2");
        properties.put("h2.url","jdbc:h2:mem:test_"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1");
        properties.put("hibernate.hbm2ddl.auto","create-drop");
        properties.put("hibernate.show_sql","false");
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test-database",properties));
        context.register(AppConfig.class);context.refresh();
        service=context.getBean(ProductService.class);
    }
    @After public void close(){if(context!=null)context.close();}
    @Test public void springTransactionProxyAndConnection(){
        assertTrue(AopUtils.isAopProxy(service));
        assertNotNull(context.getBean(org.hibernate.SessionFactory.class));
        assertTrue(service.getAllProducts().isEmpty());
    }
    @Test public void createReadUpdateDeleteAcrossTransactions(){
        Product p=new Product("Gateway",149.99);service.addProduct(p);
        assertNotNull(p.getId());assertEquals("Gateway",service.getProduct(p.getId()).getName());
        p.setName("Updated");p.setPrice(159.99);service.updateProduct(p);
        assertEquals(159.99,service.getProduct(p.getId()).getPrice(),0.001);
        assertEquals("Updated",service.getProduct(p.getId()).getName());
        service.deleteProduct(p.getId());assertNull(service.getProduct(p.getId()));
    }
    @Test public void validationRejectsNegativeNonFiniteAndBlank(){
        for(double value:new double[]{-1,Double.NaN,Double.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class,()->service.addProduct(new Product("Invalid",value)));
        assertThrows(IllegalArgumentException.class,()->service.addProduct(new Product(" ",10)));
        assertThrows(IllegalArgumentException.class,()->service.addProduct(null));
        assertTrue(service.getAllProducts().isEmpty());
    }
    @Test public void batchFailureRollsBackEarlierInsert(){
        assertThrows(IllegalArgumentException.class,()->service.addProducts(Arrays.asList(
            new Product("Inserted then rolled back",10),new Product("Bad",-1))));
        assertTrue(service.getAllProducts().isEmpty());
        service.addProduct(new Product("After rollback",0));
        assertEquals(1,service.getAllProducts().size());
    }
    @Test public void validBatchCommitsAll(){
        service.addProducts(Arrays.asList(new Product("A",1),new Product("B",2)));
        assertEquals(2,service.getAllProducts().size());
    }
    @Test public void invalidUpdateLeavesOriginalUntouched(){
        Product p=new Product("Original",10);service.addProduct(p);p.setPrice(-1);
        assertThrows(IllegalArgumentException.class,()->service.updateProduct(p));
        assertEquals(10,service.getProduct(p.getId()).getPrice(),0);
    }
    @Test public void missingProductsHandled(){
        assertNull(service.getProduct(999L));
        assertThrows(IllegalArgumentException.class,()->service.deleteProduct(999L));
        assertThrows(IllegalArgumentException.class,()->service.updateProduct(new Product("New",1)));
    }
}
