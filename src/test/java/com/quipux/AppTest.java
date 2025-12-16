package com.quipux;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    @Test
    void placeholderTest() {
        assertEquals("Quipux API implementada con éxito", App.solvePlaceholder());
    }
}
