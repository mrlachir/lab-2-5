package com.microservice.product.dao;
import com.microservice.product.entity.Product;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductDaoImpl implements IDao<Product>{
    private final SessionFactory sessionFactory;
    public ProductDaoImpl(SessionFactory sessionFactory){this.sessionFactory=sessionFactory;}
    public void save(Product product){sessionFactory.getCurrentSession().persist(product);}
    public void update(Product product){sessionFactory.getCurrentSession().merge(product);}
    public void delete(Product product){
        org.hibernate.Session session=sessionFactory.getCurrentSession();
        Product managed=session.get(Product.class,product.getId());
        if(managed==null)throw new IllegalArgumentException("Product not found");
        session.delete(managed);
    }
    public Product findById(Long id){return sessionFactory.getCurrentSession().get(Product.class,id);}
    public List<Product> findAll(){return sessionFactory.getCurrentSession().createQuery("FROM Product ORDER BY id",Product.class).list();}
}
