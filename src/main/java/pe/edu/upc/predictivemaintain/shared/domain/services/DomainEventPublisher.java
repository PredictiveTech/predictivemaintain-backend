package pe.edu.upc.predictivemaintain.shared.domain.services;

import pe.edu.upc.predictivemaintain.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

/**
 * Port used by repositories to publish the events an aggregate has registered.
 * The domain only knows this interface, never Spring.
 */
public interface DomainEventPublisher {

    void publishAll(AbstractDomainAggregateRoot aggregate);
}