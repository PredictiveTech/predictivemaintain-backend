package pe.edu.upc.predictivemaintain.maintenance.domain.model.valueobjects;

/**
 * What happened to one offline action when it reached the server.
 * APPLIED: done. ALREADY_APPLIED: it had been done before (a retry, or the order was already in that state).
 * SUPERSEDED: a more recent change exists, or the order cannot take the action any more: the server's version wins.
 * REJECTED: the action is not valid (unknown order, not assigned to the technician, empty summary, too old...).
 */
public enum SyncOutcome {
    APPLIED,
    ALREADY_APPLIED,
    SUPERSEDED,
    REJECTED
}