/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 */
public class NotificationToken {
    private final String codice;
    private Timestamp data_scadenza;
    private final Timestamp data_revoca;

    public NotificationToken(String codice, Timestamp data_revoca) {
        this.codice = codice;
        this.data_scadenza = this.getData_scadenza_from_codice(codice);
        this.data_revoca = data_revoca;
    }

    private Timestamp getData_scadenza_from_codice(String codice) {
        try {
            String data = codice.substring(codice.indexOf(",") + 1, codice.indexOf("."));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date parsedDate = dateFormat.parse(data);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            return timestamp;
        } catch (Exception e) {
            return null;
        }
    }

    public String getCodice() {
        return codice;
    }

    public Timestamp getData_scadenza() {
        return data_scadenza;
    }

    public Timestamp getData_revoca() {
        return data_revoca;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationToken that = (NotificationToken) o;
        return codice.equals(that.codice) && data_scadenza.equals(that.data_scadenza) && data_revoca.equals(that.data_revoca);
    }
}
