package store.product;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import feign.Param;

public interface ProductRepository extends CrudRepository<ProductModel, String> {
    
    @Query("SELECT p FROM ProductModel p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ProductModel> searchByNameContaining(@Param("name") String name);
}
