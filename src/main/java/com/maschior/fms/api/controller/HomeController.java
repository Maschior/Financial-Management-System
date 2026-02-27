package com.maschior.fms.api.controller;

import com.maschior.fms.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final HomeService service;

    public HomeController(HomeService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String root() {
        return service.getMessage();
    }
}
