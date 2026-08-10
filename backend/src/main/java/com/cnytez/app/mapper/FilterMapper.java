package com.cnytez.app.mapper;

import com.cnytez.app.dto.internal.FilterDto;
import com.cnytez.app.model.Filter;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface FilterMapper {
    FilterDto toDto(Filter filter);
}
