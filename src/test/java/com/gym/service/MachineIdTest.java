package com.gym.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MachineIdTest {

    @Test
    void idOfThisComputerIsWellFormedAndStable() {
        String id = MachineId.current();
        assertNotNull(MachineId.normalize(id));
        assertEquals(id, MachineId.current());
    }

    @Test
    void normalizeAcceptsOnlyTheIdFormat() {
        assertEquals("3F9A-0C72-B1D4-5E88", MachineId.normalize(" 3f9a-0c72-b1d4-5e88 "));
        assertNull(MachineId.normalize("nonsense"));
        assertNull(MachineId.normalize(null));
    }

    @Test
    void differentComputersGiveDifferentIds() {
        assertEquals(MachineId.format("abc"), MachineId.format(" ABC "));
        assertNotEquals(MachineId.format("abc"), MachineId.format("abd"));
    }
}
