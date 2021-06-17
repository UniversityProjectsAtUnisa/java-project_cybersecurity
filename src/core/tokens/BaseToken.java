/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.tokens;

import java.sql.Timestamp;

/**
 * Auth:         BASE64(id, data_creazione).HMACSHA256(BASE64(id, data_creazione), sale_2)
 * Notification: BASE64(id, data_scadenza).HMACSHA256(BASE64(id, data_scadenza),SHA256(sale_2 || SHA256(sale_1  || codice_fiscale)))
 */
public class BaseToken {
    private String payload;
    private String sigma;
    private Timestamp createdAt;

    public BaseToken() {
    }

    public BaseToken(String raw) {
        int separatorIndex = raw.indexOf(".");
        this.payload = raw.substring(0, separatorIndex);
        this.sigma = raw.substring(separatorIndex + 1);
    }

    public String getPayload() {
        return payload;
    }

    public String getSigma() {
        return sigma;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setSigma(String sigma) {
        this.sigma = sigma;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
