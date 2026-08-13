package com.cnytez.app.controller;

import com.cnytez.app.dto.internal.FilterDto;
import com.cnytez.app.service.FilterService;
import com.cnytez.app.service.JwtService;
import com.cnytez.app.logging.LogManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilterController.class)
@AutoConfigureMockMvc(addFilters = false)
class FilterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;
    
    @MockitoBean
    private LogManager logManager;

    @MockitoBean
    private FilterService filterService;

    @Test
    void getFilters_success() throws Exception {
        FilterDto filter1 = new FilterDto(1, "new", "New");
        FilterDto filter2 = new FilterDto(2, "top", "Top");
        
        when(filterService.getAllFilters()).thenReturn(List.of(filter1, filter2));

        mockMvc.perform(get("/filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("new"))
                .andExpect(jsonPath("$.data[1].name").value("top"));
    }
}
