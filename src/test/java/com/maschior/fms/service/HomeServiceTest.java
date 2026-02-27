package com.maschior.fms.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeServiceTest {

    private HomeService service = new HomeService();

    @Test
    void shouldReturnMessage() {
        assertEquals("Olha pedra", service.getMessage());
    }
}
