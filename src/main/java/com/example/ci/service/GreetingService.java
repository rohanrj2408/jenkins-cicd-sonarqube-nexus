package com.example.ci.service;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Builds greetings. The little bit of branching here (name validation and
 * trimming) exists mainly so there's something real for the unit tests and the
 * coverage report to chew on.
 */
@Service
public class GreetingService {

    private static final String DEFAULT_NAME = "World";

    // Each greeting gets a unique, incrementing id.
    private final AtomicLong counter = new AtomicLong();

    public String greet(String name) {
        String who = (name == null || name.isBlank()) ? DEFAULT_NAME : name.trim();
        return "Hello, " + who + "!";
    }

    public long nextId() {
        return counter.incrementAndGet();
    }
}
