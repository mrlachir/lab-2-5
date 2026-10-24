package com.microservice.product;
import com.microservice.product.config.AppConfig;
import com.microservice.product.entity.Product;
import com.microservice.product.service.ProductService;
import com.microservice.product.util.DriverCleanup;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import java.util.Arrays;

public class MainApp{
    public static void main(String[] args){
        try(AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class)){
            ProductService service=context.getBean(ProductService.class);
            Product p=new Product("Microservice Gateway",149.99);
            service.addProduct(p);
            System.out.println("PRODUIT AJOUTE : "+p);
            System.out.println("Total en base : "+service.getAllProducts().size());
            p.setPrice(159.99);service.updateProduct(p);
            System.out.println("PRODUIT MODIFIE : "+service.getProduct(p.getId()));
            int before=service.getAllProducts().size();
            try{
                service.addProducts(Arrays.asList(new Product("Rollback candidate",10),new Product("Invalid",-1)));
                throw new IllegalStateException("Expected failure missing");
            }catch(IllegalArgumentException expected){
                if(service.getAllProducts().size()!=before)throw new IllegalStateException("Rollback failed");
                System.out.println("ROLLBACK CONFIRME : aucun produit du lot invalide conserve");
            }
            Product temp=new Product("Temporary demo",1);service.addProduct(temp);service.deleteProduct(temp.getId());
            System.out.println("SUPPRESSION CONFIRMEE : "+(service.getProduct(temp.getId())==null));
            System.out.println("LISTE FINALE : "+service.getAllProducts());
            System.out.println("DEMO TERMINEE");
        }finally{DriverCleanup.shutdown();}
    }
}
