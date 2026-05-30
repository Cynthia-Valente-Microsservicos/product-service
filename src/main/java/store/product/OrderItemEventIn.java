package store.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderItemEventIn(
    OrderProductEventIn product,
    Integer quantity
) {}
