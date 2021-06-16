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

import java.util.TreeSet;

/**
 *
 * @author marco
 */
public class Database {
    private TreeSet<User> users;
    private TreeSet<ContactReport> contactReports;
    private TreeSet<Contact> contacts;
    private TreeSet<NotificationToken> notificationTokens;

    public Database() {
        this.users = new TreeSet<>();
        this.contactReports = new TreeSet<>();
        this.contacts = new TreeSet<>();
        this.notificationTokens = new TreeSet<>();

    }
}
