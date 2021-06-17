/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

/**
 *
 * @author marco
 */
public class InvalidToken extends RuntimeException {
    public InvalidToken(String message) {
        super(message);
    }
}
