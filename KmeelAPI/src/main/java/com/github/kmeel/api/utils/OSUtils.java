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

package com.github.kmeel.api.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URISyntaxException;

/**
 * @author Marten4n6
 */
@Slf4j
public class OSUtils {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    private OSUtils() {}

    public static File getJarFile() {
        try {
            return new File(OSUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public static String getApplicationPath() {
        return getJarFile().getParent() + File.separator;
    }

    public static String getCasesPath() {
        return getApplicationPath() + "Cases" + File.separator;
    }

    public static String getCasePath(String caseName) {
        return getCasesPath() + caseName + File.separator;
    }

    public static File getSettingsFile(String caseName) {
        return new File(getCasesPath() + caseName + File.separator + "Settings.txt");
    }

    public static String getIndexPath(String caseName) {
        return getCasesPath() + caseName + File.separator + "Index" + File.separator;
    }

    public static String getPluginPath() {
        return getApplicationPath() + "plugins" + File.separator;
    }

    public static String getTempPath() {
        return System.getProperty("java.io.tmpdir") + File.separator + "Kmeel" + File.separator;
    }

    public static boolean isWindows() {
        return (OS_NAME.contains("win"));
    }

    public static boolean isMac() {
        return (OS_NAME.contains("mac"));
    }

    public static boolean isUnix() {
        return (OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix"));
    }
}