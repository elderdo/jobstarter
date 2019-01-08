package com.boeing.jobstarter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private static List<String> fileList = new ArrayList<String>();

    public static File ZipIt(File file) throws Exception {
        if (file.isFile()) {
            return ZipSingleFile(file);
        } else if (file.isDirectory()) {
            return ZipDirectory(file);
        } else {
            throw new Exception("Error...Parameter for Zip: " + file.getPath()
                    + " is malformed, is not a file or directory");
        }

    }

    private static File ZipDirectory(File dir) throws Exception {
        if (!dir.exists()) {
            return null;
        }
        String outFile = dir + ".zip";
        PopulateFilesList(dir);
        // now zip files one by one
        // create ZipOutputStream to write to the zip file
        FileOutputStream fos = new FileOutputStream(outFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        for (String filePath : fileList) {
            // for ZipEntry we need to keep only relative file path, so we used substring on
            // absolute path
            ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length() + 1, filePath
                    .length()));
            zos.putNextEntry(ze);
            // read the file and write to ZipOutputStream
            FileInputStream fis = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
            fis.close();
        }
        zos.close();
        fos.close();
        return new File(outFile);
    }

    /**
     * This method populates all the files in a directory to a List
     * 
     * @param dir
     * @throws IOException
     */
    private static void PopulateFilesList(File dir) throws IOException {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile())
                fileList.add(file.getAbsolutePath());
            else
                PopulateFilesList(file);
        }
    }

    /**
     * This method compresses the single file to zip format
     * 
     * @param file
     * @param zipFileName
     * @throws Exception
     */
    private static File ZipSingleFile(File file) throws Exception {
        // create ZipOutputStream to write to the zip file
        if (!file.exists()) {
            return null;
        }
        String zipFileName = file.getName().substring(0, file.getName().lastIndexOf(".")) + ".zip";
        FileOutputStream fos = new FileOutputStream(file.getParent() + FileUtils.fileSeparator()
                + zipFileName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        // add a new Zip Entry to the ZipOutputStream
        ZipEntry ze = new ZipEntry(file.getName());
        zos.putNextEntry(ze);
        // read the file and write to ZipOutputStream
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }

        // Close the zip entry to write to zip file
            zos.closeEntry();
        // Close resources
        zos.close();
        fis.close();
        fos.close();
        return new File(file.getParent() + FileUtils.fileSeparator() + zipFileName);
    }

    public static File Unzip(File zipFile) throws Exception {
        if (!zipFile.exists()) {
            return null;
        }
        File dir = new File(zipFile.getParent());
        File newFile = null;
        // create output directory if it doesn't exist
        FileUtils.CreateDirectory(dir.getPath());
        FileInputStream fis;
        // buffer for read and write data to file
        byte[] buffer = new byte[1024];
        fis = new FileInputStream(zipFile.getPath());
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze = zis.getNextEntry();
        while (ze != null) {
            String fileName = ze.getName();
            newFile = new File(dir.getPath() + FileUtils.fileSeparator() + fileName);
            // create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            // close this ZipEntry
            zis.closeEntry();
            ze = zis.getNextEntry();
        }
        // close last ZipEntry
        zis.closeEntry();
        zis.close();
        fis.close();
        return newFile;
    }

    public static void UnzipAll(File dir) throws Exception {
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        });
        for (int i = 0; i < files.length; i++) {
            ZipUtils.Unzip(files[i]);
            Files.deleteIfExists(files[i].toPath());
        }

    }

    public static void UnzipAll(File dir, String filter) throws Exception {
        if (!dir.exists()) {
            return;
        }
        List<File> files = FileUtils.getFilesByPattern(dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        }), filter);
        for (File file : files) {
            ZipUtils.Unzip(file);
            Files.deleteIfExists(file.toPath());
        }
    }
}
