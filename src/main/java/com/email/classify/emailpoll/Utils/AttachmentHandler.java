package com.email.classify.emailpoll.Utils;

import com.email.classify.emailpoll.DAO.GmailExtractDAO;
import com.email.classify.emailpoll.GmailService.EmailAttachmentReceiver;
import com.email.classify.emailpoll.Repository.QuestionRepository;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class AttachmentHandler {
    @Autowired
   private QuestionRepository questionRepository;

    private static Logger log = Logger.getLogger(EmailAttachmentReceiver.class);
    public  void downloadEmailAttachments(String host, String port, String userName, String password) {

        Properties properties = new Properties();

        // server setting
        properties.put(CommonConstants.POP3_HOST, host);
        properties.put(CommonConstants.POP3_PORT, port);

        // SSL setting
        properties.setProperty(CommonConstants.POP3_CLASS,CommonConstants.JAVAX_SOCKET_FACTORY);
        properties.setProperty(CommonConstants.POP3_FALLBACK,CommonConstants.FALSE );
        properties.setProperty(CommonConstants.POP3_socketFactory, String.valueOf(port));

        Session session = Session.getDefaultInstance(properties);

        try {
            // connects to the message store
            Store store = session.getStore(CommonConstants.PROTOCOL);
            store.connect(userName, password);

            // opens the inbox folder
            Folder folderInbox = store.getFolder(CommonConstants.FOLDER_NAME);
            folderInbox.open(Folder.READ_ONLY);

            // fetches new messages from server
            Message[] arrayMessages = folderInbox.getMessages();

            for (int i = 0; i < arrayMessages.length; i++) {
                Message message = arrayMessages[i];
                Address[] fromAddress = message.getFrom();
                String from = fromAddress[0].toString();
               if (true) {
                   //CommonConstants.FROM_FILTER.equalsIgnoreCase(from)
                    String subject = message.getSubject();
                    String sentDate = message.getSentDate().toString();

                    String contentType = message.getContentType();
                    String messageContent = "";

                    // store attachment file name, separated by comma
                    StringBuilder attachFiles = new StringBuilder();

                    if (contentType.contains("multipart")) {
                        // content may contain attachments
                        Multipart multiPart = (Multipart) message.getContent();
                        int numberOfParts = multiPart.getCount();
                        for (int partCount = 0; partCount < numberOfParts; partCount++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                // this part is attachment
                                String fileName = part.getFileName();
                                attachFiles.append(fileName).append(", ");
                                part.saveFile(EmailAttachmentReceiver.saveDirectory + File.separator + fileName);
                            } else {
                                // this part may be the message content
                                messageContent = part.getContent().toString();
                            }
                        }

                        if (attachFiles.length() > 1) {
                            attachFiles = new StringBuilder(attachFiles.substring(0, attachFiles.length() - 2));
                        }
                    } else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                        Object content = message.getContent();
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    }
                    String newMessageContent = Jsoup.parse(messageContent).wholeText();
                    GmailExtractDAO gmailExtractDAO = new GmailExtractDAO();
                    gmailExtractDAO.setFrom(from);
                    gmailExtractDAO.setMessage(newMessageContent);
                    gmailExtractDAO.setSubject(subject);
                    gmailExtractDAO.setSentDate(sentDate);
                    gmailExtractDAO.setAttachment(attachFiles);
                    try {
                        questionRepository.save(gmailExtractDAO);
                    }
                    catch (Exception e){
                        System.out.println("failed here");
                    }
                    System.out.println("Message #" + (i + 1) + ":");
                    System.out.println("\t From: " + from);
                    System.out.println("\t Subject: " + subject);
                      System.out.println("\t Sent Date: " + sentDate);
                    System.out.println("\t Message: " + newMessageContent);
                    	System.out.println("\t Attachments: " + attachFiles);
                }
                else {
                    throw new AppException("Filter not matched");
                }
            }

            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for pop3.");
            ex.printStackTrace();
            log.error(ex);
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
            log.error(ex);
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error(ex);
        }catch (AppException ex){
            ex.printStackTrace();
            log.error("Filter not Matched during search");
        }
    }


}
