/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author marco
 * @param <T>
 */
public class Counter<T> {
    final Map<T, Integer> counts = new HashMap<>();

    public void add(T t, int i) {
        counts.merge(t, i, Integer::sum);
    }

    public int count(T t) {
        return counts.getOrDefault(t, 0);
    }
    
    public List<T> mostCommon(int minValue) {
        return counts.entrySet().stream()
                // Sort by value.
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                // Keys only.
                .filter(e -> e.getValue() > minValue)
                .map(e -> e.getKey())
                // As a list.
                .toList();
    }
}