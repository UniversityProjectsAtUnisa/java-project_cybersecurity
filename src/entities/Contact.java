/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;

/**
 *
 * @author marco
 */
public class Contact implements Serializable {
    public int durata = 5;
    
    
    @Override
    public String toString() {
        return "Contact: {durata: " + this.durata + "}"; //To change body of generated methods, choose Tools | Templates.
    }
}
