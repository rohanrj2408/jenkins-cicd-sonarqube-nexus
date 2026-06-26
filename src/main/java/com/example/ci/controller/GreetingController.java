package com.example.ci.controller;

import com.example.ci.model.Greeting;
import com.example.ci.service.GreetingService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private final GreetingService greetingService;

    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/api/greeting")
    public Greeting greeting(@RequestParam(defaultValue = "") String name) {
        return new Greeting(greetingService.nextId(), greetingService.greet(name));
    }

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
