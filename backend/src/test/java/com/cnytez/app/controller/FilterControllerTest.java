package com.cnytez.app.controller;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

@WebMvcTest(FilterController.class)
@AutoConfigureMockMvc(addFilters = false)
class FilterControllerTest {

}
