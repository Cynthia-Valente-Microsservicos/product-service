package store.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "product-service")
    public void onOrderEvent(String message) {
        try {
            OrderEventIn event = objectMapper.readValue(message, OrderEventIn.class);
            for (OrderItemEventIn item : event.items()) {
                productService.reduceStock(item.product().id(), item.quantity());
            }
            System.out.println("[KAFKA] Estoque atualizado para pedido: " + event.id());
        } catch (Exception e) {
            System.err.println("[KAFKA] Erro ao processar evento de pedido: " + e.getMessage());
        }
    }
}
