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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import javax.mail.internet.MimeMessage;
import java.util.Enumeration;

/**
 * @author Marten4n6
 */
@Slf4j
public class EMLIndexer {

    private KmeelAPI kmeelAPI;

    public EMLIndexer(KmeelAPI kmeelAPI) {
        this.kmeelAPI = kmeelAPI;
    }

    /**
     * Indexes the specified MimeMessage
     */
    public void indexMessage(MimeMessage message, String path, String folderPath) {
        try {
            MimeMessageParser parser = new MimeMessageParser(message).parse();
            Document document = new Document();
            StringBuilder searchableText = new StringBuilder();
            document.add(new StringField("ID", Utils.getID(message).getId(), Field.Store.YES));
            searchableText.append(" ").append(Utils.getID(message));

            // The path to the EML file, used to get the MimeMessage object back
            document.add(new StringField("Path", path, Field.Store.YES));
            searchableText.append(" ").append(path);

            // Used to get all EMLs from a specific folder just by searching for the path
            document.add(new StringField("FolderPath", folderPath, Field.Store.YES));
            searchableText.append(" ").append(folderPath);

            if (parser.getSubject() != null) {
                document.add(new TextField("Subject", parser.getSubject(), Field.Store.YES));
                searchableText.append(" ").append(parser.getSubject());
            }
            if (parser.getPlainContent() != null) {
                document.add(new TextField("Body", parser.getPlainContent(), Field.Store.NO));
                searchableText.append(" ").append(parser.getPlainContent());
            }
            if (parser.getFrom() != null) {
                document.add(new TextField("From", parser.getFrom(), Field.Store.YES));
                searchableText.append(" ").append(parser.getFrom());
            }
            if (message.getAllHeaderLines() != null) {
                StringBuilder headers = new StringBuilder();
                Enumeration allHeaderLines = message.getAllHeaderLines();
                while (allHeaderLines.hasMoreElements()) {
                    headers.append(allHeaderLines.nextElement()).append("\n");
                }

                document.add(new TextField("Headers", headers.toString(), Field.Store.NO));
                searchableText.append(" ").append(headers.toString());
            }

            document.add(new TextField("Contents", searchableText.toString(), Field.Store.NO));
            kmeelAPI.indexer().getIndexWriter().addDocument(document);
        } catch (Exception ex) {
            log.warn(ex.getMessage() + ": " + path);
        }
    }
}
