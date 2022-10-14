package com.gv.csc.util;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileUtility {

    public boolean deleteDirectory(File file) {
        String[] entries = file.list();
        for (String s : entries) {
            File currentFile = new File(file.getPath(), s);
            currentFile.delete();
        }
        return true;
    }

    public static boolean checkIfFileExists() {
        return true;
    }

}
