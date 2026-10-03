package pe.edu.upc.predictivemaintain.shared.interfaces.rest.resources;

import pe.edu.upc.predictivemaintain.shared.domain.model.valueobjects.PagedResult;

import java.util.List;
import java.util.function.Function;

/**
 * Public shape of every paginated response.
 */
public record PagedResource<T>(List<T> items, long totalElements, int page, int size, int totalPages) {

    public static <S, T> PagedResource<T> from(PagedResult<S> result, Function<S, T> mapper) {
        return new PagedResource<>(
                result.items().stream().map(mapper).toList(),
                result.totalElements(),
                result.page(),
                result.size(),
                result.totalPages());
    }
}