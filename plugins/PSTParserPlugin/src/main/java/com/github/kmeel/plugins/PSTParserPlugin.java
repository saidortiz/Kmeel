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
import com.github.kmeel.api.custom.DefaultContextMenuItems;
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.spi.Message;
import com.github.kmeel.api.spi.Parser;
import com.github.kmeel.api.spi.PluginableFileTree;
import com.github.kmeel.api.spi.listeners.MessageListener;
import com.github.kmeel.api.view.LoadingView;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.plugins.custom.PSTMenuItems;
import com.github.kmeel.plugins.handlers.AttachmentClickedHandler;
import com.github.kmeel.plugins.handlers.MessageClickedHandler;
import com.github.kmeel.plugins.handlers.TreeSelectionHandler;
import com.github.kmeel.plugins.model.PSTIndexer;
import com.github.kmeel.plugins.model.PSTModel;
import com.github.kmeel.plugins.model.object.PSTFileID;
import com.github.kmeel.plugins.model.object.TreeObject;
import com.github.kmeel.plugins.view.LoadingTask;
import com.pff.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import lombok.extern.slf4j.Slf4j;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marten4n6
 *         This plugin parses PST files using https://github.com/rjohnsondev/java-libpst
 */
@Slf4j
public class PSTParserPlugin extends Plugin {

    public PSTParserPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class PSTParser implements Parser, MessageListener, DefaultContextMenuItems, PluginableFileTree {

        private static KmeelAPI kmeelAPI;
        private static PSTIndexer pstIndexer;

        private static boolean isParsed;
        private static boolean isTreePluginEnabled;

        private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

        private static LoadingView loadingView;

        @Override
        public String getName() {
            return "PSTParser";
        }

        @Override
        public Set<String> getAcceptedExtensions() {
            Set<String> extensions = new HashSet<>();
            extensions.add("pst");
            extensions.add("ost"); // Untested

            return extensions;
        }

        @Override
        public void setup(KmeelAPI api, LoadingView loadingThing) {
            kmeelAPI = api;
            pstIndexer = new PSTIndexer(kmeelAPI);

            isParsed = Boolean.parseBoolean(kmeelAPI.settings().get(getName() + "-Parsed"));
            isTreePluginEnabled = kmeelAPI.plugins().getPluginManager().getExtensions(PluginableFileTree.class).size() != 0;

            loadingView = loadingThing;
        }

        @Override
        public void parseFile(File file, int totalFiles, ActionListener finishedListener) {
            Task<CheckBoxTreeItem<TreeObject>> task = new Task<CheckBoxTreeItem<TreeObject>>() {
                private CheckBoxTreeItem<TreeObject> rootTreeItem;

                private LoadingTask loadingTask;
                private AtomicInteger messageAmount = new AtomicInteger(0);
                private AtomicInteger finishedAmount = new AtomicInteger(0);

                @Override
                protected CheckBoxTreeItem<TreeObject> call() throws Exception {
                    try {
                        PSTFile pstFile = new PSTFile(file);

                        if (pstFile.getRootFolder() != null) {
                            if (isTreePluginEnabled) {
                                rootTreeItem = new CheckBoxTreeItem<>(new TreeObject(file.getName(), null));
                            }

                            if (!isParsed) {
                                loadingTask = new LoadingTask();

                                Platform.runLater(() -> {
                                    loadingTask.updateMessage("Getting message amount...");
                                    loadingTask.updateProgress(0, totalFiles);
                                    loadingView.addTask(loadingTask);
                                });

                                calculateMessageAmount(pstFile.getRootFolder()); //Update the messageAmount
                                loadingTask.updateMessage("Indexing: " + file.getName());
                            }

                            PSTModel.getInstance().getFileFromHash().put(new PSTFileID(pstFile).getId(), pstFile);

                            for (PSTFolder subFolder : pstFile.getRootFolder().getSubFolders()) {
                                extractSubFolder(pstFile, subFolder, file.getName() + "/", rootTreeItem);
                            }
                        }
                    } catch (IOException | PSTException ex) {
                        log.error(ex.getMessage(), ex);
                    }

                    if (loadingTask != null) loadingTask.done();
                    return rootTreeItem;
                }

                private void calculateMessageAmount(PSTFolder folder) {
                    try {
                        if (folder.getContentCount() != 0 && folder.getNodeType() != 3) {
                            messageAmount.getAndAdd(folder.getContentCount());
                        }

                        if (folder.getNodeType() != 3) {
                            for (PSTFolder pstFolder : folder.getSubFolders()) {
                                calculateMessageAmount(pstFolder);
                            }
                        }
                    } catch (IOException | PSTException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }

                private void extractSubFolder(PSTFile pstFile, PSTFolder subFolder, String folderPath, CheckBoxTreeItem<TreeObject> treeItem) {
                    try {
                        String newFolderPath = folderPath + subFolder.getDisplayName();

                        CheckBoxTreeItem<TreeObject> subFolderTreeItem = null;

                        if (isTreePluginEnabled) {
                            String title = subFolder.getContentCount() != 0 ? subFolder.getDisplayName() + " (" + subFolder.getContentCount() + ")" : subFolder.getDisplayName();
                            subFolderTreeItem = new CheckBoxTreeItem<>(new TreeObject(title, subFolder));

                            treeItem.getChildren().add(subFolderTreeItem);
                        }

                        if (!isParsed) {
                            if (subFolder.getContentCount() > 0) {
                                for (Integer descriptorID : subFolder.getChildDescriptorNodes()) {
                                    try {
                                        // Do stuff with the PSTObject here...
                                        PSTObject pstObject = PSTObject.detectAndLoadPSTObject(pstFile, descriptorID);

                                        if (pstObject instanceof PSTAppointment) {
                                            PSTAppointment appointment = (PSTAppointment) pstObject;

                                            pstIndexer.index(pstFile, subFolder, newFolderPath, appointment);
                                        } else if (pstObject instanceof PSTContact) {
                                            PSTContact contact = (PSTContact) pstObject;

                                            pstIndexer.index(pstFile, subFolder, newFolderPath, contact);
                                        } else {
                                            PSTMessage message = (PSTMessage) pstObject;

                                            pstIndexer.index(pstFile, subFolder, newFolderPath, message);
                                        }

                                        finishedAmount.incrementAndGet();
                                        loadingTask.updateProgress(finishedAmount.get(), messageAmount.get());
                                    } catch (PSTException ex) {
                                        log.debug(ex.getMessage());
                                    }
                                }
                            }
                        }

                        // Loop through sub-folders of this sub-folder
                        if (subFolder.getNodeType() != 3) {
                            for (PSTFolder subFolderInfo : subFolder.getSubFolders()) {
                                extractSubFolder(pstFile, subFolderInfo, newFolderPath + "/", subFolderTreeItem);
                            }
                        }
                    } catch (IOException | PSTException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            };

            task.setOnSucceeded((event) -> {
                if (isTreePluginEnabled) {
                    CheckBoxTreeItem<TreeObject> rootTreeItem = (CheckBoxTreeItem<TreeObject>)event.getSource().getValue();

                    kmeelAPI.plugins().getPluginManager().getExtensions(PluginableFileTree.class).forEach(pluginableFileTree -> {
                        pluginableFileTree.addTreeItem(rootTreeItem);
                    });
                }

                kmeelAPI.settings().set(getName() + "-Parsed", "true");
                if (finishedListener != null) finishedListener.actionPerformed(null);
            });

            executorService.submit(task);
        }

        @Override
        public Message getMessage(ID id) {
            return PSTModel.getInstance().getMessage(kmeelAPI, id);
        }

        @Override
        public void messageSelected(MessagePane messagePane, ID id) {
            new MessageClickedHandler(kmeelAPI).handle(messagePane, id);
        }

        @Override
        public void attachmentSelected(MessagePane messagePane) {
            new AttachmentClickedHandler(kmeelAPI).handle(messagePane);
        }

        @Override
        public void addMenuItems(MessagePane messagePane, ContextMenu contextMenu) {
            new PSTMenuItems(kmeelAPI, messagePane, contextMenu).add();
        }

        @Override
        public void addTreeItem(CheckBoxTreeItem treeItem) {}

        @Override
        public void treeSelectionUpdate(CheckBoxTreeItem<Object> updatedItem, boolean removed, MessagePane messagePane) {
            TreeSelectionHandler.getInstance().handleUpdate(kmeelAPI, updatedItem, removed, messagePane);
        }
    }
}
