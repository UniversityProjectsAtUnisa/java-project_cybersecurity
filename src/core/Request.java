/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.Serializable;

/**
 *
 * @author marco
 */
public class Request implements Serializable{

    private String endpointName;
    
    public Request(String endpointName) {
        this.endpointName = endpointName;
    }

    public String getEndpointName() {
        return endpointName;
    }
    
}
