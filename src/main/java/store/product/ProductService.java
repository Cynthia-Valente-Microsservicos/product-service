package store.product;

import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;

    @CachePut(value = "products", key = "#result.id")
    public Product create(Product product) {
        return productRepository.save(new ProductModel(product)).to();
    }

    @CacheEvict(value = "products", key = "#id")
    public void delete(String id) {
        productRepository.deleteById(id);
    }

    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public Product findById(String id) {
        return productRepository.findById(id).orElse(null).to();
    }

    public List<Product> findByAll() {
        return StreamSupport.stream(
            productRepository.findAll().spliterator(),
            false 
        ).map(ProductModel::to)
        .toList();
    }
}
