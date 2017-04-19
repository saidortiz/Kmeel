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
import com.github.kmeel.api.utils.OSUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Marten4n6
 *         This class handles settings
 */
@Slf4j
public class Settings {

    private String caseName;

    public Settings(String caseName) {
        this.caseName = caseName;
    }

    /**
     * Sets the value of the key
     */
    public void set(String key, String value) {
        try {
            Properties properties = new Properties();

            if (OSUtils.getSettingsFile(caseName).exists()) {
                @Cleanup FileInputStream inputStream = new FileInputStream(OSUtils.getSettingsFile(caseName));
                properties.load(inputStream);
            }

            @Cleanup FileOutputStream outputStream = new FileOutputStream(OSUtils.getSettingsFile(caseName));

            properties.put(key, value);
            properties.store(outputStream, null);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * @return The value of the key.
     * @throws NullPointerException if the key is null.
     */
    public String get(@NonNull String key) {
        try {
            Properties properties = new Properties();
            @Cleanup FileInputStream inputStream = new FileInputStream(OSUtils.getSettingsFile(caseName));

            properties.load(inputStream);
            return properties.get(key).toString();
        } catch (IOException | NullPointerException ex) {
            return null;
        }
    }
}