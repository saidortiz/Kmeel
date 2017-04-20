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

package com.github.kmeel.plugins.handlers;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.api.view.objects.Footer;
import com.github.kmeel.plugins.Utils;
import com.github.kmeel.plugins.model.PSTModel;
import com.pff.*;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author Marten4n6
 *         Handles message clicked events, updates the body, headers and attachments
 */
@Slf4j
public class MessageClickedHandler {

    private KmeelAPI kmeelAPI;

    public MessageClickedHandler(KmeelAPI kmeelAPI) {
        this.kmeelAPI = kmeelAPI;
    }

    public void handle(MessagePane messagePane, ID id) {
        PSTObject pstObject = PSTModel.getInstance().getFromID(kmeelAPI, id);
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(kmeelAPI.settings().get("DateFormat"));

        PSTModel.getInstance().getAttachmentFromID().clear();

        if (pstObject != null) {
            if (pstObject instanceof PSTAppointment) {
                // Appointment
                PSTAppointment appointment = (PSTAppointment) pstObject;

                messagePane.setBodyText(Utils.getBody(DATE_FORMAT, appointment));
                messagePane.setHeadersText(appointment.getTransportMessageHeaders().replaceAll("\n", "<br/>"));

                if (appointment.hasAttachments()) {
                    messagePane.setAttachmentTabAmount(appointment.getNumberOfAttachments());

                    for (int i = 0; i < appointment.getNumberOfAttachments(); i++) {
                        try {
                            PSTAttachment attachment = appointment.getAttachment(i);

                            PSTModel.getInstance().getAttachmentFromID().put(PSTModel.getInstance().getID(attachment), attachment.getFileInputStream());
                            Platform.runLater(() -> messagePane.getAttachmentsTable().getItems().add(Utils.getAttachmentRow(attachment, DATE_FORMAT)));
                        } catch (IOException | PSTException ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }
                }
            } else if (pstObject instanceof PSTContact) {
                // Contact
                PSTContact contact = (PSTContact) pstObject;

                messagePane.setBodyText(Utils.getBody(DATE_FORMAT, contact));
                messagePane.setHeadersText(contact.getTransportMessageHeaders().replaceAll("\n", "<br/>"));

                if (contact.hasAttachments()) {
                    messagePane.setAttachmentTabAmount(contact.getNumberOfAttachments());

                    for (int i = 0; i < contact.getNumberOfAttachments(); i++) {
                        try {
                            PSTAttachment attachment = contact.getAttachment(i);

                            PSTModel.getInstance().getAttachmentFromID().put(PSTModel.getInstance().getID(attachment), attachment.getFileInputStream());
                            Platform.runLater(() -> messagePane.getAttachmentsTable().getItems().add(Utils.getAttachmentRow(attachment, DATE_FORMAT)));
                        } catch (IOException | PSTException ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }
                }
            } else if (pstObject instanceof PSTTask) {
                // Task
                PSTTask task = (PSTTask) pstObject;

                messagePane.setBodyText(Utils.getBody(DATE_FORMAT, task));
                messagePane.setHeadersText(task.getTransportMessageHeaders().replaceAll("\n", "<br/>"));
            } else if (pstObject instanceof PSTRss) {
                // RSS
                log.error("RSS clicked, not yet supported.");
            } else if (pstObject instanceof PSTActivity) {
                // Activity
                log.error("Activity clicked, not yet supported.");
            } else {
                // Message
                PSTMessage message = (PSTMessage) pstObject;

                messagePane.setBodyText(Utils.getBody(DATE_FORMAT, message));
                messagePane.setHeadersText(message.getTransportMessageHeaders().replaceAll("\n", "<br/>"));

                if (message.hasAttachments()) {
                    messagePane.setAttachmentTabAmount(message.getNumberOfAttachments());

                    for (int i = 0; i < message.getNumberOfAttachments(); i++) {
                        try {
                            PSTAttachment attachment = message.getAttachment(i);

                            PSTModel.getInstance().getAttachmentFromID().put(PSTModel.getInstance().getID(attachment), attachment.getFileInputStream());
                            Platform.runLater(() -> messagePane.getAttachmentsTable().getItems().add(Utils.getAttachmentRow(attachment, DATE_FORMAT)));
                        } catch (IOException | PSTException ex) {
                            log.error(ex.getMessage());
                        }
                    }
                }
            }

            messagePane.setFooter(new Footer(PSTModel.getInstance().getFolderPath(kmeelAPI, id), kmeelAPI, messagePane.getTable()));
        }
    }
}
