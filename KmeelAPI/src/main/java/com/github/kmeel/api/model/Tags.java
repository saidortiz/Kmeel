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

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.spi.listeners.TagListener;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marten4n6
 *         This class handles tags
 */
@Slf4j
public class Tags {

    private String caseName;
    private Database database;
    private Plugins plugins;

    public Tags(String caseName, Database database, Plugins plugins) {
        this.caseName = caseName;
        this.database = database;
        this.plugins = plugins;
    }

    /**
     * Sets the tag of the ID, calls the TagListener when done.
     *
     * @param id      The ID whose tag to set.
     * @param tagName The name of the tag to set.
     * @throws NullPointerException if the ID or tagName is null.
     * @see TagListener
     */
    public void set(@NonNull ID id, @NonNull String tagName) {
        try {
            if (!tagName.trim().isEmpty()) {
                @Cleanup Connection connection = database.getDataSource().getConnection();
                @Cleanup PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO Tags(CaseName, ID, TagName) VALUES (?,?,?)");

                statement.setString(1, caseName);
                statement.setString(2, id.getId());
                statement.setString(3, tagName);

                statement.execute();
            } else {
                removeTag(id);
            }

            plugins.getPluginManager().getExtensions(TagListener.class).forEach(listener -> {
                listener.tagChanged(id, tagName);
            });
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * Removes the tag from the ID, calls the TagListener when done.
     *
     * @param id The ID whose tag to remove.
     * @throws NullPointerException if the ID is null.
     * @see TagListener
     */
    public void removeTag(@NonNull ID id) {
        try {
            @Cleanup Connection connection = database.getDataSource().getConnection();
            @Cleanup PreparedStatement statement = connection.prepareStatement("DELETE FROM Tags WHERE CaseName=? AND ID=?");

            statement.setString(1, caseName);
            statement.setString(2, id.getId());

            statement.execute();

            plugins.getPluginManager().getExtensions(TagListener.class).forEach(listener -> {
                listener.tagRemoved(id);
            });
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * @param id The ID whose tag to get.
     * @return The tag of the ID.
     * @throws NullPointerException if the ID is null.
     */
    public String get(@NonNull ID id) {
        try {
            @Cleanup Connection connection = database.getDataSource().getConnection();
            @Cleanup PreparedStatement statement = connection.prepareStatement("SELECT * FROM Tags WHERE ID=? AND CaseName=?");

            statement.setString(1, id.getId());
            statement.setString(2, caseName);

            @Cleanup ResultSet resultSet = statement.executeQuery();

            return resultSet.getString("TagName");
        } catch (SQLException ex) {
            return "";
        }
    }

    /**
     * @return A set of existing tags from the case
     */
    public Set<String> getExistingTags() {
        try {
            @Cleanup Connection connection = database.getDataSource().getConnection();
            @Cleanup PreparedStatement statement = connection.prepareStatement("SELECT * FROM Tags WHERE CaseName=?");

            statement.setString(1, caseName);

            @Cleanup ResultSet resultSet = statement.executeQuery();
            Set<String> tagNames = new HashSet<>();

            while (resultSet.next()) {
                tagNames.add(resultSet.getString("TagName"));
            }
            return tagNames;
        } catch (SQLException ex) {
            return new HashSet<>(0);
        }
    }
}