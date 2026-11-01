package com.example.testgen.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeExtractorTest {

    @Test
    void stripsMarkdownFences() {
        String raw = "Voici :\n```java\nclass A {}\n```\nBonne chance";
        assertEquals("class A {}", CodeExtractor.extractJava(raw));
    }

    @Test
    void keepsPlainCode() {
        assertEquals("class A {}", CodeExtractor.extractJava("  class A {}  "));
    }

    @Test
    void handlesNull() {
        assertEquals("", CodeExtractor.extractJava(null));
    }
}
