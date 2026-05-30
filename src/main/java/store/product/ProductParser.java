package store.product;

import java.util.List;

public class ProductParser {
    
    public static ProductOut to(Product p) {
        return p == null ? null :
            ProductOut.builder()
            .id(p.id())
            .name(p.name())
            .price(p.price())
            .unit(p.unit())
            .stock(p.stock())
            .build();
    }

    public static List<ProductOut> to(List<Product> l) {
        return l.stream().map(ProductParser::to).toList();
    }

    public static Product to(ProductIn in) {
        return in == null ? null :
            Product.builder()
            .name(in.name())
            .price(in.price())
            .unit(in.unit())
            .stock(in.stock() != null ? in.stock() : 0)
            .build();
    }

}
