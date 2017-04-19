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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Marten4n6
 */
@Slf4j
public class EMLModel {

    private KmeelAPI kmeelAPI;

    public EMLModel(KmeelAPI kmeelAPI) {
        this.kmeelAPI = kmeelAPI;
    }

    /**
     * @return The MimeMessage from the specified ID
     */
    public MimeMessage getFromID(ID id) {
        try {
            Query query = new TermQuery(new Term("ID", id.getId()));
            ScoreDoc[] hits = kmeelAPI.searcher().search(query, 1).scoreDocs;
            Document document = kmeelAPI.searcher().getDocument(hits[0].doc);

            return new MimeMessage(null, new FileInputStream(new File(document.get("Path"))));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * @return A list of message IDs from the specified folder
     */
    public Set<String> getFromFolder(String folderPath) {
        Set<String> messageIDs = new HashSet<>();

        try {
            Query query = new TermQuery(new Term("FolderPath", folderPath));
            TopDocs topDocs = kmeelAPI.searcher().search(query, Integer.MAX_VALUE);
            ScoreDoc[] hits = topDocs.scoreDocs;

            for (int i = 0; i < hits.length; i++) {
                Document document = kmeelAPI.searcher().getDocument(hits[i].doc);

                if (document.get("FolderPath").equals(folderPath)) {
                    messageIDs.add(document.get("ID"));
                }
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return messageIDs;
    }

    public Message getMessage(ID id) {
        if (getFromID(id) == null) return null;

        return new Message() {
            @Override
            public MessageRow getRow() {
                try {
                    MimeMessage message = new MimeMessage(getFromID(id));
                    MimeMessageParser parser = new MimeMessageParser(message).parse();

                    return new MessageRow(
                            kmeelAPI,
                            id,
                            parser.getFrom(),
                            (parser.getTo().isEmpty() ? "" : parser.getCc().stream().map(Object::toString).collect(Collectors.joining(", "))),
                            Utils.humanReadableByteCount(message.getSize()),
                            message.getSubject(),
                            (parser.getCc().isEmpty() ? "" : parser.getCc().stream().map(Object::toString).collect(Collectors.joining(", "))),
                            new SimpleObjectProperty<>(message.getSentDate()),
                            new SimpleStringProperty(kmeelAPI.tags().get(id)),
                            new SimpleBooleanProperty(kmeelAPI.bookmarks().isBookmark(id)));
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
                return null;
            }

            @Override
            public String getBody() {
                try {
                    MimeMessage message = new MimeMessage(getFromID(id));
                    MimeMessageParser parser = new MimeMessageParser(message).parse();

                    if (parser.getHtmlContent() != null) {
                        return parser.getHtmlContent();
                    } else {
                        return parser.getPlainContent();
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
                return null;
            }

            @Override
            public String getHeaders() {
                try {
                    MimeMessage message = new MimeMessage(getFromID(id));
                    Enumeration headersEnumeration = message.getAllHeaderLines();
                    StringBuilder headers = new StringBuilder();

                    while (headersEnumeration.hasMoreElements()) {
                        headers.append(headersEnumeration.nextElement().toString()).append("\n");
                    }

                    return headers.toString();
                } catch (MessagingException ex) {
                    log.error(ex.getMessage(), ex);
                }

                return null;
            }

            @Override
            public List<MessageAttachment> getAttachments() {
                try {
                    MimeMessage message = new MimeMessage(getFromID(id));
                    MimeMessageParser parser = new MimeMessageParser(message).parse();
                    List<MessageAttachment> attachments = new ArrayList<>();

                    parser.getAttachmentList().forEach(attachment -> {
                        try {
                            attachments.add(new MessageAttachment(attachment.getName(), attachment.getInputStream(), attachment));
                        } catch (IOException ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    });

                    return attachments;
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
                return null;
            }
        };
    }
}
