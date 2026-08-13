package com.cnytez.app.controller;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security for tests
class AuthControllerTest {

}
