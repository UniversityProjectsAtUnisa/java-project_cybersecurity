/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

/**
 */
public class User implements Comparable {
    private static int count = 0;

    private final byte[] cf;
    private final int id;
    private byte[] password;
    private byte[] sale_utente;
    private Timestamp data_ultimo_login;
    private Timestamp data_ultimo_tampone_positivo;
    private Timestamp data_creazione_ultimo_tampone;

    public User(byte[] cf,byte[] password,byte[] sale_utente) {
        this.cf = cf;
        this.id = count++;
        this.password = password;
        this.sale_utente = sale_utente;
        this.data_ultimo_login = null;
        this.data_creazione_ultimo_tampone = null;
        this.data_ultimo_tampone_positivo = null;
    }

    public User(byte[] cf,byte[] password,byte[] sale_utente,
                Timestamp data_ultimo_login, Timestamp data_creazione_ultimo_tampone,
                Timestamp data_ultimo_tampone_positivo) {
        this.cf = cf;
        this.id = count++;
        this.password = password;
        this.sale_utente = sale_utente;
        this.data_ultimo_login = data_ultimo_login;
        this.data_creazione_ultimo_tampone = data_creazione_ultimo_tampone;
        this.data_ultimo_tampone_positivo = data_ultimo_tampone_positivo;
    }

    public byte[] getCf() {
        return cf;
    }

    public int getId() {
        return id;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public byte[] getSale_utente() {
        return sale_utente;
    }

    public void setSale_utente(byte[] sale_utente) {
        this.sale_utente = sale_utente;
    }

    public Timestamp getData_ultimo_login() {
        return data_ultimo_login;
    }

    public void setData_ultimo_login(Timestamp data_ultimo_login) {
        this.data_ultimo_login = data_ultimo_login;
    }

    public Timestamp getData_ultimo_tampone_positivo() {
        return data_ultimo_tampone_positivo;
    }

    public void setData_ultimo_tampone_positivo(Timestamp data_ultimo_tampone_positivo) {
        this.data_ultimo_tampone_positivo = data_ultimo_tampone_positivo;
    }

    public Timestamp getData_creazione_ultimo_tampone() {
        return data_creazione_ultimo_tampone;
    }

    public void setData_creazione_ultimo_tampone(Timestamp data_creazione_ultimo_tampone) {
        this.data_creazione_ultimo_tampone = data_creazione_ultimo_tampone;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Arrays.equals(cf, user.cf) && Arrays.equals(password, user.password) && Arrays.equals(sale_utente, user.sale_utente) && Objects.equals(data_ultimo_login, user.data_ultimo_login) && Objects.equals(data_ultimo_tampone_positivo, user.data_ultimo_tampone_positivo) && Objects.equals(data_creazione_ultimo_tampone, user.data_creazione_ultimo_tampone);
    }


    @Override
    public int compareTo(Object o) {
        User user = (User) o;
        if(this == user){
           return 0;
        }else if(this.getId() > user.getId()){
            return 1;
        }else{
            return -1;
        }
    }
}
