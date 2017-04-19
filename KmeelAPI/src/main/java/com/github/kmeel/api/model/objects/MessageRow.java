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

package com.github.kmeel.api.model.objects;

import com.github.kmeel.api.KmeelAPI;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import lombok.Data;

import java.util.Date;

/**
 * @author Marten4n6
 */
@Data
public class MessageRow {

    private final ID id;
    private final String from, to, size, subject, cc;
    private final ObjectProperty<Date> received;
    private final StringProperty tag;
    private final BooleanProperty bookmarked;

    public MessageRow(KmeelAPI kmeelAPI, ID id, String from, String to, String size, String subject, String cc, ObjectProperty<Date> received, StringProperty tag, BooleanProperty bookmarked) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.size = size;
        this.subject = subject;
        this.cc = cc;
        this.received = received;
        this.tag = tag;
        this.bookmarked = bookmarked;

        bookmarked.addListener((ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                kmeelAPI.bookmarks().add(getId(), null);
            } else {
                kmeelAPI.bookmarks().remove(getId());
            }
        });
        tag.addListener((ObservableValue<? extends String> observableValue, String oldValue, String newValue) -> {
            kmeelAPI.tags().set(id, newValue);
        });
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked.set(bookmarked);
    }

    public void setTag(String tag) {
        this.tag.set(tag);
    }
}
