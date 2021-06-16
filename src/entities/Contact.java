/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;

/**
 *
 * @author marco
 */
public class Contact extends ContactReport {

    public Contact(byte[] id_segnalatore, byte[] id_segnalato, int durata, Timestamp data_inizio_contatto) {
        super(id_segnalatore, id_segnalato, durata, data_inizio_contatto);
    }
}
