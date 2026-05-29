package store.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class ProductResource implements ProductController {

    @Autowired
    private ProductService productService;
    
    @Override
    public ResponseEntity<List<ProductOut>> findAll() {
        return ResponseEntity.ok(
            ProductParser.to(
                productService.findByAll()
            )
        );
    }

    @Override
    public ResponseEntity<ProductOut> findById(String id) {
        Product out = productService.findById(id);
        return out == null ?
            ResponseEntity.notFound().build() :
            ResponseEntity.ok(
                ProductParser.to(out)
            );
    }

    @Override
    public ResponseEntity<Void> create(ProductIn in, @RequestHeader("role") String role){
        if (role == null || !role.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        final Product p = productService.create(
            ProductParser.to(in)
        );
        return ResponseEntity.created(
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(p.id())
                .toUri()
        ).build();
    }

    @Override
    public ResponseEntity<Void> delete(String id, @RequestHeader("role") String role){
        if (role == null || !role.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }  
}
