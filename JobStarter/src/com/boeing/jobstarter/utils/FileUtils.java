package com.boeing.jobstarter.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileUtils {

    public static File MoveToWork(File iFile, String newName) throws Exception {
        if (!iFile.exists()) {
            return null;
        }
        String path = XMLHandler.getDirectories(Globals.TagNames.WORK_DIR_TAG) + fileSeparator()
                + Globals.getProcessId("<PID>");

        File oFile = new File(path + fileSeparator() + newName + ".dat");
        return Move(iFile, oFile);
    }

    public static File MoveToWork(File iFile, String newName, String schema) throws Exception {
        if (!iFile.exists()) {
            return null;
        }
        String path = XMLHandler.getDirectories(Globals.TagNames.WORK_DIR_TAG) + schema.toUpperCase()
                + fileSeparator() + Globals.getProcessId("<PID>");

        File oFile = new File(path + fileSeparator() + newName + ".dat");
        return Move(iFile, oFile);
    }

    public static File MoveToArchive(File iFile, String schemaName) throws Exception {
        if (!iFile.exists()) {
            return null;
        }
        String path = XMLHandler.getDirectories(Globals.TagNames.ARCHIVE_DIR_TAG)
                + schemaName.toUpperCase();

        File oFile = new File(path + fileSeparator() + iFile.getName());

        return Move(iFile, oFile);
    }

    public static File CopyToArchive(File iFile, String schemaName) throws Exception {
        if (!iFile.exists()) {
            return null;
        }
        String path = XMLHandler.getDirectories(Globals.TagNames.ARCHIVE_DIR_TAG)
                + schemaName.toUpperCase();

        File oFile = new File(path + fileSeparator() + iFile.getName());

        return Copy(iFile, oFile);
    }

    public static File Move(File iFile, File oFile) throws Exception {
        if (!iFile.exists()) {
            return null;
        }
        if (oFile.isDirectory()) {
            CreateDirectory(oFile.getPath());
            oFile = new File(oFile.getPath()
                    + (oFile.getPath().endsWith(FileUtils.fileSeparator()) ? "" : FileUtils.fileSeparator())
                    + iFile.getName());
        } else {
            if (oFile.getName().contains(".")) {
                CreateDirectory(oFile.getParent());
            } else {
                CreateDirectory(oFile.getPath());
            }
        }
        Path moved = Files.move(iFile.toPath(), oFile.toPath());
        if (moved == null) {
            throw new Exception("Process failed, moving file into folder: " + oFile.getAbsolutePath());
        }
        return oFile;
    }

    public static File Copy(File iFile, File oFile) throws Exception {
        if (!iFile.exists()) {
            return null;
        }
        if (oFile.isDirectory()) {
            CreateDirectory(oFile.getPath());
            oFile = new File(oFile.getPath()
                    + (oFile.getPath().endsWith(FileUtils.fileSeparator()) ? "" : FileUtils.fileSeparator())
                    + iFile.getName());
        } else {
            if (oFile.getName().contains(".")) {
                CreateDirectory(oFile.getParent());
            } else {
                CreateDirectory(oFile.getPath());
            }
        }
        Path moved = Files.copy(iFile.toPath(), oFile.toPath());
        if (moved == null) {
            throw new Exception("Process failed, copying file into folder: " + oFile.getAbsolutePath());
        }
        return oFile;
    }

    public static void DeleteDirectory(File dir) throws Exception {
        if (!dir.exists()) {
            return;
        }
        Path rootPath = dir.toPath();
        List<Path> pathsToDelete = Files.walk(rootPath).sorted(Comparator.reverseOrder()).collect(
                Collectors.toList());
        for (Path path : pathsToDelete) {
            Files.deleteIfExists(path);
        }
    }

    public static void DeleteFile(File file) throws Exception {
        Files.deleteIfExists(file.toPath());
    }

    public static String ReadFile(File file) throws Exception {
        if (!file.exists()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
        try {

            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

        } catch (Exception ex) {
            throw ex;
        } finally {
            br.close();
        }
        return sb.toString();
    }

    public static String ReadHeader(File file) throws Exception {
        BufferedReader br = null;
        String text = null;
        try {
            br = new BufferedReader(new FileReader(file.getPath()));
            text = br.readLine();
        } catch (Exception e) {
            throw e;
        } finally {
            br.close();
        }
        return text;
    }

    public static List<File> getFilesByPattern(File[] files, List<String> patterns) {
        List<File> matchedFiles = new ArrayList<File>();
        patterns.forEach(
                p -> {
            Pattern pattern = Pattern.compile("^" + p.replace("*", "(.*)") + "$");
                    matchedFiles.addAll(Arrays.asList(files).stream().filter(
                            f -> (pattern.matcher(f.getName()).find())).collect(Collectors.toList()));
                });

        return matchedFiles;
    }

    public static List<File> getFilesByPattern(File[] files, String pattern) {
        Pattern p = Pattern.compile("^" + pattern.replace("*", "(.*)") + "$");
        return new ArrayList<File>(Arrays.asList(files).stream().filter(f -> (p.matcher(f.getName()).find()))
                .collect(Collectors.toList()));
    }

    public static boolean DirectoryExist(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    public static void CreateDirectory(String path) throws Exception {
        if (!DirectoryExist(path)) {
            Files.createDirectories(Paths.get(path));
        }
    }

    public static String fileSeparator() {
        return System.getProperty("file.separator");
    }
}
