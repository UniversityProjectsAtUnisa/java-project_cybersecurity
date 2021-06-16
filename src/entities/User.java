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
public class User implements Serializable {

    public String name = "Marco";

    @Override
    public String toString() {
        return "User: {name: " + this.name.toString() + "}"; //To change body of generated methods, choose Tools | Templates.
    }

}
