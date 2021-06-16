/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.nio.charset.StandardCharsets;

/**
 *
 * @author marco
 */
public class Utils {
    public static byte[] toByteArray(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }
    
    public static String toString(byte[] array) {
        return new String(array, StandardCharsets.UTF_8);
    }
}
