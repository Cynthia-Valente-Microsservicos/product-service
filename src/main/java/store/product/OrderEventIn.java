package store.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEventIn(
    String id,
    List<OrderItemEventIn> items
) {}
