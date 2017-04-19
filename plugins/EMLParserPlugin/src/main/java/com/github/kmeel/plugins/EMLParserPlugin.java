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
import com.github.kmeel.api.model.objects.AttachmentRow;
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.model.objects.MessageAttachment;
import com.github.kmeel.api.spi.Message;
import com.github.kmeel.api.spi.Parser;
import com.github.kmeel.api.spi.PluginableFileTree;
import com.github.kmeel.api.spi.listeners.MessageListener;
import com.github.kmeel.api.view.LoadingView;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.plugins.handlers.TreeSelectionHandler;
import com.github.kmeel.plugins.model.EMLIndexer;
import com.github.kmeel.plugins.model.EMLModel;
import com.github.kmeel.plugins.view.LoadingTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marten4n6
 */
@Slf4j
public class EMLParserPlugin extends Plugin {

    public EMLParserPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class EMLParser implements Parser, MessageListener, PluginableFileTree {

        private static KmeelAPI kmeelAPI;
        private static EMLModel emlModel;
        private static EMLIndexer emlIndexer;

        private static boolean isParsed;

        // Loading
        private static LoadingView loadingView;
        private static LoadingTask loadingTask;
        private static AtomicInteger loadingProgress;

        // Tree
        private static boolean isTreePluginEnabled;
        private static CheckBoxTreeItem<TreeObject> rootTreeItem;
        private static HashMap<String, TreeItem<TreeObject>> rootTreeChilden;

        @Override
        public String getName() {
            return "EMLParser";
        }

        @Override
        public Set<String> getAcceptedExtensions() {
            Set<String> extensions = new HashSet<>();
            extensions.add("eml");

            return extensions;
        }

        @Override
        public void setup(KmeelAPI api, LoadingView loadingThing) {
            kmeelAPI = api;
            emlModel = new EMLModel(api);
            emlIndexer = new EMLIndexer(api);

            isParsed = Boolean.parseBoolean(kmeelAPI.settings().get(getName() + "-IsParsed"));

            loadingView = loadingThing;
            loadingTask = null;
            loadingProgress = new AtomicInteger(0);
        }

        @Override
        public void parseFile(File file, int totalFiles, ActionListener finishedListener) {
            if (!isParsed) {
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if (loadingTask == null) {
                            loadingTask = new LoadingTask();
                            isTreePluginEnabled = kmeelAPI.plugins().getPluginManager().getExtensions(PluginableFileTree.class).size() != 0;

                            Platform.runLater(() -> loadingView.addTask(loadingTask));

                            if (isTreePluginEnabled) {
                                rootTreeItem = new CheckBoxTreeItem<>(new TreeObject("EMLs", null));
                                rootTreeChilden = new HashMap<>();
                            }
                        }

                        loadingTask.updateMessage("Parsing: " + file.getParent());
                        loadingTask.updateProgress(loadingProgress.get(), totalFiles);

                        @Cleanup InputStream messageInputStream = Files.newInputStream(Paths.get(file.getPath()));
                        MimeMessage message = new MimeMessage(null, messageInputStream);

                        emlIndexer.indexMessage(message, file.getPath(), file.getParent());

                        if (isTreePluginEnabled) {
                            if (rootTreeChilden.get(file.getParent()) == null) {
                                CheckBoxTreeItem<TreeObject> folderNameItem = new CheckBoxTreeItem<>(new TreeObject(new File(file.getParent()).getName(), file.getParent()));

                                rootTreeItem.getChildren().add(folderNameItem);
                                rootTreeChilden.put(file.getParent(), folderNameItem);
                            }
                        }
                        return null;
                    }
                };

                task.setOnSucceeded((event) -> {
                    if (finishedListener != null) finishedListener.actionPerformed(null);
                    loadingProgress.incrementAndGet();

                    if (loadingTask != null && loadingProgress.get() == totalFiles) {
                        kmeelAPI.settings().set(getName() + "-Parsed", "true");

                        if (isTreePluginEnabled) {
                            kmeelAPI.plugins().getPluginManager().getExtensions(PluginableFileTree.class).forEach(pluginableFileTree -> {
                                pluginableFileTree.addTreeItem(rootTreeItem);
                            });
                        }

                        loadingTask.done();
                    }
                });

                new Thread(task).start();
            }
        }

        @Override
        public Message getMessage(ID id) {
            return emlModel.getMessage(id);
        }

        @Override
        public void messageSelected(MessagePane messagePane, ID id) {
            Message message = getMessage(id);

            if (message != null) {
                MimeMessage mimeMessage = emlModel.getFromID(id);
                List<MessageAttachment> attachments = message.getAttachments();

                messagePane.setBodyText(message.getBody().replaceAll("\n", "<br/>"));
                messagePane.setHeadersText(message.getHeaders().replaceAll("\n", "<br/>"));
                messagePane.setAttachmentTabAmount(attachments.size());

                attachments.forEach(attachment -> {
                    try {
                        DataSource dataSource = (DataSource) attachment.getOptionalObject();

                        messagePane.getAttachmentsTable().getItems().add(new AttachmentRow(
                                attachment.getAttachmentName(),
                                dataSource.getContentType(),
                                Utils.humanReadableByteCount(mimeMessage.getSize()),
                                id.getId()));
                    } catch (MessagingException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });
            }
        }

        @Override
        public void attachmentSelected(MessagePane messagePane) {
        }

        @Override
        public void addTreeItem(CheckBoxTreeItem treeItem) {
        }

        @Override
        public void treeSelectionUpdate(CheckBoxTreeItem<Object> updatedItem, boolean removed, MessagePane messagePane) {
            TreeSelectionHandler.getInstance().handle(kmeelAPI, updatedItem, removed, messagePane);
        }
    }
}
