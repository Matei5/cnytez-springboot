package com.cnytez.app.service;

import com.cnytez.app.dto.internal.FilterDto;
import com.cnytez.app.mapper.FilterMapper;
import com.cnytez.app.repository.FilterRepository;
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
