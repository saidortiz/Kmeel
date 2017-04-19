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

package com.github.kmeel.api.model;

import com.github.kmeel.api.utils.OSUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marten4n6
 *         This class handles searching for indexed messages
 */
@Slf4j
public class Searcher {

    private static IndexSearcher searcher;
    private static QueryParser parser;

    public Searcher(String caseName) {
        try {
            searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(
                       new File(OSUtils.getIndexPath(caseName)).toPath())));
            parser = new QueryParser("Contents", new StandardAnalyzer());
            parser.setLowercaseExpandedTerms(true);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public TopDocs search(String queryString, int maxHits) {
        try {
            return searcher.search(parser.parse(queryString), maxHits);
        } catch (ParseException | NullPointerException | IOException ex) {
            log.error(ex.getMessage());

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
                Label message = new Label(ex.getMessage());
                message.setWrapText(true);

                alert.getDialogPane().setContent(message);
                alert.showAndWait();
            });
            return null;
        } catch (IllegalStateException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public TopDocs search(Query query, int maxHits) {
        try {
            return searcher.search(query, maxHits);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public Document getDocument(int id) throws IOException {
        return searcher.doc(id);
    }

    /**
     * @return A set of all indexed fields.
     */
    public Set<String> getIndexedFields() {
        Set<String> fields = new HashSet<>();

        for (LeafReaderContext leafReaderContext : searcher.getIndexReader().leaves()) {
            for (FieldInfo fieldInfo : leafReaderContext.reader().getFieldInfos()) {
                fields.add(fieldInfo.name);
            }
        }
        return fields;
    }
}