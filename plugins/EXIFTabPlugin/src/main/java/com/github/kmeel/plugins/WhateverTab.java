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
import com.github.kmeel.api.model.objects.MessageAttachment;
import com.github.kmeel.api.utils.OSUtils;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Marten4n6
 *         Couldn't name this class EXIFTab since I already used that name so fuck it
 */
@Slf4j
class WhateverTab {

    private static final File OUTPUT_FOLDER = new File(OSUtils.getTempPath() + "EXIF");

    private KmeelAPI kmeelAPI;
    private EXIFParser exifParser = new EXIFParser();
    private ComboBox<MessageAttachment> comboBox;

    WhateverTab(KmeelAPI kmeelAPI) {
        this.kmeelAPI = kmeelAPI;
        OUTPUT_FOLDER.mkdir();
    }

    Tab getTab() {
        Tab tab = new Tab(); // Has TabData as user data
        GridPane gridPane = new GridPane();

        // Tab
        tab.setId("EXIFTabPlugin");
        tab.setText("EXIF");
        tab.setClosable(false);

        // Combo Box
        comboBox = new ComboBox<>();

        comboBox.setVisibleRowCount(7);
        comboBox.setConverter(new StringConverter<MessageAttachment>() {
            @Override
            public String toString(MessageAttachment messageAttachment) {
                return messageAttachment.getAttachmentName();
            }

            @Override
            public MessageAttachment fromString(String s) {
                for (MessageAttachment attachment : comboBox.getItems()) {
                    if (attachment.getAttachmentName().equals(s)) {
                        return attachment;
                    }
                }
                return null;
            }
        });
        comboBox.setPromptText("Select Attachment...");

        comboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(comboBox, Priority.ALWAYS);

        // Text Field
        TextArea textArea = new TextArea();

        textArea.setEditable(false);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setVgrow(textArea, Priority.ALWAYS);

        // Add
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(5, 5, 5, 5));
        gridPane.add(comboBox, 0, 0);
        gridPane.add(textArea, 0, 1);

        tab.setContent(gridPane);

        TabData tabData = new TabData();
        tabData.setUpdateListener((id) -> {
            // Called when a (new) message is clicked
            try {
                FileUtils.cleanDirectory(OUTPUT_FOLDER);
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
            comboBox.getItems().clear();
            textArea.clear();

            updateEXIF(id);
        });

        tab.setUserData(tabData);

        // Listeners
        tab.selectedProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) -> {
            if (newValue && comboBox.getItems().isEmpty())
                updateEXIF(((TabData) tab.getUserData()).getSelectedID()); //Tab is clicked
        });
        comboBox.setOnAction((event) -> {
            if (comboBox.getSelectionModel().getSelectedItem() != null) {
                MessageAttachment selectedAttachment = comboBox.getSelectionModel().getSelectedItem();
                File outputFile = new File(OSUtils.getTempPath() + "EXIF" + File.separator + selectedAttachment.getAttachmentName());

                if (!outputFile.exists()) {
                    try {
                        FileUtils.copyInputStreamToFile(selectedAttachment.getInputStream(), outputFile);
                        outputFile.deleteOnExit();
                    } catch (IOException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }

                textArea.clear();
                exifParser.parse(outputFile).entrySet().forEach(entry -> textArea.appendText(entry.getKey().getName() + " : " + entry.getValue() + "\n"));

                if (textArea.getText().trim().isEmpty()) textArea.setText("No EXIF data found.");
            }
        });
        return tab;
    }

    /**
     * Updates the EXIF tab
     */
    private void updateEXIF(ID id) {
        List<MessageAttachment> attachmentList = kmeelAPI.messages().getMessage(id).getAttachments();

        if (attachmentList != null) {
            for (MessageAttachment attachment : attachmentList) {
                comboBox.getItems().add(attachment);
            }
        }
    }
}
