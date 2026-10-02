package pe.edu.upc.predictivemaintain.shared.domain.model.aggregates;

import pe.edu.upc.predictivemaintain.shared.domain.model.events.DomainEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for aggregate roots. It collects domain events so they can be
 * published after the aggregate has been persisted.
 */
public abstract class AbstractDomainAggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Returns the pending events and clears the internal list.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}