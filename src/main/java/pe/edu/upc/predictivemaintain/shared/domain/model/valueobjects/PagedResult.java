package pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects;

import java.util.List;

/**
 * One page of results plus the total, without depending on Spring Data's Page type.
 */
public record PagedResult<T>(List<T> items, long totalElements, int page, int size) {

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }
}