/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

/**
 *
 * @author marco
 */
public class ContactReport {
    public String type = "Temporaneo";
    
    
    @Override
    public String toString() {
        return "ContactReport: {type: " + this.type.toString() + "}"; //To change body of generated methods, choose Tools | Templates.
    }
}
