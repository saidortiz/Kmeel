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

package com.github.kmeel;

import com.github.kmeel.api.utils.OSUtils;
import com.github.kmeel.view.TabStage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Marten4n6
 *         This is the starting class
 */
@Slf4j
public class Kmeel extends Application {

    public static void main(String[] args) {
        log.info("Starting Kmeel...");

        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        checkNotInSourceCode();
        confirmLiveInCurrentDirectory();
        createDirectories();
        copyPlugins();

        new TabStage();
    }

    private ArrayList<String> getFilesInJarDirectory() {
        ArrayList<String> files = new ArrayList<>();

        try {
            Files.list(Paths.get(OSUtils.getJarFile().getParent())).forEach(path -> {
                files.add(path.toFile().getName());
            });
            return files;
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return new ArrayList<>(0);
        }
    }

    private void checkNotInSourceCode() {
        ArrayList<String> filesInJarDirectory = getFilesInJarDirectory();

        if (filesInJarDirectory.contains("pom.xml") && filesInJarDirectory.contains("KmeelAPI")) {
            log.error("Please move the jar file away from the source code.");
            System.exit(1);
        }
    }

    private void confirmLiveInCurrentDirectory() {
        ArrayList<String> filesInJarDirectory = getFilesInJarDirectory();

        if (!filesInJarDirectory.contains("Cases") && !filesInJarDirectory.contains("plugins")) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
            Label label = new Label("Kmeel will live in the current folder, are you sure you want to continue?");

            alert.getDialogPane().setContent(label);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.NO) {
                Platform.exit();
                System.exit(1);
            }
        }
    }

    private void createDirectories() {
        new File(OSUtils.getApplicationPath()).mkdir();
        new File(OSUtils.getCasesPath()).mkdir();
        new File(OSUtils.getPluginPath()).mkdir();
        new File(OSUtils.getTempPath()).mkdir();
    }

    private void copyPlugins() {
        if (new File(OSUtils.getPluginPath()).list().length == 0) {
            if (hasDefaultPlugins()) {
                copyDefaultPlugins();
            } else {
                copyMavenPlugins();
            }
        }
    }

    private boolean hasDefaultPlugins() {
        try {
            JarFile jarFile = new JarFile(OSUtils.getJarFile().getPath());

            for (Enumeration<JarEntry> enumeration = jarFile.entries(); enumeration.hasMoreElements(); ) {
                JarEntry entry = enumeration.nextElement();

                if (entry.getName().startsWith("plugins/") && !entry.getName().equals("plugins/")) {
                    return true;
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return false;
    }

    private void copyDefaultPlugins() {
        try {
            log.info("Attempting to get default plugins...");

            JarFile jarFile = new JarFile(OSUtils.getJarFile().getPath());

            for (Enumeration<JarEntry> enumeration = jarFile.entries(); enumeration.hasMoreElements(); ) {
                JarEntry entry = enumeration.nextElement();

                if (entry.getName().startsWith("plugins/") && !entry.getName().equals("plugins/")) {
                    InputStream plugin = jarFile.getInputStream(entry);
                    Path pluginOutputPath = Paths.get(OSUtils.getApplicationPath() + entry.getName());

                    Files.copy(plugin, pluginOutputPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            log.info("Plugins successfully copied.");
        } catch (IOException ex) {
            // Started from IDE, continue to attempt to get from maven
        }
    }

    private void copyMavenPlugins() {
        try {
            log.info("No default plugins found, attempting to get from maven...");

            // Here goes nothing...
            String[] executeCommand = null;

            if (OSUtils.isWindows()) {
                executeCommand = new String[]{"cmd.exe", "/c", "cd " + OSUtils.getApplicationPath() + " && mvn help:effective-settings"};
            } else if (OSUtils.isUnix() || OSUtils.isMac()) {
                executeCommand = new String[]{"/bin/sh", "-c", "cd " + OSUtils.getApplicationPath() + " && mvn help:effective-settings"};
            }

            Process process = Runtime.getRuntime().exec(executeCommand);
            @Cleanup BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("localRepository")) {
                    String mavenRepositoryPath = line.replace("<localRepository xmlns=\"http://maven.apache.org/SETTINGS/1.1.0\">", "").replace("</localRepository>", "").replace("  ", "");
                    String mavenRepositoryPluginsPath = mavenRepositoryPath + "/com/github/kmeel/plugins/";

                    if (new File(mavenRepositoryPluginsPath).exists()) {
                        log.info("Plugins location found: " + mavenRepositoryPluginsPath);

                        Files.walk(Paths.get(mavenRepositoryPluginsPath))
                                .filter(path -> path.toFile().getName().endsWith(".zip"))
                                .forEach(source -> {
                                    try {
                                        Path target = Paths.get(OSUtils.getPluginPath() + source.toFile().getName());

                                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                                    } catch (IOException ex2) {
                                        log.error(ex2.getMessage(), ex2);
                                    }
                                });

                        log.info("Plugins successfully copied.");
                        break;
                    } else {
                        log.error(mavenRepositoryPluginsPath + " doesn't exist!");
                        log.error("Please compile from source using maven.");
                        return;
                    }
                }
            }

            if (line == null) {
                log.error("Maven is not installed!");
            }
        } catch (IOException ex) {
            log.error("Failed to copy plugins: " + ex);
        }
    }
}