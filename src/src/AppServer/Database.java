/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppServer;

import entities.Contact;
import entities.ContactReport;
import entities.Notification;
import entities.User;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Database {

    private static int usersCount = 0;
    // TODO: Rimetti private
    final HashMap<Integer, User> users = new HashMap<>();
    final TreeSet<ContactReport> contactReports = new TreeSet<>();
    final TreeSet<Contact> contacts = new TreeSet<>();
    final TreeSet<Notification> notifications = new TreeSet<>();

    public boolean addUser(byte[] cf, byte[] password, byte[] userSalt) {
        return users.putIfAbsent(++usersCount, new User(usersCount, cf, password, userSalt)) == null;
    }

    public User findUser(byte[] cf) {
        for (User user : users.values()) {
            if (Arrays.equals(user.getCf(), cf)) {
                return user;
            }
        }
        return null;
    }

    public User findUser(int id) {
        return users.get(id);
    }

    public User updateUser(byte[] cf, Timestamp lastLoginDate, Timestamp lastSwabCreationDate,
            Timestamp lastPositiveSwabDate) {
        User user = findUser(cf);
        if (lastLoginDate != null) {
            user.setLastLoginDate(lastLoginDate);
        }
        if (lastPositiveSwabDate != null) {
            user.setLastSwabCreationDate(lastPositiveSwabDate);
        }
        if (lastPositiveSwabDate != null) {
            user.setLastPositiveSwabDate(lastPositiveSwabDate);
        }
        return user;
    }

    public boolean removeUser(byte[] cf) {
        int userId = findUser(cf).getId();
        return users.remove(userId) != null;
    }

    public boolean removeUser(int id) {
        return users.remove(id) != null;
    }

    public boolean addContactReport(byte[] reporterId, byte[] reportedId, int duration, Timestamp startContactDate) {
        return contactReports.add(new ContactReport(reporterId, reportedId, duration, startContactDate));
    }

    public boolean addContactReport(ContactReport report) {
        return contactReports.add(report);
    }

    public ContactReport searchContactReport(byte[] reporterId, byte[] reportedId, Timestamp startContactDate) {
        for (ContactReport contactReport : contactReports) {
            if (Arrays.equals(contactReport.getReporterId(), reporterId) && Arrays.equals(contactReport.getReportedId(), reportedId) && contactReport.getStartDate().equals(startContactDate)) {
                return contactReport;
            }
        }
        return null;
    }

    public List<ContactReport> searchContactReportsOfUsers(byte[] reporterId, byte[] reportedId) {
        return contactReports
                .stream()
                .filter(report -> Arrays.equals(report.getReporterId(), reporterId)
                && Arrays.equals(report.getReportedId(), reportedId))
                .toList();
    }

    public List<ContactReport> searchContactReportOfReported(byte[] reportedId) {
        return contactReports.stream().filter(report -> Arrays.equals(report.getReportedId(), reportedId)).toList();
    }

    public boolean removeContactReport(byte[] reporterId, byte[] reportedId, Timestamp startContactDate) {
        return contactReports.remove(searchContactReport(reporterId, reportedId, startContactDate));
    }

    public boolean addContact(byte[] reporterId, byte[] reportedId, int duration, Timestamp startContactDate) {
        return contacts.add(new Contact(reporterId, reportedId, duration, startContactDate));
    }

    public boolean addContact(ContactReport report) {
        System.out.println("CREAZIONE CONTATTO");
        Logger.getGlobal().log(Level.INFO, "Contact: {0}", report.toString());
        return contacts.add(new Contact(report));
    }

    public Contact searchContact(byte[] reporterId, byte[] reportedId, Timestamp startDate) {
        for (Contact contact : contacts) {
            // La data di inizio è uguale
            if (contact.getStartDate().equals(startDate)
                    // Il contatto è bidirezionale
                    && (Arrays.equals(contact.getReporterId(), reporterId) && Arrays.equals(contact.getReportedId(), reportedId)
                    || (Arrays.equals(contact.getReporterId(), reportedId) && Arrays.equals(contact.getReportedId(), reporterId)))) {
                return contact;
            }
        }
        return null;
    }

    public List<Contact> searchContactsOfUser(byte[] userId) {

        return contacts
                .stream()
                .filter(contact -> Arrays.equals(contact.getReporterId(), userId) || Arrays.equals(contact.getReportedId(), userId))
                .toList();
    }

    public boolean removeContact(byte[] reporterId, byte[] reportedId, Timestamp startContactDate) {
        return contacts.remove(searchContact(reporterId, reportedId, startContactDate));
    }

    public boolean removeContactsUser(byte[] userId) {
        return contacts.removeAll(searchContactsOfUser(userId));
    }

    public boolean addNotification(String code, int id) {
        return notifications.add(new Notification(code, id));
    }

    public Notification searchNotification(String code) {
        for (Notification notification : notifications) {
            if (notification.getCode().equals(code)) {
                return notification;
            }
        }
        return null;
    }

    public List<Notification> searchUserNotifications(int id) {
        return notifications.stream().filter(notification -> notification.getId() == id).toList();
    }

    public Notification updateNotification(String code, Timestamp suspensionDate) {
        Notification notification = searchNotification(code);
        if (notification == null) {
            return null;
        }
        notification.setSuspensionDate(suspensionDate);
        return notification;
    }

    public boolean removeNotification(String code) {
        return notifications.remove(searchNotification(code));
    }

}
