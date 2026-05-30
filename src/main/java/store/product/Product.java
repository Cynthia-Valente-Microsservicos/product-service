package store.product;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder @Accessors(chain = true, fluent = true)
public class Product {
    private String id;
    private String name;
    private Float price;
    private String unit;
    private Integer stock;
}
