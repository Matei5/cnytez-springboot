package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.FilterDto;
import cnytez.reddit.app.model.Filter;
import cnytez.reddit.app.repository.FilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterService {
    private final FilterRepository filterRepository;

    public List<FilterDto> getAllFilters() {
        return filterRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    private FilterDto toDto(Filter filter) {
        return new FilterDto(
                filter.getId(),
                filter.getName(),
                filter.getLabel()
        );
    }
}
