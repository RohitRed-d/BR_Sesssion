package com.gv.csc.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class Utility {
    public static final int BUFFER_SIZE = 4096;
    static final boolean IS_WINDOWS;

    static {
        IS_WINDOWS = (File.separatorChar == '\\');
    }

    public void unzip(String src, String dest) throws IOException {
        File destDir = new File(dest);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(src));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    public static File createThumbnail(File imageFile) throws IOException {
        int thumbnail_height = 400;
        int thumbnail_width = 400;
        BufferedImage img = new BufferedImage(thumbnail_width, thumbnail_height, BufferedImage.TYPE_INT_RGB);
        img.createGraphics().drawImage(ImageIO.read(imageFile).getScaledInstance(thumbnail_width, thumbnail_height, Image.SCALE_SMOOTH), 0, 0, null);
        File outputFile = new File(imageFile.getParentFile() + File.separator + "thumbnail_" + imageFile.getName());
        ImageIO.write(img, "jpg", outputFile);
        return outputFile;
    }

    public static MultiValueMap<String, Object> prepareMultipartFilesBody(List<File> files) {
        MultiValueMap<String, Object> multiPartsBody = new LinkedMultiValueMap<>();
        for(File file : files) {
            multiPartsBody.add(file.getName(), new FileSystemResource(file));
        }

        return multiPartsBody;
    }

    public static JSONArray prepareAttachmentJSONArrayForStyle(JSONArray BRAttachmentsJSON, JSONArray CLOSETAttachmentsJSON) {
        JSONObject CLOSETAttachmentJSON = new JSONObject();
        JSONObject BRAttachmentJSON = new JSONObject();
        JSONObject finalAttachmentJSON = new JSONObject();
        JSONArray finalAttachmentJSONArray = new JSONArray();
        String CLOSETAttachmentName = "";
        String CLOSETAttachmentPath = "";
        String CLOSETAttachmentNo = "";

        String BRAttachmentName = "";
        String BRAttachmentPath = "";
        String BRAttachmentNo = "";

        for (int i = 0; i < CLOSETAttachmentsJSON.length(); i++) {
            finalAttachmentJSON = new JSONObject();
            CLOSETAttachmentJSON = new JSONObject();
            BRAttachmentJSON = new JSONObject();
            CLOSETAttachmentJSON = CLOSETAttachmentsJSON.getJSONObject(i);

            if(CLOSETAttachmentJSON.isEmpty()) {
                continue;
            }

            for (int j = 0; j < BRAttachmentsJSON.length(); j++) {
                BRAttachmentJSON = BRAttachmentsJSON.getJSONObject(j);
                if(BRAttachmentJSON.isEmpty()) {
                    continue;
                }
                CLOSETAttachmentName = CLOSETAttachmentJSON.getString("fileName");
                BRAttachmentName = BRAttachmentJSON.getString("@oldname");

                if(CLOSETAttachmentName.equals(BRAttachmentName)) {
                    finalAttachmentJSON.put("@location", BRAttachmentJSON.getString("@newname"));
                    finalAttachmentJSON.put("@attachment_no", CLOSETAttachmentJSON.getString("@attachment_no"));
                    finalAttachmentJSONArray.put(finalAttachmentJSON);
                    break;
                }
            }
        }
        return finalAttachmentJSONArray;
    }

    public static List<File> getListOfFilesToUpload(JSONArray attachments) {
        File file = null;
        List<File> files = new ArrayList<File>();
        JSONObject attachment = new JSONObject();
        String filePath = "";
        for(int i = 0; i < attachments.length(); i++) {
            attachment = attachments.getJSONObject(i);
            filePath = attachment.getString("filePath");
            file = new File(filePath);
            files.add(file);
        }

        return files;
    }

    public static final boolean hasContent(final String s) {
        if (s == null || "null".equals(s) || "undefined".equals(s) || "0".equals(s) || "0.0".equals(s) || "0,0".equals(s)) {
            return false;
        }
        if (s.startsWith("0.0") || s.startsWith("0,0")) {
            boolean allZeros = true;
            for (int strLen = s.length(), i = 2; i < strLen; ++i) {
                if (s.charAt(i) != '0') {
                    allZeros = false;
                    break;
                }
            }
            if (allZeros) {
                return false;
            }
        }
        for (int j = 0; j < s.length(); ++j) {
            if (!Character.isWhitespace(s.charAt(j))) {
                return true;
            }
        }
        return false;
    }
    public static boolean isJSONObject(String jsonString) {
        try {
            new JSONObject(jsonString);
        } catch (JSONException ex) {
            return false;
        }

        return true;
    }
    public static boolean isJSONArray(String jsonString) {
        try {
            new JSONArray(jsonString);
        } catch (JSONException ex) {
                return false;
        }

        return true;
    }

    public static void extractFile(ZipInputStream zipIn, String filePath) throws Exception {
        System.out.println("INFO::Utility: extractFile() -> started");
        BufferedOutputStream bos = null ;
        try{
            FileOutputStream fs = new FileOutputStream(filePath);
            bos = new BufferedOutputStream(fs);
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new Exception(e.getLocalizedMessage());
        }
        finally{
            try {
                bos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new Exception(e.getLocalizedMessage());
            }
        }
        System.out.println("INFO::Utility: extractFile() -> end");
    }

    public static final String formatOSFolderLocation(final String folderLocation) {
        String formatedFolderLocation = folderLocation;
        if (hasContent(formatedFolderLocation)) {
            try {
                if (Utility.IS_WINDOWS) {
                    formatedFolderLocation = formatedFolderLocation.replace('/', '\\');
                }
                else {
                    formatedFolderLocation = formatedFolderLocation.replace('\\', '/');
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return formatedFolderLocation;
    }
}
