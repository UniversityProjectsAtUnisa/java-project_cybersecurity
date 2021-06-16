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
public class ContactReport {
    private final byte[] id_segnalatore;
    private final byte[] id_segnalato;
    private final int durata;
    private final Timestamp data_inizio_contatto;


    public ContactReport(byte[] id_segnalatore, byte[] id_segnalato, int durata, Timestamp data_inizio_contatto) {
        this.id_segnalatore = id_segnalatore;
        this.id_segnalato = id_segnalato;
        this.durata = durata;
        this.data_inizio_contatto = data_inizio_contatto;
    }

    public Timestamp getData_inizio_contatto() {
        return data_inizio_contatto;
    }

    public int getDurata() {
        return durata;
    }

    public byte[] getId_segnalato() {
        return id_segnalato;
    }

    public byte[] getId_segnalatore() {
        return id_segnalatore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactReport that = (ContactReport) o;
        return durata == that.durata && Arrays.equals(id_segnalatore, that.id_segnalatore) && Arrays.equals(id_segnalato, that.id_segnalato) && data_inizio_contatto.equals(that.data_inizio_contatto);
    }

}
