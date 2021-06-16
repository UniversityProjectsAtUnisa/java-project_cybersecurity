/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppServer;

import entities.Contact;
import entities.ContactReport;
import entities.NotificationToken;
import entities.User;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 *
 */
public class Database {
    private TreeSet<User> users;
    private TreeSet<ContactReport> contactReports;
    private TreeSet<Contact> contacts;
    private TreeSet<NotificationToken> notificationTokens;

    public Database() {
        this.users = new TreeSet<User>();
        this.contactReports = new TreeSet<>();
        this.contacts = new TreeSet<>();
        this.notificationTokens = new TreeSet<>();
    }

    public boolean add_user(byte[] cf, int id, byte[] password, byte[] sale_utente) {
        return users.add(new User(cf, id, password, sale_utente));
    }

    public User find_user(byte[] cf) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getCf() == cf) {
                return user;
            }
        }
        return null;
    }

    public User find_user(int id) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public User update_user(byte[] cf, Timestamp data_ultimo_login, Timestamp data_creazione_ultimo_tampone,
                            Timestamp data_ultimo_tampone_positivo) {
        User user = find_user(cf);
        if (data_ultimo_login != null) {
            user.setData_ultimo_login(data_ultimo_login);
            return user;
        }
        if (data_creazione_ultimo_tampone != null) {
            user.setData_creazione_ultimo_tampone(data_creazione_ultimo_tampone);
            return user;
        }
        if (data_ultimo_tampone_positivo != null) {
            user.setData_ultimo_tampone_positivo(data_ultimo_tampone_positivo);
            return user;
        }
        return user;
    }

    public boolean remove_user(byte[] cf) {
        return users.remove(find_user(cf));
    }

    public boolean remove_user(int id) {
        return users.remove(find_user(id));
    }

    public boolean add_contactReport(byte[] id_segnalatore, byte[] id_segnalato, int durata, Timestamp data_inizio_contatto) {
        return contactReports.add(new ContactReport(id_segnalatore, id_segnalato, durata, data_inizio_contatto));
    }

    public ContactReport search_contactReport(byte[] id_segnalatore, byte[] id_segnalato, Timestamp data_inizio_contatto) {
        Iterator<ContactReport> iterator = contactReports.iterator();
        while (iterator.hasNext()) {
            ContactReport contactReport = iterator.next();
            if (contactReport.getId_segnalatore() == id_segnalatore && contactReport.getId_segnalato() == id_segnalato && contactReport.getData_inizio_contatto() == data_inizio_contatto) {
                return contactReport;
            }
        }
        return null;
    }

    public boolean remove_contactReport(byte[] id_segnalatore, byte[] id_segnalato, Timestamp data_inizio_contatto){
        return contactReports.remove(search_contactReport(id_segnalatore,id_segnalato,data_inizio_contatto));
    }


    public boolean add_contact(byte[] id_segnalatore, byte[] id_segnalato, int durata, Timestamp data_inizio_contatto) {
        return contacts.add(new Contact(id_segnalatore, id_segnalato, durata, data_inizio_contatto));
    }

    public Contact search_contact(byte[] id_segnalatore, byte[] id_segnalato, Timestamp data_inizio_contatto) {
        Iterator<Contact> iterator = contacts.iterator();
        while (iterator.hasNext()) {
            Contact contact = iterator.next();
            if (contact.getId_segnalatore() == id_segnalatore && contact.getId_segnalato() == id_segnalato && contact.getData_inizio_contatto() == data_inizio_contatto) {
                return contact;
            }
        }
        return null;
    }

    public LinkedList<Contact> search_contacts_of_user(byte[] id_user) {
        LinkedList<Contact> user_contacts = new LinkedList<>();
        Iterator<Contact> iterator = contacts.iterator();

        while (iterator.hasNext()) {
            Contact contact = iterator.next();
            if (contact.getId_segnalatore() == id_user || contact.getId_segnalato() == id_user) {
                user_contacts.add(contact);
            }
        }
        return user_contacts;
    }

    public boolean remove_contact(byte[] id_segnalatore, byte[] id_segnalato, Timestamp data_inizio_contatto){
        return contacts.remove(search_contact(id_segnalatore,id_segnalato,data_inizio_contatto));
    }

    public boolean remove_contacts_user(byte[] id_user){
        return contacts.removeAll(search_contacts_of_user(id_user));
    }

    public boolean add_notificationToken(String codice, Timestamp data_revoca){
        return notificationTokens.add(new NotificationToken(codice,data_revoca));
    }

    public NotificationToken search_notificationToken(String codice){
        Iterator<NotificationToken> iterator = notificationTokens.iterator();
        while (iterator.hasNext()) {
            NotificationToken notificationToken = iterator.next();
            if (notificationToken.getCodice() == codice) {
                return notificationToken;
            }
        }
        return null;
        return null;
    }

    public boolean remove_notificationToken(String codice){
        return notificationTokens.remove(search_notificationToken(codice));
    }
}
