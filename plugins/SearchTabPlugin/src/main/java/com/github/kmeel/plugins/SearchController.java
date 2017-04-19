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

package com.github.kmeel.plugins;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.model.objects.MessageRow;
import com.github.kmeel.api.view.MessagePane;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marten4n6
 *         This class updates the message table with the search results
 */
@Slf4j
class SearchController {

    private KmeelAPI kmeelAPI;

    private MessagePane messagePane;
    private Task<List<MessageRow>> worker;

    private String query;

    SearchController(KmeelAPI kmeelAPI, MessagePane messagePane) {
        this.kmeelAPI = kmeelAPI;
        this.messagePane = messagePane;
    }

    void search(String query) {
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }

        this.query = query;

        worker = new Task<List<MessageRow>>() {
            @Override
            protected List<MessageRow> call() throws Exception {
                try {
                    Platform.runLater(() -> {
                        messagePane.getTable().setCursor(Cursor.WAIT);
                        messagePane.getTable().getItems().clear();
                        messagePane.setFooter(null);
                    });

                    int searchLimit = Integer.parseInt(kmeelAPI.settings().get("SearchLimit"));
                    if (searchLimit == 0) searchLimit = Integer.MAX_VALUE;

                    TopDocs topDocs = kmeelAPI.searcher().search(query, searchLimit);

                    if (topDocs == null) {
                        new Alert(Alert.AlertType.ERROR, "Invalid query.", ButtonType.CLOSE).showAndWait();
                        return null;
                    }

                    ScoreDoc[] hits = topDocs.scoreDocs;
                    List<MessageRow> rows = new ArrayList<>();

                    for (int i = 0; i < hits.length; i++) {
                        if (isCancelled()) break;

                        Document document = kmeelAPI.searcher().getDocument(hits[i].doc);

                        rows.add(kmeelAPI.messages().getMessage(new ID(document.get("ID"))).getRow());
                    }

                    return rows;
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                }
                return null;
            }
        };

        worker.setOnSucceeded((event) -> {
            List<MessageRow> rows = (List<MessageRow>)event.getSource().getValue();

            Platform.runLater(() -> {
                messagePane.getTable().getItems().addAll(rows);
                messagePane.getTable().setCursor(Cursor.DEFAULT);
            });
        });

        new Thread(worker).start();
    }

    void refresh() {
        if (query != null) search(query);
    }
}