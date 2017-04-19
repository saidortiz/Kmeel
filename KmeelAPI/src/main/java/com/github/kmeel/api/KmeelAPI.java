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

package com.github.kmeel.api;

import com.github.kmeel.api.model.*;
import com.github.kmeel.api.model.objects.Case;

/**
 * @author Marten4n6
 */
public class KmeelAPI {

    private Case caseObject;
    private Plugins plugins;
    private Database database;
    private Bookmarks bookmarks;
    private Indexer indexer;
    private Searcher searcher;
    private Messages messages;
    private Settings settings;
    private Tags tags;

    public KmeelAPI(Case caseObject) {
        this.caseObject = caseObject;
        plugins = new Plugins();
        messages = new Messages(plugins());
    }

    public void setCase(Case caseObject) {
        if (this.caseObject != null) throw new IllegalArgumentException("Case object already set.");
        this.caseObject = caseObject;
    }

    public Plugins plugins() {
        return plugins;
    }

    public Database database() {
        if (database == null && caseObject != null) {
            database = new Database(caseObject.getName());
        }
        return database;
    }

    public Bookmarks bookmarks() {
        if (bookmarks == null && caseObject != null) {
            bookmarks = new Bookmarks(caseObject.getName(), database(), plugins());
        }
        return bookmarks;
    }

    public Indexer indexer() {
        if (indexer == null && caseObject != null) {
            indexer = new Indexer(caseObject.getName());
        }
        return indexer;
    }

    public Searcher searcher() {
        if (searcher == null && caseObject != null) {
            searcher = new Searcher(caseObject.getName());
        }
        return searcher;
    }

    public Settings settings() {
        if (settings == null && caseObject != null) {
            settings = new Settings(caseObject.getName());
        }
        return settings;
    }

    public Messages messages() {
        return messages;
    }

    public Tags tags() {
        if (tags == null && caseObject != null) {
            tags = new Tags(caseObject.getName(), database(), plugins());
        }
        return tags;
    }
}
