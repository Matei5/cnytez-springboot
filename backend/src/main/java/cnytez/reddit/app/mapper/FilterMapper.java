package cnytez.reddit.app.mapper;

import cnytez.reddit.app.dto.internal.FilterDto;
import cnytez.reddit.app.model.Filter;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface FilterMapper {
    FilterDto toDto(Filter filter);
}
