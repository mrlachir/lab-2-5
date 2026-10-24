package com.microservice.product.config;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.UUID;

@Configuration @PropertySource("classpath:application.properties")
public class HibernateConfig{
    private final Environment env;
    public HibernateConfig(Environment env){this.env=env;}
    private boolean h2(){return "h2".equals(env.getProperty("lab.db","mysql"));}
    private String setting(String environment,String key){
        return env.getProperty(environment,env.getRequiredProperty(key));
    }
    @Bean public DataSource dataSource(){
        DriverManagerDataSource source=new DriverManagerDataSource();
        source.setDriverClassName(h2()?"org.h2.Driver":env.getRequiredProperty("jdbc.driver"));
        source.setUrl(h2()?env.getProperty("h2.url","jdbc:h2:mem:products_"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1"):setting("DB_URL","jdbc.url"));
        source.setUsername(h2()?"sa":setting("DB_USER","jdbc.username"));
        source.setPassword(h2()?"":setting("DB_PASSWORD","jdbc.password"));
        return source;
    }
    @Bean public LocalSessionFactoryBean sessionFactory(DataSource dataSource){
        LocalSessionFactoryBean factory=new LocalSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.microservice.product.entity");
        Properties properties=new Properties();
        properties.setProperty("hibernate.dialect",h2()?"org.hibernate.dialect.H2Dialect":env.getRequiredProperty("hibernate.dialect"));
        properties.setProperty("hibernate.hbm2ddl.auto",env.getProperty("hibernate.hbm2ddl.auto","update"));
        properties.setProperty("hibernate.show_sql",env.getProperty("hibernate.show_sql","true"));
        properties.setProperty("hibernate.format_sql","true");
        factory.setHibernateProperties(properties);
        return factory;
    }
}
