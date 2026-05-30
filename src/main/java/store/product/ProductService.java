package store.product;

import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return productRepository.findById(id)
        .map(ProductModel::to)
        .orElse(null);
    }

    public List<Product> findByAll() {
        return StreamSupport.stream(
            productRepository.findAll().spliterator(),
            false 
        ).map(ProductModel::to)
        .toList();
    }

    public List<Product> findByNameLike(String name) {
        return productRepository.searchByNameContaining(name).stream()
            .map(ProductModel::to)
            .toList();
    }

    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void reduceStock(String productId, Integer quantity) {
        productRepository.findById(productId).ifPresent(product -> {
            int newStock = Math.max(0, product.stock() - quantity);
            product.stock(newStock);
            productRepository.save(product);
        });
    }
}
