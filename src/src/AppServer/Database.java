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

    public boolean addUser(byte[] cf, byte[] password, byte[] userSalt) {
        return users.add(new User(cf, password, userSalt));
    }

    public User findUser(byte[] cf) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getCf() == cf) {
                return user;
            }
        }
        return null;
    }

    public User findUser(int id) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public User updateUser(byte[] cf, Timestamp lastLoginDate, Timestamp lastSwabCreationDate,
                           Timestamp lastPositiveSwabDate) {
        User user = findUser(cf);
        if (lastLoginDate != null) {
            user.setLastLoginDate(lastLoginDate);
            return user;
        }
        if (lastPositiveSwabDate != null) {
            user.setLastSwabCreationDate(lastPositiveSwabDate);
            return user;
        }
        if (lastPositiveSwabDate != null) {
            user.setLastPositiveSwabDate(lastPositiveSwabDate);
            return user;
        }
        return user;
    }

    public boolean removeUser(byte[] cf) {
        return users.remove(findUser(cf));
    }

    public boolean removeUser(int id) {
        return users.remove(findUser(id));
    }

    public boolean addContactReport(byte[] reporterId, byte[] reportedId, int duration, Timestamp startContactDate) {
        return contactReports.add(new ContactReport(reporterId, reportedId, duration, startContactDate));
    }

    public ContactReport searchContactReport(byte[] reporterId, byte[] reportedId, Timestamp startContactDate) {
        Iterator<ContactReport> iterator = contactReports.iterator();
        while (iterator.hasNext()) {
            ContactReport contactReport = iterator.next();
            if (contactReport.getReporterId() == reporterId && contactReport.getReportedId() == reportedId && contactReport.getStartContactDate() == startContactDate) {
                return contactReport;
            }
        }
        return null;
    }

    public LinkedList<ContactReport> searchContactReportOfUsers(byte[] reporterId, byte[] reportedId) {
        LinkedList<ContactReport> userContactReports = new LinkedList<>();
        Iterator<ContactReport> iterator = contactReports.iterator();
        while (iterator.hasNext()) {
            ContactReport contactReport = iterator.next();
            if (contactReport.getReporterId() == reporterId && contactReport.getReportedId() == reportedId) {
                userContactReports.add(contactReport);
            }
        }
        return userContactReports;
    }

    public boolean removeContactReport(byte[] reporterId, byte[] reportedId, Timestamp startContactDate) {
        return contactReports.remove(searchContactReport(reporterId, reportedId, startContactDate));
    }


    public boolean addContact(byte[] reporterId, byte[] reportedId, int duration, Timestamp startContactDate) {
        return contacts.add(new Contact(reporterId, reportedId, duration, startContactDate));
    }

    public Contact searchContact(byte[] reporterId, byte[] reportedId, Timestamp startContactDate) {
        Iterator<Contact> iterator = contacts.iterator();
        while (iterator.hasNext()) {
            Contact contact = iterator.next();
            if (contact.getReporterId() == reporterId && contact.getReportedId() == reportedId && contact.getStartContactDate() == startContactDate) {
                return contact;
            }
        }
        return null;
    }

    public LinkedList<Contact> searchContactsOfUser(byte[] id_user) {
        LinkedList<Contact> userContacts = new LinkedList<>();
        Iterator<Contact> iterator = contacts.iterator();

        while (iterator.hasNext()) {
            Contact contact = iterator.next();
            if (contact.getReporterId() == id_user || contact.getReportedId() == id_user) {
                userContacts.add(contact);
            }
        }
        return userContacts;
    }

    public boolean removeContact(byte[] reporterId, byte[] reportedId, Timestamp startContactDate) {
        return contacts.remove(searchContact(reporterId, reportedId, startContactDate));
    }

    public boolean removeContactsUser(byte[] id_user) {
        return contacts.removeAll(searchContactsOfUser(id_user));
    }

    public boolean addNotificationToken(String code, int id) {
        return notificationTokens.add(new NotificationToken(code, id));
    }

    public NotificationToken searchNotificationToken(String code) {
        Iterator<NotificationToken> iterator = notificationTokens.iterator();
        while (iterator.hasNext()) {
            NotificationToken notificationToken = iterator.next();
            if (notificationToken.getCode() == code) {
                return notificationToken;
            }
        }
        return null;
    }

    public LinkedList<NotificationToken> searchUserNotificationTokens(int id) {
        LinkedList<NotificationToken> userNotificationTokens = new LinkedList<>();
        Iterator<NotificationToken> iterator = userNotificationTokens.iterator();
        while(iterator.hasNext()){
            NotificationToken notificationToken = iterator.next();
            if(notificationToken.getId() == id){
                userNotificationTokens.add(notificationToken);
            }
        }
        return userNotificationTokens;
    }

    public NotificationToken updateNotificationToken(String code, Timestamp suspensionDate) {
        NotificationToken notificationToken = searchNotificationToken(code);
        notificationToken.setSuspensionDate(suspensionDate);
        return notificationToken;
    }

    public boolean removeNotificationToken(String code) {
        return notificationTokens.remove(searchNotificationToken(code));
    }
}
