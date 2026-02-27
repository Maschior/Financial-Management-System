package com.maschior.fms.controller;

import com.maschior.fms.api.controller.HomeController;
import com.maschior.fms.service.HomeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(HomeController.class)
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HomeService service;

    @Test
    void shouldReturnMessageFromService() throws Exception {
        when(service.getMessage()).thenReturn("Olha pedra");
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Olha pedra"));
    }
}
