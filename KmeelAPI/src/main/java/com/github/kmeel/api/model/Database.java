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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.github.kmeel.api.utils.OSUtils;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Marten4n6
 *         Database thread pool, not sure if this is over-kill
 */
@Slf4j
public class Database {

    private @Getter HikariDataSource dataSource;

    public Database(String caseName) {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(100);
        config.setJdbcUrl("jdbc:sqlite:" + OSUtils.getCasePath(caseName) + "Kmeel.db");

        dataSource = new HikariDataSource(config);

        try {
            @Cleanup Statement statement = dataSource.getConnection().createStatement();

            statement.execute("CREATE TABLE IF NOT EXISTS Bookmarks(CaseName, ID, Other)");
            statement.execute("CREATE TABLE IF NOT EXISTS Tags(CaseName, ID, TagName)");
        } catch (SQLException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}