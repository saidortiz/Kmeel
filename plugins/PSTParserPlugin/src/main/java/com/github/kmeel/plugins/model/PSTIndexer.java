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

package com.github.kmeel.plugins.model;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.plugins.Utils;
import com.github.kmeel.plugins.model.object.PSTFileID;
import com.github.kmeel.plugins.model.object.PSTFolderID;
import com.pff.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author Marten4n6
 *         This class indexes PST files
 */
@Slf4j
public class PSTIndexer {

    private KmeelAPI kmeelAPI;
    private SimpleDateFormat DATE_FORMAT;

    public PSTIndexer(KmeelAPI kmeelAPI) {
        this.kmeelAPI = kmeelAPI;
        DATE_FORMAT = new SimpleDateFormat(kmeelAPI.settings().get("DateFormat"));
    }

    /*
     * DON'T change StringField to TextField for IDs or it will break things:
     * TextField indexed content is broken into separate tokens where there is no need for an exact match.
     * A StringField will only give results for exact matches.
     */

    public void index(PSTFile pstFile, PSTFolder pstFolder, String folderPath, PSTAppointment appointment) {
        Document document = new Document();
        StringBuilder searchableText = new StringBuilder();

        document.add(new StringField("ID", PSTModel.getInstance().getID(appointment).getId(), Field.Store.YES));
        searchableText.append(" ").append(PSTModel.getInstance().getID(appointment));

        document.add(new StringField("PSTFileID", new PSTFileID(pstFile).getId(), Field.Store.YES));
        searchableText.append(" ").append(new PSTFileID(pstFile).getId());

        // Used to get all messages from a specific PSTFolder by
        // searching for the folder ID.
        document.add(new StringField("PSTFolderID", new PSTFolderID(pstFolder).getId(), Field.Store.YES));
        searchableText.append(" ").append(new PSTFolderID(pstFolder).getId());

        document.add(new StringField("FolderPath", folderPath, Field.Store.YES));

        if (!appointment.getSubject().isEmpty()) {
            document.add(new TextField("Subject", appointment.getSubject(), Field.Store.NO));
            searchableText.append(" ").append(appointment.getSubject());
        }

        String body = Utils.getBody(DATE_FORMAT, appointment);
        if (!body.isEmpty()) {
            document.add(new TextField("Body", body, Field.Store.NO));
            searchableText.append(" ").append(body);
        }
        if (appointment.getAllAttendees().isEmpty()) {
            document.add(new TextField("Attendees", appointment.getAllAttendees(), Field.Store.NO));
            searchableText.append(" ").append(appointment.getAllAttendees());
        }
        if (appointment.getMessageDeliveryTime() != null) {
            document.add(new TextField("DeliveryTime", DATE_FORMAT.format(appointment.getMessageDeliveryTime()), Field.Store.NO));
            searchableText.append(" ").append(DATE_FORMAT.format(appointment.getMessageDeliveryTime()));
        }
        if (appointment.getClientSubmitTime() != null) {
            document.add(new TextField("SubmitTime", DATE_FORMAT.format(appointment.getClientSubmitTime()), Field.Store.NO));
            searchableText.append(" ").append(DATE_FORMAT.format(appointment.getClientSubmitTime()));
        }

        if (appointment.hasAttachments()) {
            for (int i = 0; i < appointment.getNumberOfAttachments(); i++) {
                try {
                    PSTAttachment attachment = appointment.getAttachment(i);

                    document.add(new TextField("AttachmentName", attachment.getLongFilename(), Field.Store.NO));
                    searchableText.append(" ").append(attachment.getLongFilename());
                } catch (PSTException | IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }

        try {
            document.add(new TextField("Contents", searchableText.toString(), Field.Store.NO));
            kmeelAPI.indexer().getIndexWriter().addDocument(document);
        } catch (IOException ex) {
            log.error(ex.getMessage() + ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
        }
    }

    public void index(PSTFile pstFile, PSTFolder pstFolder, String folderPath, PSTContact contact) {
        Document document = new Document();
        StringBuilder searchableText = new StringBuilder();

        document.add(new StringField("ID", PSTModel.getInstance().getID(contact).getId(), Field.Store.YES));
        searchableText.append(" ").append(PSTModel.getInstance().getID(contact));

        document.add(new StringField("PSTFileID", new PSTFileID(pstFile).getId(), Field.Store.YES));
        searchableText.append(" ").append(new PSTFileID(pstFile).getId());

        // Used to get all messages from a specific PSTFolder
        // by searching for the ID.
        document.add(new StringField("PSTFolderID", new PSTFolderID(pstFolder).getId(), Field.Store.YES));
        searchableText.append(" ").append(new PSTFolderID(pstFolder).getId());

        document.add(new StringField("FolderPath", folderPath, Field.Store.YES));

        if (!contact.getSubject().isEmpty()) {
            document.add(new TextField("Subject", contact.getSubject(), Field.Store.NO));
            searchableText.append(" ").append(contact.getSubject());
        }

        String body = Utils.getBody(DATE_FORMAT, contact);
        if (!body.isEmpty()) {
            document.add(new TextField("Body", body, Field.Store.NO));
            searchableText.append(" ").append(body);
        }

        try {
            document.add(new TextField("Contents", searchableText.toString(), Field.Store.NO));
            kmeelAPI.indexer().getIndexWriter().addDocument(document);
        } catch (IOException ex) {
            log.error(ex.getMessage() + ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
        }
    }

    public void index(PSTFile pstFile, PSTFolder pstFolder, String folderPath, PSTMessage message) {
        Document document = new Document();
        StringBuilder searchableText = new StringBuilder();

        document.add(new StringField("ID", PSTModel.getInstance().getID(message).getId(), Field.Store.YES));
        searchableText.append(" ").append(PSTModel.getInstance().getID(message));

        document.add(new StringField("PSTFileID", new PSTFileID(pstFile).getId(), Field.Store.YES));
        searchableText.append(" ").append(new PSTFileID(pstFile).getId());

        // Used to get all messages from a specific PSTFolder
        // by searching for the ID.
        document.add(new StringField("PSTFolderID", new PSTFolderID(pstFolder).getId(), Field.Store.YES));
        searchableText.append(" ").append(new PSTFolderID(pstFolder).getId());

        document.add(new StringField("FolderPath", folderPath, Field.Store.YES));

        if (!message.getSubject().isEmpty()) {
            document.add(new TextField("Subject", message.getSubject(), Field.Store.NO));
            searchableText.append(" ").append(message.getSubject());
        }
        if (!message.getSenderName().isEmpty()) {
            document.add(new TextField("From", message.getSenderName(), Field.Store.NO));
            searchableText.append(" ").append(message.getSenderName());
        }

        String body = Utils.getBody(DATE_FORMAT, message);
        if (!body.isEmpty()) {
            document.add(new TextField("Body", body, Field.Store.NO));
            searchableText.append(" ").append(body);
        }
        if (!message.getTransportMessageHeaders().isEmpty()) {
            document.add(new TextField("Headers", message.getTransportMessageHeaders(), Field.Store.NO));
            searchableText.append(" ").append(message.getTransportMessageHeaders());
        }
        if (message.getMessageDeliveryTime() != null) {
            document.add(new TextField("Received", DATE_FORMAT.format(message.getMessageDeliveryTime()), Field.Store.NO));
            searchableText.append(" ").append(DATE_FORMAT.format(message.getMessageDeliveryTime()));
        }
        if (message.getClientSubmitTime() != null) {
            document.add(new TextField("SubmitTime", DATE_FORMAT.format(message.getClientSubmitTime()), Field.Store.NO));
            searchableText.append(" ").append(DATE_FORMAT.format(message.getClientSubmitTime()));
        }

        if (message.hasAttachments()) {
            for (int i = 0; i < message.getNumberOfAttachments(); i++) {
                try {
                    PSTAttachment attachment = message.getAttachment(i);

                    document.add(new TextField("AttachmentName", attachment.getLongFilename(), Field.Store.NO));
                    searchableText.append(" ").append(attachment.getLongFilename());
                } catch (PSTException | IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }

        try {
            document.add(new TextField("Contents", searchableText.toString(), Field.Store.NO));
            kmeelAPI.indexer().getIndexWriter().addDocument(document);
        } catch (IOException ex) {
            log.error(ex.getMessage() + ": " + Thread.currentThread().getStackTrace()[2].getLineNumber());
        }
    }
}
