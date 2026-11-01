package com.example.testgen.service;

import com.example.testgen.exception.InvalidSourceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassAnalyzerTest {

    private final ClassAnalyzer analyzer = new ClassAnalyzer();

    private static final String SRC = """
            package demo;
            public class Calc {
                private final Repo repo;
                public int add(int a, int b) { return a + b; }
                private void secret() {}
            }
            """;

    @Test
    void analyze_extractsPackageClassAndPublicMethods() {
        var info = analyzer.analyze(SRC);
        assertEquals("demo", info.packageName());
        assertEquals("Calc", info.className());
        assertEquals(1, info.methods().size());
        assertTrue(info.methods().get(0).contains("add"));
        assertEquals(1, info.dependencies().size());
    }

    @Test
    void analyze_throwsOnInvalidJava() {
        assertThrows(InvalidSourceException.class, () -> analyzer.analyze("ceci n'est pas du java"));
    }

    @Test
    void syntaxErrors_emptyForValidCode() {
        assertTrue(analyzer.syntaxErrors(SRC).isEmpty());
    }

    @Test
    void syntaxErrors_reportsBrokenCode() {
        assertFalse(analyzer.syntaxErrors("public class X { void a( }").isEmpty());
    }
}
