/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppServer;

import core.tokens.NotificationToken;
import entities.Contact;
import entities.ContactReport;
import entities.Notification;
import entities.User;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Database {

    //    Comparator.comparing(Report::getReportKey)
//            .thenComparing(Report::getStudentNumber)
//            .thenComparing(Report::getSchool)

    private static int usersCount = 0;
    // TODO: Rimetti private
    private Comparator<ContactReport> cmpContacts =  Comparator.comparing
            (ContactReport::getStartDate)
            .thenComparing(ContactReport::getDuration)
            .thenComparing((e1, e2) -> Arrays.compare(e1.getReporterHashedCf(), e2.getReporterHashedCf()))
            .thenComparing((e1, e2) -> Arrays.compare(e1.getReportedHashedCf(), e2.getReportedHashedCf()));
    final HashMap<Integer, User> users = new HashMap<>();
    final TreeSet<ContactReport> contactReports = new TreeSet<>(cmpContacts);
    final TreeSet<Contact> contacts = new TreeSet<>(cmpContacts);
    final TreeSet<Notification> notifications = new TreeSet<>();

    public boolean addUser(byte[] hashedCf, byte[] password, byte[] userSalt) {
        return users.putIfAbsent(++usersCount, new User(usersCount, hashedCf, password, userSalt)) == null;
    }

    public User findUser(byte[] hashedCf) {
        for (User user : users.values()) {
            if (Arrays.equals(user.getHashedCf(), hashedCf)) {
                return user;
            }
        }
        return null;
    }

    public User findUser(int id) {
        return users.get(id);
    }

    public User updateUser(byte[] hashedCf, Timestamp lastLoginDate, Timestamp lastSwabCreationDate,
                           Timestamp lastPositiveSwabDate) {
        User user = findUser(hashedCf);
        if (user == null) {
            return null;
        }
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

    public boolean removeUser(byte[] hashedCf) {
        int userId = findUser(hashedCf).getId();
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
            if (Arrays.equals(contactReport.getReporterHashedCf(), reporterId) &&
                    Arrays.equals(contactReport.getReportedHashedCf(), reportedId) &&
                    contactReport.getStartDate().equals(startContactDate)) {
                return contactReport;
            }
        }
        return null;
    }

    //dev
    public boolean isAlreadyPresentContactReport(ContactReport c) {
        return this.contactReports.contains(c);
    }

    public List<ContactReport> searchContactReportsOfUsers(byte[] reporterId, byte[] reportedId) {
        return contactReports
                .stream()
                .filter(report -> Arrays.equals(report.getReporterHashedCf(), reporterId)
                        && Arrays.equals(report.getReportedHashedCf(), reportedId))
                .toList();
    }

    public List<ContactReport> searchContactReportOfReported(byte[] reportedId) {
        return contactReports.stream().filter(report -> Arrays.equals(report.getReportedHashedCf(), reportedId)).toList();
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
                    && (Arrays.equals(contact.getReporterHashedCf(), reporterId) && Arrays.equals(contact.getReportedHashedCf(), reportedId)
                    || (Arrays.equals(contact.getReporterHashedCf(), reportedId) && Arrays.equals(contact.getReportedHashedCf(), reporterId)))) {
                return contact;
            }
        }
        return null;
    }

    public List<Contact> searchContactsOfUser(byte[] userId) {

        return contacts
                .stream()
                .filter(contact -> Arrays.equals(contact.getReporterHashedCf(), userId) || Arrays.equals(contact.getReportedHashedCf(), userId))
                .toList();
    }

    public boolean removeContact(byte[] reporterId, byte[] reportedId, Timestamp startContactDate) {
        return contacts.remove(searchContact(reporterId, reportedId, startContactDate));
    }

    public boolean removeContactsUser(byte[] userId) {
        return contacts.removeAll(searchContactsOfUser(userId));
    }

    public boolean addNotification(String code) throws InvalidKeyException, NoSuchAlgorithmException {
        return notifications.add(new Notification(code));
    }

    public boolean addNotification(NotificationToken token) throws InvalidKeyException, NoSuchAlgorithmException {
        return notifications.add(new Notification(token));
    }

    public boolean addNotification(Notification n) throws InvalidKeyException, NoSuchAlgorithmException {
        return notifications.add(n);
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
