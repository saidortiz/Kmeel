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
import com.github.kmeel.plugins.TreeObject;
import com.github.kmeel.plugins.model.EMLModel;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBoxTreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marten4n6
 */
public class TreeSelectionHandler {

    private static final TreeSelectionHandler INSTANCE = new TreeSelectionHandler();

    private final ExecutorService updateQueue = Executors.newSingleThreadExecutor();

    private TreeSelectionHandler() {}

    public static TreeSelectionHandler getInstance() {
        return INSTANCE;
    }

    public void handle(KmeelAPI kmeelAPI, CheckBoxTreeItem<Object> treeItem, boolean removed, MessagePane messagePane) {
        EMLModel emlModel = new EMLModel(kmeelAPI);

        updateQueue.submit(() -> {
            Platform.runLater(() -> messagePane.getTable().setCursor(Cursor.WAIT));
            List<MessageRow> rows = new ArrayList<>();

            if (treeItem.getValue() != null && treeItem.getValue() instanceof TreeObject) {
                TreeObject treeObject = (TreeObject) treeItem.getValue();
                Set<String> messagesFromNode = emlModel.getFromFolder(treeObject.getFolderPath());

                if (messagesFromNode != null) {
                    for (String id : messagesFromNode) {
                        if (removed) {
                            Platform.runLater(() -> {
                                messagePane.getTable().getItems().removeIf(item -> item.getId().equals(new ID(id)));
                            });
                        } else {
                            rows.add(emlModel.getMessage(new ID(id)).getRow());
                        }
                    }

                    if (!removed) {
                        Platform.runLater(() -> messagePane.getTable().getItems().addAll(rows));
                    }
                }
            }

            Platform.runLater(() -> messagePane.getTable().setCursor(Cursor.DEFAULT));
        });
    }
}
