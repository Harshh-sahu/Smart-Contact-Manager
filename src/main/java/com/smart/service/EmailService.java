package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {


    public boolean sendEmail(String subject, String message, String to) {
        boolean f = false;
        String from = "yumxpress2024@gmail.com";
        String host = "smtp.gmail.com";
        //get the system properties

        Properties properties = System.getProperties();
        System.out.println("PROPERTIES" + properties);


        //setting important information to properties object

        //host set

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");

        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");


        //step1: to get the session object

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // TODO Auto-generated method stub
                return new PasswordAuthentication("yumxpress2024@gmail.com", "mgow sotk kzzh pmil");
            }
        });

        session.setDebug(true);
        //step 2 : compose the message [text/multimedia];
        MimeMessage m = new MimeMessage(session);

        try {
            //from
            m.setFrom(from);
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            //adding subject to message

            m.setSubject(subject);

            //adding tet to message

//            m.setText(message);
            m.setContent(message,"text/html");

            //send

            //step 3: send the message using transpot class
            Transport.send(m);
            System.out.println("sent succesfullly");
            f = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return f;
    }

	
}
