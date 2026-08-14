package com.cnytez.app.service;

import com.cnytez.app.dto.internal.FilterDto;
import com.cnytez.app.mapper.FilterMapper;
import com.cnytez.app.model.Filter;
import com.cnytez.app.repository.FilterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilterServiceTest {

    @Mock
    private FilterRepository filterRepository;

    @Mock
    private FilterMapper filterMapper;

    @InjectMocks
    private FilterService filterService;

    @Test
    void getAllFilters_ReturnsListOfFilters() {
        // arrange
        Filter filter = Filter.builder().id(1).name("TestFilter").build();
        FilterDto filterDto = new FilterDto(filter.getId(), "TestFilter", null);
        
        when(filterRepository.findAll()).thenReturn(List.of(filter));
        when(filterMapper.toDto(any(Filter.class))).thenReturn(filterDto);

        // act
        List<FilterDto> result = filterService.getAllFilters();

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TestFilter", result.get(0).name());
    }
}
