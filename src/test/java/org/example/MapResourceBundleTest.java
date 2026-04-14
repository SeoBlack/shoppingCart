package org.example;

import org.junit.jupiter.api.Test;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapResourceBundleTest {

    @Test
    void returnsMappedValueAndFallbackForMissingKey() {
        MapResourceBundle bundle = new MapResourceBundle(Map.of("hello", "world"));

        assertEquals("world", bundle.getObject("hello"));
        assertEquals("?missing?", bundle.getObject("missing"));
    }

    @Test
    void exposesKeysViaEnumerationAndKeySet() {
        MapResourceBundle bundle = new MapResourceBundle(Map.of("a", "1", "b", "2"));

        Enumeration<String> keys = bundle.getKeys();
        Set<String> keySet = bundle.keySet();

        assertTrue(keys.hasMoreElements());
        assertEquals(Set.of("a", "b"), keySet);
    }
}
