package com.example.ci.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GreetingServiceTest {

    private final GreetingService service = new GreetingService();

    @Test
    void greetsNamedUser() {
        assertThat(service.greet("Iftekar")).isEqualTo("Hello, Iftekar!");
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertThat(service.greet("  Asha  ")).isEqualTo("Hello, Asha!");
    }

    @Test
    void fallsBackToDefaultForNull() {
        assertThat(service.greet(null)).isEqualTo("Hello, World!");
    }

    @Test
    void fallsBackToDefaultForBlank() {
        assertThat(service.greet("   ")).isEqualTo("Hello, World!");
    }

    @Test
    void idsAreUniqueAndIncreasing() {
        long first = service.nextId();
        long second = service.nextId();
        assertThat(second).isGreaterThan(first);
    }
}
