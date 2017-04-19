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

package com.github.kmeel.api.spi;

import com.github.kmeel.api.KmeelAPI;
import ro.fortsoft.pf4j.ExtensionPoint;

import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Marten4n6
 *         Pluginable interface for creating reports.
 */
public interface Report extends ExtensionPoint {

    /**
     * @return The format of this report (PDF, DOCX etc).
     */
    String getReportFileFormat();

    /**
     * Creates a report.
     *
     * @param kmeelAPI         API to interact with the current case.
     * @param outputFolder     The folder this report should be created in.
     * @param reportName       The name of the report.
     * @param finishedListener This listener should be called when the report is created.
     */
    void createReport(KmeelAPI kmeelAPI, File outputFolder, String reportName, ActionListener finishedListener);
}
