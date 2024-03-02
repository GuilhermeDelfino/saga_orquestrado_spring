package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilters;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.*;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    public Event save (Event event) {
        return eventRepository.save(event);
    }

    public void notifyEnding(Event event) {
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(event.getCreatedAt());

        save(event);
        log.info("Order {} with saga notified, transaction id {}", event, event.getTransactionId());
    }

    public List<Event> findAll(){
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters eventFilters){
        validateEmptyFilters(eventFilters);

        if(!isEmpty(eventFilters.getOrderId())){
            return eventRepository.findTop1ByOrderIdOrderByCreatedAtDesc(eventFilters.getOrderId())
                    .orElseThrow(() -> new ValidationException("Event not found by orderId"));
        }
        if(!isEmpty(eventFilters.getTransactionId())){
            return eventRepository.findTop1ByTransactionIdOrderByCreatedAtDesc(eventFilters.getTransactionId())
                    .orElseThrow(() -> new ValidationException("Event not found by transactionId"));
        }
        return null;
    }

    private void validateEmptyFilters(EventFilters eventFilters){
        if(isEmpty(eventFilters.getOrderId()) && isEmpty(eventFilters.getTransactionId())) {
            throw new ValidationException("OrderId or TransactionId must be informed");
        }
    }
}
