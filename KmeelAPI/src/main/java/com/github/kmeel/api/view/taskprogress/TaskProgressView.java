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

package com.github.kmeel.api.view.taskprogress;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Callback;

/**
 * The task progress view is used to visualize the progress of long running
 * tasks. These tasks are created via the {@link Task} class. This view
 * manages a list of such tasks and displays each one of them with their
 * name, progress, and update messages.<p>
 * An optional graphic factory can be set to place a graphic in each row.
 * This allows the user to more easily distinguish between different types
 * of tasks.
 * <p>
 * <p>
 * Taken from ControlsFX.
 */
public class TaskProgressView<T extends Task<?>> extends Control {

    /**
     * Constructs a new task progress view.
     */
    public TaskProgressView() {
        getStyleClass().add("taskprogressview.css");

        EventHandler<WorkerStateEvent> taskHandler = evt -> {
            if (evt.getEventType().equals(
                    WorkerStateEvent.WORKER_STATE_SUCCEEDED)
                    || evt.getEventType().equals(
                    WorkerStateEvent.WORKER_STATE_CANCELLED)
                    || evt.getEventType().equals(
                    WorkerStateEvent.WORKER_STATE_FAILED)) {
                getTasks().remove(evt.getSource());
            }
        };

        getTasks().addListener(new ListChangeListener<Task<?>>() {
            @Override
            public void onChanged(Change<? extends Task<?>> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (Task<?> task : c.getAddedSubList()) {
                            task.addEventHandler(WorkerStateEvent.ANY,
                                    taskHandler);
                        }
                    } else if (c.wasRemoved()) {
                        for (Task<?> task : c.getAddedSubList()) {
                            task.removeEventHandler(WorkerStateEvent.ANY,
                                    taskHandler);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TaskProgressViewSkin<>(this);
    }

    private final ObservableList<T> tasks = FXCollections.observableArrayList();

    /**
     * Returns the list of tasks currently monitored by this view.
     *
     * @return the monitored tasks
     */
    public final ObservableList<T> getTasks() {
        return tasks;
    }

    private ObjectProperty<Callback<T, Node>> graphicFactory;

    /**
     * Returns the property used to store an optional callback for creating
     * custom graphics for each task.
     *
     * @return the graphic factory property
     */
    public final ObjectProperty<Callback<T, Node>> graphicFactoryProperty() {
        if (graphicFactory == null) {
            graphicFactory = new SimpleObjectProperty<Callback<T, Node>>(
                    this, "graphicFactory");
        }

        return graphicFactory;
    }

    /**
     * Returns the value of {@link #graphicFactoryProperty()}.
     *
     * @return the optional graphic factory
     */
    public final Callback<T, Node> getGraphicFactory() {
        return graphicFactory == null ? null : graphicFactory.get();
    }

    /**
     * Sets the value of {@link #graphicFactoryProperty()}.
     *
     * @param factory an optional graphic factory
     */
    public final void setGraphicFactory(Callback<T, Node> factory) {
        graphicFactoryProperty().set(factory);
    }
}