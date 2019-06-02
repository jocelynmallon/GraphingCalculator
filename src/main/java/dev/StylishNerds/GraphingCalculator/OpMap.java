/*
    OpMap.java
    This class holds the mapping model used
    to convert the text of buttons/keys
    into the appropriate Enum constants
 */
package dev.StylishNerds.GraphingCalculator;

import java.util.concurrent.ConcurrentHashMap;

public class OpMap {

    private final ConcurrentHashMap<String, String> map;

    public OpMap() {
        this.map = initMap();
    }

    /**
     * Creates and populates a simple, thread safe hashmap
     * for quickly mapping key text, and button labels,
     * to arithmetic enum constants.
     *
     * @return  the new hashmap
     */
    private ConcurrentHashMap<String, String> initMap() {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        map.put("+", "ADD");
        map.put("-", "SUB");
        map.put("×", "MLT");
        map.put("÷", "DIV");
        map.put("/", "DIV");
        map.put("%", "MOD");
        map.put("*", "MLT");
        map.put("ⁿ√x", "NRT");
        map.put("^", "EXP");
        map.put("sin", "SIN");
        map.put("cos", "COS");
        map.put("tan", "TAN");
        map.put("asin", "ASIN");
        map.put("acos", "ACOS");
        map.put("atan", "ATAN");
        map.put("√", "SQT");
        map.put("log", "LOG");
        return map;
    }

    public ConcurrentHashMap<String, String> getMap() {
        return this.map;
    }
}
