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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marten4n6
 *         Calls all supported parsers on the sources.
 */
@Slf4j
public class FileParser {

    private KmeelAPI kmeelAPI;
    private LoadingView loadingView;

    private ExecutorService executorService;
    private FinishedListener finishedListener;

    public FileParser(KmeelAPI kmeelAPI, LoadingView loadingView) {
        this.kmeelAPI = kmeelAPI;
        this.loadingView = loadingView;

        executorService = Executors.newSingleThreadExecutor();
    }

    public void parseFiles(List<String> filePaths) {
        executorService.submit(() -> {
            AtomicInteger runningParsersForFile = new AtomicInteger(0);

            filePaths.forEach(filePath -> {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                AtomicBoolean foundParser = new AtomicBoolean(false);

                // Call all parsers on the file
                kmeelAPI.plugins().getPluginManager().getExtensions(Parser.class).forEach(parser -> {
                    if (parser.getAcceptedExtensions().contains(FilenameUtils.getExtension(filePath))) {
                        foundParser.set(true);
                        runningParsersForFile.incrementAndGet();

                        ActionListener finishedListener = (event) -> {
                            runningParsersForFile.decrementAndGet();

                            if (runningParsersForFile.get() == 0) {
                                countDownLatch.countDown();
                            }
                        };

                        parser.parseFile(new File(filePath), filePaths.size(), finishedListener);
                    }
                });

                if (foundParser.get()) {
                    try {
                        countDownLatch.await(); // Wait for parsers to finish before continuing
                    } catch (InterruptedException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            });

            finishedListener.finished(kmeelAPI, loadingView);
        });
    }

    public void parseDirectory(File directory, boolean parseSubFolders) {
        executorService.submit(() -> {
            try {
                HashMap<String, Integer> fileAmountFromExtension = getFileAmountFromExtension(kmeelAPI.messages().getSupportedFileExtensions(), directory, parseSubFolders);
                AtomicInteger runningParsersForFile = new AtomicInteger(0);

                Files.walkFileTree(Paths.get(directory.getPath()), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                        String extension = FilenameUtils.getExtension(path.toString());

                        if (kmeelAPI.messages().getSupportedFileExtensions().contains(extension)) {
                            CountDownLatch countDownLatch = new CountDownLatch(1);
                            AtomicBoolean foundParser = new AtomicBoolean(false);

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
                                    countDownLatch.await(); // Wait for parsers to finish before continuing
                                } catch (InterruptedException ex) {
                                    log.error(ex.getMessage(), ex);
                                }
                            }
                        }

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        if (!parseSubFolders && !path.toString().equals(directory.getPath())) {
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

            finishedListener.finished(kmeelAPI, loadingView);
        });
    }

    private HashMap<String, Integer> getFileAmountFromExtension(Set<String> extensions, File folder, boolean parseSubFolders) {
        try {
            HashMap<String, Integer> fileAmountFromExtension = new HashMap<>();

            Files.walkFileTree(Paths.get(folder.getPath()), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    String extension = FilenameUtils.getExtension(path.toFile().getName());

                    if (extensions.contains(extension)) {
                        fileAmountFromExtension.putIfAbsent(extension, 0);
                        fileAmountFromExtension.put(extension, fileAmountFromExtension.get(extension) + 1);
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

    /**
     * Called when the parseFiles or parseDirectory method is finished.
     */
    public void setFinishedListener(FinishedListener listener) {
        finishedListener = listener;
    }
}
