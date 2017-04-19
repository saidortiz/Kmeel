/*
 * This file is part of Kmeel.
 * Copyright (C) 2017  Marten4n6
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.kmeel.plugins.custom;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.utils.OSUtils;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.plugins.Utils;
import com.github.kmeel.plugins.model.PSTModel;
import com.pff.PSTAttachment;
import com.pff.PSTException;
import com.pff.PSTMessage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import lombok.extern.slf4j.Slf4j;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marten4n6
 *         Adds items to the DefaultContextMenu
 */
@Slf4j
public class PSTMenuItems {

    private KmeelAPI kmeelAPI;
    private MessagePane messagePane;
    private ContextMenu contextMenu;

    public PSTMenuItems(KmeelAPI kmeelAPI, MessagePane messagePane, ContextMenu contextMenu) {
        this.kmeelAPI = kmeelAPI;
        this.messagePane = messagePane;
        this.contextMenu = contextMenu;
    }

    public void add() {
        contextMenu.getItems().forEach(menuItem -> {
            if (menuItem.getText().equals("Export")) {
                Menu menuExport = (Menu) menuItem;
                MenuItem menuItemExportToEML = new MenuItem("Export to EML");

                menuExport.getItems().add(menuItemExportToEML);

                /* Listeners */
                menuItemExportToEML.setOnAction((event) -> {
                    exportToEML();
                });
            }
        });
    }

    private void exportToEML() {
        if (!messagePane.getTable().getSelectionModel().getSelectedItems().isEmpty()) {
            DirectoryChooser directoryChooser = new DirectoryChooser();

            File outputDirectory = directoryChooser.showDialog(null);

            if (outputDirectory != null) {
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> messagePane.getTable().setCursor(Cursor.WAIT));

                        AtomicInteger uniqueNumber = new AtomicInteger(1);

                        messagePane.getTable().getSelectionModel().getSelectedItems().forEach((row) -> {
                            PSTMessage message = ((PSTMessage) PSTModel.getInstance().getFromID(kmeelAPI, row.getId()));

                            try {
                                MimeMessage mimeMessage = getMimeMessage(message);

                                //Increment number until the file doesn't exist
                                while (new File(outputDirectory.getPath() + File.separator + uniqueNumber.get() + ".eml").exists()) {
                                    uniqueNumber.incrementAndGet();
                                }

                                mimeMessage.writeTo(new FileOutputStream(outputDirectory.getPath() + File.separator + uniqueNumber.get() + ".eml"));
                            } catch (MessagingException | IOException ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        });

                        Platform.runLater(() -> {
                            messagePane.getTable().setCursor(Cursor.DEFAULT);
                            new Alert(Alert.AlertType.INFORMATION, "Successfully exported to EML.", ButtonType.CLOSE).showAndWait();
                        });
                        return null;
                    }
                };

                new Thread(task).start();
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "No rows selected.", ButtonType.CLOSE).showAndWait();
        }
    }

    private MimeMessage getMimeMessage(PSTMessage message) {
        try {
            Properties properties = System.getProperties();
            properties.setProperty("mail.mime.address.strict", "false");

            MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(properties));

            mimeMessage.setFrom("\"" + message.getSenderEmailAddress() + "\"");
            mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, "\"" + message.getDisplayTo() + "\"");
            mimeMessage.setSubject(message.getSubject());

            MimeBodyPart content = new MimeBodyPart();

            content.setText(message.getBody());
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(content);

            if (message.getNumberOfAttachments() > 0) {
                for (int i = 0; i < message.getNumberOfAttachments(); i++) {
                    try {
                        PSTAttachment attachment = message.getAttachment(i);
                        String attachmentName = Utils.getAttachmentName(attachment);
                        MimeBodyPart mimeBodyPart = new MimeBodyPart();

                        Files.copy(attachment.getFileInputStream(), Paths.get(OSUtils.getTempPath() + attachmentName), StandardCopyOption.REPLACE_EXISTING);

                        mimeBodyPart.setDataHandler(new DataHandler(new FileDataSource(new File(OSUtils.getTempPath() + attachmentName))));
                        mimeBodyPart.setFileName(new File(OSUtils.getTempPath() + attachmentName).getName());
                        multipart.addBodyPart(mimeBodyPart);
                    } catch (PSTException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            }

            mimeMessage.setContent(multipart);
            return mimeMessage;
        } catch (IOException | MessagingException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
}
