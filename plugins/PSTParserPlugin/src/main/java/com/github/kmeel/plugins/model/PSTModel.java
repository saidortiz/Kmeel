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
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.model.objects.MessageAttachment;
import com.github.kmeel.api.model.objects.MessageRow;
import com.github.kmeel.api.spi.Message;
import com.github.kmeel.plugins.Utils;
import com.github.kmeel.plugins.model.object.PSTFileID;
import com.github.kmeel.plugins.model.object.TreeObject;
import com.pff.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Marten4n6
 */
@Slf4j
public class PSTModel {

    private static final PSTModel INSTANCE = new PSTModel();

    private @Getter HashMap<String, PSTFile> fileFromHash = new HashMap<>();
    private @Getter HashMap<String, InputStream> attachmentFromID = new HashMap<>();

    private PSTModel() {}

    /**
     * @return The PSTModel instance
     */
    public static PSTModel getInstance() {
        return INSTANCE;
    }

    /**
     * @return The PSTObject from the specified ID
     */
    public PSTObject getFromID(KmeelAPI kmeelAPI, ID id) {
        try {
            Query query = new TermQuery(new Term("ID", id.getId()));
            ScoreDoc[] hits = kmeelAPI.searcher().search(query, 1).scoreDocs;
            Document document = kmeelAPI.searcher().getDocument(hits[0].doc);

            return PSTObject.detectAndLoadPSTObject(fileFromHash.get(new PSTFileID(document.get("PSTFileID")).getId()), Long.parseLong(id.getId().split(" ")[0]));
        } catch (Exception ex) {
            // This ID doesn't belong to the PSTParser (hopefully)
            // log.debug(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * @return A list of IDs from the specified node
     */
    public Set<String> getFromNode(KmeelAPI kmeelAPI, TreeItem treeItem) {
        if (!(treeItem.getValue() instanceof TreeObject) || treeItem.getValue() == null) return null;

        try {
            TreeObject treeObject = (TreeObject) treeItem.getValue();
            Set<String> messageIDs = new HashSet<>();

            Query query = new TermQuery(new Term("PSTFolderID", treeObject.getFolderID()));
            ScoreDoc[] hits = kmeelAPI.searcher().search(query, Integer.MAX_VALUE).scoreDocs;

            for (int i = 0; i < hits.length; i++) {
                Document document = kmeelAPI.searcher().getDocument(hits[i].doc);

                if (document.get("PSTFolderID").equals(treeObject.getFolderID())) {
                    messageIDs.add(document.get("ID"));
                }
            }

            return messageIDs;
        } catch (Exception ex) {
            // This ID doesn't belong to the PSTParser (hopefully)
            // log.debug(ex.getMessage(), ex);
            return null;
        }
    }

    public String getFolderPath(KmeelAPI kmeelAPI, ID id) {
        try {
            Query query = new TermQuery(new Term("ID", id.getId()));
            ScoreDoc[] hits = kmeelAPI.searcher().search(query, 1).scoreDocs;
            Document document = kmeelAPI.searcher().getDocument(hits[0].doc);

            return document.get("FolderPath");
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * @return The Message from the specified ID
     */
    public Message getMessage(KmeelAPI kmeelAPI, ID id) {
        if (getFromID(kmeelAPI, id) == null) return null;

        return new Message() {
            @Override
            public MessageRow getRow() {
                PSTObject pstObject = getFromID(kmeelAPI, id);

                if (pstObject instanceof PSTAppointment) {
                    // Appointment
                    PSTAppointment appointment = (PSTAppointment) pstObject;

                    return new MessageRow(
                            kmeelAPI,
                            id,
                            appointment.getSenderEmailAddress(),
                            appointment.getDisplayTo(),
                            Utils.humanReadableByteCount(appointment.getMessageSize()),
                            appointment.getSubject(),
                            appointment.getDisplayCC(),
                            new SimpleObjectProperty<>(appointment.getMessageDeliveryTime()),
                            new SimpleStringProperty(kmeelAPI.tags().get(id)),
                            new SimpleBooleanProperty(kmeelAPI.bookmarks().isBookmark(id))
                    );
                } else if (pstObject instanceof PSTContact) {
                    // Contact
                    PSTContact contact = (PSTContact) pstObject;

                    return new MessageRow(
                            kmeelAPI,
                            id,
                            contact.getSenderEmailAddress(),
                            contact.getDisplayTo(),
                            Utils.humanReadableByteCount(contact.getMessageSize()),
                            contact.getSubject(),
                            contact.getDisplayCC(),
                            new SimpleObjectProperty<>(contact.getMessageDeliveryTime()),
                            new SimpleStringProperty(kmeelAPI.tags().get(id)),
                            new SimpleBooleanProperty(kmeelAPI.bookmarks().isBookmark(id))
                    );
                } else {
                    // Message
                    PSTMessage message = (PSTMessage) pstObject;

                    return new MessageRow(
                            kmeelAPI,
                            id,
                            message.getSenderEmailAddress(),
                            message.getDisplayTo(),
                            Utils.humanReadableByteCount(message.getMessageSize()),
                            message.getSubject(),
                            message.getDisplayCC(),
                            new SimpleObjectProperty<>(message.getMessageDeliveryTime()),
                            new SimpleStringProperty(kmeelAPI.tags().get(id)),
                            new SimpleBooleanProperty(kmeelAPI.bookmarks().isBookmark(id))
                    );
                }
            }

            @Override
            public String getBody() {
                return Utils.getBody(new SimpleDateFormat(kmeelAPI.settings().get("DateFormat")), getFromID(kmeelAPI, id));
            }

            @Override
            public String getHeaders() {
                PSTObject pstObject = getFromID(kmeelAPI, id);
                PSTMessage message = (PSTMessage) pstObject;

                return message.getTransportMessageHeaders().replaceAll("\n", "<br/>");
            }

            @Override
            public List<MessageAttachment> getAttachments() {
                PSTObject pstObject = getFromID(kmeelAPI, id);
                PSTMessage message = (PSTMessage) pstObject;

                if (message.hasAttachments()) {
                    List<MessageAttachment> attachments = new ArrayList<>();

                    for (int i = 0; i < message.getNumberOfAttachments(); i++) {
                        try {
                            PSTAttachment attachment = message.getAttachment(i);

                            attachments.add(new MessageAttachment(Utils.getAttachmentName(attachment), attachment.getFileInputStream(), null));
                        } catch (PSTException | IOException ex) {
                            log.error(ex.getMessage());
                        }
                    }
                    return attachments;
                }
                return null;
            }
        };
    }

    /*
     * DON'T TOUCH THIS.
     * The first part of the ID contains the descriptor node ID,
     * this is used to retrieve the PSTObject (PSTObject::detectAndLoadPSTObject).
     */

    public String getID(PSTMessage message) {
        String id = message.getInternetMessageId();
        return message.getDescriptorNodeId() + " " + DigestUtils.sha1Hex(id);
    }

    public String getID(PSTAppointment appointment) {
        String id = appointment.getInternetMessageId();
        return appointment.getDescriptorNodeId() + " " + DigestUtils.sha1Hex(id);
    }

    public String getID(PSTContact contact) {
        String id = contact.getInternetMessageId();
        return contact.getDescriptorNodeId() + " " + DigestUtils.sha1Hex(id);
    }

    public String getID(PSTAttachment attachment) {
        return DigestUtils.sha1Hex(attachment.getLongFilename() + attachment.getDescriptorNodeId());
    }
}
