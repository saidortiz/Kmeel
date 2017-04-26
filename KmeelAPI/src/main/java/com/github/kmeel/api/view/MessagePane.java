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

package com.github.kmeel.api.view;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.custom.DefaultContextMenu;
import com.github.kmeel.api.model.objects.AttachmentRow;
import com.github.kmeel.api.model.objects.MessageRow;
import com.github.kmeel.api.spi.MessageTab;
import com.github.kmeel.api.spi.listeners.MessageListener;
import com.github.kmeel.api.view.objects.Footer;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Marten4n6
 *         Simple panel for the message table and tabbed pane
 */
public class MessagePane {

    private KmeelAPI kmeelAPI;

    private @Getter BorderPane pane;

    private @Getter TableView<MessageRow> table;
    private @Getter TableView<AttachmentRow> attachmentsTable;
    private @Getter TabPane tabPane;
    private WebView messageArea;
    private WebView headersArea;

    private Label footerLabel;
    private @Getter Footer footer;

    private Tab tabAttachments;

    public MessagePane(KmeelAPI kmeelAPI) {
        this.kmeelAPI = kmeelAPI;
        pane = new BorderPane();
        SplitPane splitPane = new SplitPane();
        footerLabel = new Label();
        footer = new Footer("", kmeelAPI, getTable());

        // Split Pane
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setMaxWidth(Double.MAX_VALUE);
        splitPane.setMaxHeight(Double.MAX_VALUE);

        // Table
        table = new TableView<>();

        TableColumn<MessageRow, String> columnFrom = new TableColumn<>("From");
        TableColumn<MessageRow, String> columnTo = new TableColumn<>("To");
        TableColumn<MessageRow, String> columnSubject = new TableColumn<>("Subject");
        TableColumn<MessageRow, Date> columnReceived = new TableColumn<>("Received");
        TableColumn<MessageRow, String> columnSize = new TableColumn<>("Size");
        TableColumn<MessageRow, String> columnCC = new TableColumn<>("CC");
        TableColumn<MessageRow, String> columnTag = new TableColumn<>("Tag");
        TableColumn<MessageRow, Boolean> columnBookmark = new TableColumn<>("Bookmark");

        // Cell value factories
        columnFrom.setCellValueFactory(new PropertyValueFactory<>("from"));
        columnTo.setCellValueFactory(new PropertyValueFactory<>("to"));
        columnSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        columnReceived.setCellValueFactory(param -> param.getValue().getReceived());
        columnSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        columnCC.setCellValueFactory(new PropertyValueFactory<>("cc"));
        columnTag.setCellValueFactory(param -> param.getValue().getTag());
        columnBookmark.setCellValueFactory(param -> param.getValue().getBookmarked());

        // Cell factories
        columnReceived.setCellFactory(column -> new TableCell<MessageRow, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(kmeelAPI.settings().get("DateFormat"));

                    setText(DATE_FORMAT.format(item));
                }
            }
        });
        columnReceived.setComparator((Date date1, Date date2) -> {
            // Sort date from new to old
            if (date1 == null && date2 == null) {
                return 0;
            } else if (date1 == null) {
                return 1;
            } else if (date2 == null) {
                return -1;
            }
            return date2.compareTo(date1);
        });

        columnBookmark.setCellFactory(CheckBoxTableCell.forTableColumn(columnBookmark));

        table.setContextMenu(new DefaultContextMenu(this, kmeelAPI));
        table.setPlaceholder(new Label(""));
        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(columnFrom, columnTo, columnSubject, columnReceived, columnSize, columnCC, columnTag, columnBookmark);

        // Tabbed Pane
        tabPane = new TabPane();

        // Message Tab
        Tab tabMessage = new Tab();
        messageArea = new WebView();
        GridPane gridPaneMessage = new GridPane();

        tabMessage.setClosable(false);
        tabMessage.setText("Message");

        GridPane.setHgrow(messageArea, Priority.ALWAYS);
        GridPane.setVgrow(messageArea, Priority.ALWAYS);
        messageArea.setMaxHeight(Double.MAX_VALUE);
        messageArea.setMaxWidth(Double.MAX_VALUE);

        gridPaneMessage.add(messageArea, 0, 0);
        tabMessage.setContent(gridPaneMessage);

        messageArea.getEngine().getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State t1) -> {
            if (!messageArea.getEngine().getLocation().isEmpty()) {
                Platform.runLater(() -> messageArea.getEngine().getLoadWorker().cancel());
            }
        });

        // Headers Tab
        Tab tabHeaders = new Tab();
        headersArea = new WebView();
        GridPane gridPaneHeaders = new GridPane();

        tabHeaders.setClosable(false);
        tabHeaders.setText("Headers");

        GridPane.setHgrow(headersArea, Priority.ALWAYS);
        GridPane.setVgrow(headersArea, Priority.ALWAYS);
        headersArea.setMaxHeight(Double.MAX_VALUE);
        headersArea.setMaxWidth(Double.MAX_VALUE);

        gridPaneHeaders.add(headersArea, 0, 0);
        tabHeaders.setContent(gridPaneHeaders);

        // Attachments Tab
        tabAttachments = new Tab();

        tabAttachments.setClosable(false);
        tabAttachments.setText("Attachments (0)");

        attachmentsTable = new TableView<>();
        TableColumn<AttachmentRow, String> columnAttachmentName = new TableColumn<>("Name");
        TableColumn<AttachmentRow, String> columnAttachmentContentType = new TableColumn<>("Content Type");
        TableColumn<AttachmentRow, String> columnAttachmentSize = new TableColumn<>("Size");
        TableColumn<AttachmentRow, String> columnAttachmentCreationTime = new TableColumn<>("Creation Time");
        TableColumn<AttachmentRow, String> columnAttachmentModificationTime = new TableColumn<>("Modification Time");

        columnAttachmentName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAttachmentName()));
        columnAttachmentContentType.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getContentType()));
        columnAttachmentSize.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSize()));
        columnAttachmentCreationTime.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCreationTime()));
        columnAttachmentModificationTime.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getModificationTime()));

        attachmentsTable.setPlaceholder(new Label(""));
        attachmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        attachmentsTable.getColumns().addAll(columnAttachmentName, columnAttachmentContentType, columnAttachmentSize, columnAttachmentCreationTime, columnAttachmentModificationTime);

        attachmentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            Platform.runLater(() -> {
                if (newSelection != null) {
                    kmeelAPI.plugins().getPluginManager().getExtensions(MessageListener.class).forEach(listener -> {
                        listener.attachmentSelected(this);
                    });
                }
            });
        });

        GridPane.setHgrow(attachmentsTable, Priority.ALWAYS);
        GridPane.setVgrow(attachmentsTable, Priority.ALWAYS);
        attachmentsTable.setMaxHeight(Double.MAX_VALUE);
        attachmentsTable.setMaxWidth(Double.MAX_VALUE);

        tabAttachments.setContent(attachmentsTable);

        // Footer
        footerLabel.setPadding(new Insets(0, 0, 0, 2));

        // Add
        splitPane.getItems().addAll(table, tabPane);
        tabPane.getTabs().addAll(tabMessage, tabHeaders, tabAttachments);

        kmeelAPI.plugins().getPluginManager().getExtensions(MessageTab.class).forEach(tab -> {
            tabPane.getTabs().add(tab.getMessageTab(kmeelAPI, this));
        });

        pane.setCenter(splitPane);
        pane.setBottom(footerLabel);

        // Listeners
        table.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends MessageRow> observableValue, MessageRow oldSelection, MessageRow newSelection) -> {
            Platform.runLater(() -> {
                if (observableValue.getValue() == null) return;

                setBodyText("");
                setHeadersText("");
                setAttachmentTabAmount(0);
                attachmentsTable.getItems().clear();

                kmeelAPI.plugins().getPluginManager().getExtensions(MessageListener.class).forEach(message -> {
                    message.messageSelected(this, observableValue.getValue().getId());
                });
            });
        });
    }

    public void setFooter(Footer footer) {
        this.footer = footer;

        if (footer != null && !footer.toString().isEmpty()) {
            footerLabel.setText(getFooter().toString());
        }
    }

    public void updateFooter() {
        if (getFooter() != null) {
            setFooter(getFooter());
        }
    }

    public void setAttachmentTabAmount(int attachmentAmount) {
        Platform.runLater(() -> tabAttachments.setText("Attachments (" + attachmentAmount + ")"));
    }

    public void setBodyText(String text) {
        Platform.runLater(() -> messageArea.getEngine().loadContent(text, "text/html"));
    }

    public void setHeadersText(String text) {
        Platform.runLater(() -> headersArea.getEngine().loadContent(text, "text/html"));
    }
}
