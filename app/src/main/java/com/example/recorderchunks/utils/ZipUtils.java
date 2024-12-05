package com.example.recorderchunks.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

    public static void extractZip(InputStream zipInputStream, File outputDir) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(zipInputStream);
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            File outFile = new File(outputDir, zipEntry.getName());
            if (zipEntry.isDirectory()) {
                outFile.mkdirs();
            } else {
                outFile.getParentFile().mkdirs(); // Create parent directories
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
        zis.closeEntry();
        zis.close();
    }
}
