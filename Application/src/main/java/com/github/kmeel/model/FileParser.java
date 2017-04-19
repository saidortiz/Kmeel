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

package com.github.kmeel.model;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.spi.Parser;
import com.github.kmeel.api.view.LoadingView;
import com.github.kmeel.listeners.FinishedListener;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marten4n6
 */
@Slf4j
public class FileParser {

    private static final FileParser INSTANCE = new FileParser();

    private FinishedListener finishedListener;
    private AtomicInteger runningParsersForFile;

    public static FileParser getInstance() {
        return INSTANCE;
    }

    public void parseFilesOrDirectory(KmeelAPI kmeelAPI, LoadingView loadingView, List<String> sources, boolean parseSubFolders) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HashMap<String, Integer> fileAmountFromExtension = getFileAmountFromExtension(kmeelAPI.messages().getSupportedFileExtensions(), new File(sources.get(0)), parseSubFolders);
                runningParsersForFile = new AtomicInteger(0);

                if (new File(sources.get(0)).isDirectory()) {
                    /* Directory */
                    try {
                        Files.walkFileTree(Paths.get(new File(sources.get(0)).getPath()), new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                                String extension = FilenameUtils.getExtension(path.toString());

                                if (kmeelAPI.messages().getSupportedFileExtensions().contains(extension)) {
                                    CountDownLatch countDownLatch = new CountDownLatch(1);
                                    AtomicBoolean foundParser = new AtomicBoolean(false); //Found a parser that accepts this file

                                    // Call all parsers on the file
                                    kmeelAPI.plugins().getPluginManager().getExtensions(Parser.class).forEach(parser -> {
                                        if (parser.getAcceptedExtensions().contains(extension)) {
                                            foundParser.set(true);
                                            runningParsersForFile.incrementAndGet();

                                            ActionListener finishedListener = (event) -> {
                                                runningParsersForFile.decrementAndGet();

                                                if (runningParsersForFile.get() == 0) {
                                                    countDownLatch.countDown();
                                                }
                                            };

                                            parser.parseFile(path.toFile(), fileAmountFromExtension.get(extension), finishedListener);
                                        }
                                    });

                                    if (foundParser.get()) {
                                        try {
                                            countDownLatch.await(); //Wait for all parsers to finish before continuing
                                        } catch (InterruptedException ex) {
                                            log.error(ex.getMessage(), ex);
                                        }
                                    }
                                }

                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                                if (!parseSubFolders && !path.toString().equals(new File(sources.get(0)).getPath())) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                } else {
                                    return FileVisitResult.CONTINUE;
                                }
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException ex) {
                                log.error(file.toFile().getPath() + ": " + ex.getMessage());
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (IOException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                } else {
                    /* File */
                    sources.forEach(source -> {
                        CountDownLatch countDownLatch = new CountDownLatch(1);
                        AtomicBoolean foundParser = new AtomicBoolean(false);

                        // Call all parsers on the file
                        kmeelAPI.plugins().getPluginManager().getExtensions(Parser.class).forEach(parser -> {
                            if (parser.getAcceptedExtensions().contains(FilenameUtils.getExtension(source))) {
                                foundParser.set(true);
                                runningParsersForFile.incrementAndGet();

                                ActionListener finishedListener = (event) -> {
                                    runningParsersForFile.decrementAndGet();

                                    if (runningParsersForFile.get() == 0) {
                                        countDownLatch.countDown();
                                    }
                                };

                                parser.parseFile(new File(source), sources.size(), finishedListener);
                            }
                        });

                        if (foundParser.get()) {
                            try {
                                countDownLatch.await(); //Wait for all parsers to finish before continuing
                            } catch (InterruptedException ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        }
                    });
                }

                finishedListener.finished(kmeelAPI, loadingView);
                return null;
            }
        };

        new Thread(task).start();
    }

    private HashMap<String, Integer> getFileAmountFromExtension(Set<String> extensions, File folder, boolean parseSubFolders) {
        try {
            HashMap<String, Integer> fileAmountFromExtension = new HashMap<>();

            Files.walkFileTree(Paths.get(folder.getPath()), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    String extension = FilenameUtils.getExtension(path.toFile().getName());

                    if (extensions.contains(extension)) {
                        if (fileAmountFromExtension.get(extension) == null) {
                            fileAmountFromExtension.put(extension, 1);
                        } else {
                            fileAmountFromExtension.put(extension, fileAmountFromExtension.get(extension) + 1);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    if (!parseSubFolders && !path.toString().equals(folder.getPath())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException ex) {
                    log.error(file.toFile().getPath() + ": " + ex.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

            return fileAmountFromExtension;
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public void setFinishedListener(FinishedListener listener) {
        finishedListener = listener;
    }
}
