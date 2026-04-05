package org.example;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;


public final class MapResourceBundle extends ResourceBundle {

    private final Map<String, String> strings;

    public MapResourceBundle(Map<String, String> strings) {
        this.strings = strings;
    }

    @Override
    protected Object handleGetObject(String key) {
        if (strings.containsKey(key)) {
            return strings.get(key);
        }
        return "?" + key + "?";
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(strings.keySet());
    }

    @Override
    protected Set<String> handleKeySet() {
        return strings.keySet();
    }
}
