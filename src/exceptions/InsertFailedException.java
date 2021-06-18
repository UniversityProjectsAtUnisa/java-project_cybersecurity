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
public class InsertFailedException extends ServerException {
    public InsertFailedException(String message) {
        super(message);
    }
}