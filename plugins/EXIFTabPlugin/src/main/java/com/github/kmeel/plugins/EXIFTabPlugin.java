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
import com.github.kmeel.api.model.Plugins;
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.spi.MessageTab;
import com.github.kmeel.api.spi.listeners.MessageListener;
import com.github.kmeel.api.view.MessagePane;
import javafx.scene.control.Tab;
import lombok.extern.slf4j.Slf4j;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * @author Marten4n6
 *         This plugin adds the EXIF tab,
 *         requires exiftool by Phil Harvey to be installed and runnable in the terminal
 */
@Slf4j
public class EXIFTabPlugin extends Plugin {

    public EXIFTabPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() throws PluginException {
        // EXIFTool is probably not installed so let's disable this plugin by default
        Plugins.addDisabledByDefault(this.wrapper.getPluginId());
    }

    @Extension
    public static class EXIFTab implements MessageTab, MessageListener {

        @Override
        public Tab getMessageTab(KmeelAPI kmeelAPI, MessagePane messagePane) {
            WhateverTab tab = new WhateverTab(kmeelAPI);
            return tab.getTab();
        }

        @Override
        public void messageSelected(MessagePane messagePane, ID id) {
            messagePane.getTabPane().getTabs().forEach(tab -> {
                if (tab.getId() != null && tab.getId().equals("EXIFTabPlugin")) {
                    TabData tabData = (TabData)tab.getUserData();

                    tabData.setSelectedID(id);
                    tabData.getUpdateListener().update(id);
                }
            });
        }

        @Override
        public void attachmentSelected(MessagePane messagePane) {}
    }
}
