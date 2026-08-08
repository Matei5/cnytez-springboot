package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.FilterDto;
import cnytez.reddit.app.mapper.FilterMapper;
import cnytez.reddit.app.model.Filter;
import cnytez.reddit.app.repository.FilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterService {
    private final FilterRepository filterRepository;
    private final FilterMapper filterMapper;

    public List<FilterDto> getAllFilters() {
        return filterRepository.findAll().stream()
                .map(filterMapper::toDto)
                .toList();
    }
}
