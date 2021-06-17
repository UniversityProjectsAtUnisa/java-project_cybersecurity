/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;
import java.util.Arrays;

/**
 *
 * @author marco
 */
public class Contact extends ContactReport {

    public Contact(byte[] id_segnalatore, byte[] id_segnalato, int durata, Timestamp data_inizio_contatto) {
        super(id_segnalatore, id_segnalato, durata, data_inizio_contatto);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactReport that = (ContactReport) o;
        return this.getDurata() == that.getDurata() && ((Arrays.equals(getId_segnalatore(), that.getId_segnalato()) && Arrays.equals(getId_segnalato(), that.getId_segnalato())) ||
                (Arrays.equals(getId_segnalatore(), that.getId_segnalato()) && Arrays.equals(getId_segnalato(), that.getId_segnalatore()))) && getData_inizio_contatto().equals(that.getData_inizio_contatto());
    }
}
