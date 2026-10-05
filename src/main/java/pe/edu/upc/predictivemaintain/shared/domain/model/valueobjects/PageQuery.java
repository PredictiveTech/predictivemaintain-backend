package pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects;

import pe.edu.upc.predictivemaintain.shared.domain.exceptions.DomainValidationException;

/**
 * Which page of a collection is requested. Pages start at 0; the size is limited to protect the server.
 */
public record PageQuery(int page, int size) {

    public PageQuery {
        if (page < 0 || size < 1 || size > 100) {
            throw new DomainValidationException("validation.page.invalid");
        }
    }
}