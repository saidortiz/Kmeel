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

package com.github.kmeel.api.model;

import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.spi.listeners.BookmarkListener;
import com.google.gson.Gson;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;

/**
 * @author Marten4n6
 *         This class handles bookmarks
 */
@Slf4j
public class Bookmarks {

    private String caseName;
    private Database database;
    private Plugins plugins;

    public Bookmarks(String caseName, Database database, Plugins plugins) {
        this.caseName = caseName;
        this.database = database;
        this.plugins = plugins;
    }

    /**
     * @return A list of all bookmarks
     */
    public ArrayList<ID> get() {
        try {
            @Cleanup Connection connection = database.getDataSource().getConnection();
            @Cleanup PreparedStatement statement = connection.prepareStatement("SELECT * FROM Bookmarks WHERE CaseName=?");

            statement.setString(1, caseName);

            @Cleanup ResultSet resultSet = statement.executeQuery();
            ArrayList<ID> bookmarks = new ArrayList<>();

            while (resultSet.next()) {
                bookmarks.add(new ID(resultSet.getString("ID")));
            }
            return bookmarks;
        } catch (SQLException | NullPointerException ex) {
            return new ArrayList<>(0);
        }
    }

    /**
     * @param tagName The tag to get the bookmarks of.
     * @return A list of all bookmarks from the specified tag
     * @throws NullPointerException if the tagName is null.
     */
    public ArrayList<ID> getFromTag(@NonNull String tagName) {
        try {
            @Cleanup Connection connection = database.getDataSource().getConnection();
            @Cleanup PreparedStatement statement = connection.prepareStatement("SELECT * FROM Bookmarks WHERE CaseName=? AND TagName=?");

            statement.setString(1, caseName);
            statement.setString(2, tagName);

            @Cleanup ResultSet resultSet = statement.executeQuery();
            ArrayList<ID> bookmarks = new ArrayList<>();

            while (resultSet.next()) {
                bookmarks.add(new ID(resultSet.getString("ID")));
            }
            return bookmarks;
        } catch (SQLException | NullPointerException ex) {
            return new ArrayList<>(0);
        }
    }

    /**
     * Adds the ID to the list of bookmarks, calls the BookmarkListener when done.
     *
     * @param id             The ID to set as a bookmark.
     * @param optionalObject An optional object for plugins to use, stored in JSON.
     * @throws NullPointerException if the ID is null.
     * @see BookmarkListener
     */
    public void add(@NonNull ID id, Object optionalObject) {
        try {
            @Cleanup Connection connection = database.getDataSource().getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Bookmarks(CaseName, ID, Other) VALUES (?, ?, ?)");

            preparedStatement.setString(1, caseName);
            preparedStatement.setString(2, id.getId());
            if (optionalObject == null) {
                preparedStatement.setString(3, "");
            } else {
                preparedStatement.setString(3, new Gson().toJson(optionalObject));
            }
            preparedStatement.execute();

            plugins.getPluginManager().getExtensions(BookmarkListener.class).forEach(listener -> {
                listener.bookmarkAdded(id);
            });
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * Removes the bookmark, calls the BookmarkListener when done.
     *
     * @throws NullPointerException if the ID is null.
     * @see BookmarkListener
     */
    public void remove(@NonNull ID bookmarkID) {
        try {
            @Cleanup Connection connection = database.getDataSource().getConnection();
            @Cleanup PreparedStatement statement = connection.prepareStatement("DELETE FROM Bookmarks WHERE CaseName=? AND ID=?");

            statement.setString(1, caseName);
            statement.setString(2, bookmarkID.getId());

            statement.execute();

            plugins.getPluginManager().getExtensions(BookmarkListener.class).forEach(listener -> {
                listener.bookmarkRemoved(bookmarkID);
            });
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return The optional object from the bookmark.
     * @throws NullPointerException if the ID is null.
     */
    public Object getObject(@NonNull ID bookmarkID) {
        try {
            @Cleanup Connection connection = database.getDataSource().getConnection();
            @Cleanup PreparedStatement statement = connection.prepareStatement("SELECT * FROM Bookmarks WHERE CaseName=? AND ID=?");

            statement.setString(1, caseName);
            statement.setString(2, bookmarkID.getId());

            @Cleanup ResultSet resultSet = statement.executeQuery();

            return new Gson().fromJson(resultSet.getString("Object"), Object.class);
        } catch (SQLException ex) {
            return null;
        }
    }

    /**
     * @return True if the ID is a bookmark.
     * @throws NullPointerException if the ID is null.
     */
    public boolean isBookmark(@NonNull ID id) {
        return get().contains(id);
    }
}
