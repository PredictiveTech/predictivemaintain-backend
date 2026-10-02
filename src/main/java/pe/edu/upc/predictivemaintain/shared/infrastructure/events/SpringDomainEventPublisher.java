package pe.edu.upc.predictivemaintain.shared.infrastructure.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import pe.edu.upc.predictivemaintain.shared.domain.services.DomainEventPublisher;

/**
 * Publishes domain events through Spring's in-process event bus.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publishAll(AbstractDomainAggregateRoot aggregate) {
        aggregate.pullDomainEvents().forEach(event -> publisher.publishEvent(event));
    }
}