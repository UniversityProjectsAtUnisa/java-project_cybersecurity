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
public class InvalidTokenSigmaException extends InvalidTokenException {

    private static final String message = "This sigma is not valid for this payload";

    public InvalidTokenSigmaException(String message) {
        super(message);
    }

    public InvalidTokenSigmaException() {
        super(message);
    }
}
