package com.example.ci.model;

/**
 * A greeting returned by the API. Using a record keeps it immutable and gives
 * us equals/hashCode/toString for free.
 */
public record Greeting(long id, String content) {
}
