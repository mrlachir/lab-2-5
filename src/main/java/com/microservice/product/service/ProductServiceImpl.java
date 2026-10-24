package com.microservice.product.service;
import com.microservice.product.entity.Product;
import com.microservice.product.dao.IDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @Transactional(readOnly=true)
public class ProductServiceImpl implements ProductService{
    private final IDao<Product> productDao;
    public ProductServiceImpl(IDao<Product> productDao){this.productDao=productDao;}
    private void validate(Product p){
        if(p==null||p.getName()==null||p.getName().trim().isEmpty())
            throw new IllegalArgumentException("Product name is required");
        if(!Double.isFinite(p.getPrice())||p.getPrice()<0)
            throw new IllegalArgumentException("Price must be finite and non-negative");
    }
    @Transactional public void addProduct(Product p){validate(p);productDao.save(p);}
    @Transactional public void updateProduct(Product p){
        validate(p);
        if(p.getId()==null||productDao.findById(p.getId())==null)throw new IllegalArgumentException("Product not found");
        productDao.update(p);
    }
    @Transactional public void deleteProduct(Long id){
        Product p=productDao.findById(id);
        if(p==null)throw new IllegalArgumentException("Product not found");
        productDao.delete(p);
    }
    public Product getProduct(Long id){return productDao.findById(id);}
    public List<Product> getAllProducts(){return productDao.findAll();}
    @Transactional public void addProducts(List<Product> products){
        if(products==null)throw new IllegalArgumentException("List is required");
        // Persist as we go to prove the entire transaction rolls back if a later item fails.
        for(Product p:products){validate(p);productDao.save(p);}
    }
}
