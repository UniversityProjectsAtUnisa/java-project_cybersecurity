/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.List;

/**
 *
 * @author marco
 */
public class RandomUtils {

    public static int randomIntFromInterval(int min, int max) { // min and max included 
        return (int) Math.floor(Math.random() * (max - min + 1) + min);
    }

    public static <T> T pickOne(List<T> coll) {
        if (coll.isEmpty()) {
            return null;
        }
        int randomIndex = randomIntFromInterval(0, coll.size() - 1);
        return coll.get(randomIndex);
    }
}
