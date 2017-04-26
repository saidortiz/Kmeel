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
import com.github.kmeel.api.model.objects.MessageRow;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.plugins.model.PSTModel;
import com.github.kmeel.plugins.model.object.TreeObject;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBoxTreeItem;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marten4n6
 */
@Slf4j
public class TreeSelectionHandler {

    private static final TreeSelectionHandler INSTANCE = new TreeSelectionHandler();

    private final ExecutorService updateQueue = Executors.newSingleThreadExecutor();

    private TreeSelectionHandler() {}

    /**
     * @return The TreeSelectionHandler instance
     */
    public static TreeSelectionHandler getInstance() {
        return INSTANCE;
    }

    public void handleUpdate(KmeelAPI kmeelAPI, CheckBoxTreeItem<Object> treeItem, boolean removed, MessagePane messagePane) {
        updateQueue.submit(() -> {
            Platform.runLater(() -> messagePane.getTable().setCursor(Cursor.WAIT));
            List<MessageRow> rows = new ArrayList<>();

            // Get all messages from the selected tree item then add them to the MessagePane
            if (treeItem.getValue() != null && treeItem.getValue() instanceof TreeObject) {
                Set<ID> messagesFromNode = PSTModel.getInstance().getFromNode(kmeelAPI, treeItem);

                if (messagesFromNode != null) {
                    for (ID id : messagesFromNode) {

                        if (removed) {
                            Platform.runLater(() -> {
                                messagePane.getTable().getItems().removeIf(item -> item.getId().equals(id));
                            });
                        } else {
                            rows.add(PSTModel.getInstance().getMessage(kmeelAPI, id).getRow());
                        }
                    }

                    if (!removed) {
                        Platform.runLater(() -> {
                            messagePane.getTable().getItems().addAll(rows);
                        });
                    }
                }
            }

            Platform.runLater(() -> messagePane.getTable().setCursor(Cursor.DEFAULT));
            return null;
        });
    }
}
