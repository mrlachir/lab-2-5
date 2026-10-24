package com.microservice.product;
import com.microservice.product.config.AppConfig;
import com.microservice.product.util.DriverCleanup;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import javax.sql.DataSource;

public class TestHibernate{
    public static void main(String[] args)throws Exception{
        try(AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class)){
            try(java.sql.Connection c=context.getBean(DataSource.class).getConnection()){
                System.out.println("DATABASE: "+c.getMetaData().getDatabaseProductName());
            }
            System.out.println("Contexte Spring initialise avec succes !");
        }finally{DriverCleanup.shutdown();}
    }
}
