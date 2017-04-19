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

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import com.github.kmeel.api.utils.OSUtils;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marten4n6
 */
@Slf4j
public class Plugins {

    private PluginManager pluginManager;
    private static @Getter Set<String> pluginsDisabledByDefault = new HashSet<>();

    public Plugins() {
        pluginManager = new DefaultPluginManager(new File(OSUtils.getPluginPath()));

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        int startedPlugins = pluginManager.getStartedPlugins().size();
        if (startedPlugins == 1) {
            log.info("Started 1 plugin.");
        } else if (startedPlugins > 1) {
            log.info("Started " + startedPlugins + " plugins.");
        } else {
            log.warn("No plugins started!");
        }
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Disables the plugin by default.
     *
     * @throws NullPointerException if the pluginName is null.
     */
    public static void addDisabledByDefault(@NonNull String pluginName) {
        pluginsDisabledByDefault.add(pluginName);
    }
}
