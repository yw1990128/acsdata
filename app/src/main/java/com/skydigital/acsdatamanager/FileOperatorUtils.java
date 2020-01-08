package com.skydigital.acsdatamanager;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileOperatorUtils {
    private static final String TAG = "FileOperatorUtils";
    public static final int SIZE_WHOLE_FILE = 0;

    public static boolean isExist(String filename) {
        File file = new File(filename);
        return file.exists();
    }

    public static boolean isDirectory(String filename) {
        File file = new File(filename);
        return file.isDirectory();
    }

    public static boolean isFile(String filename) {
        File file = new File(filename);
        return file.isFile();
    }

    public static String readStringFromFile(String path, String def) {
        BufferedReader reader = null;

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        try {
            StringBuffer fileData = new StringBuffer(100);
            reader = new BufferedReader(new FileReader(path));
            char[] buf = new char[100];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();

            return fileData.toString();
        } catch (Throwable e) {
            Log.e(TAG,"Exception", e);
        } finally {
            if (null != reader)
                try {
                    reader.close();
                    reader = null;
                } catch (Throwable t) {
                    ;
                }
        }

        return def;
    }

    public static boolean writeStringToFile(String path, String s) {
        return writeStringToFile(path, s, true);
    }

    public static boolean writeByteToFile(String path, byte[] aByte,boolean sync) {
        FileOutputStream wrt = null;

        if (TextUtils.isEmpty(path)) {
            return false;
        }

        try {
            wrt = new FileOutputStream(path);
            wrt.write(aByte);

            if (sync) {
                wrt.getFD().sync();
            }

            wrt.close();
            wrt = null;

            return true;
        } catch (Throwable t) {
            //SkyLog.e("Exception", t);
            t.printStackTrace();
        } finally {
            if (null != wrt)
                try {
                    wrt.close();
                } catch (Throwable t) {
                }
            ;
        }

        return false;
    }

    public static boolean writeByteToFile(String path, byte[] aByte) {
        return writeByteToFile(path, aByte, false);
    }

    public static byte[] readBytesFromFile(String path) {
        return readBytesFromFile(path, SIZE_WHOLE_FILE);
    }

    public static byte[] readBytesFromFile(String path, int length) {
        FileInputStream inputStream = null;
        byte[] ret = null;

        try {
            inputStream = new FileInputStream(path);
            ret=new byte[length>0?length:inputStream.available()];
            inputStream.read(ret);
        }
        catch (Throwable e) {
            Log.e(TAG, "Exception", e);
        }
        finally {
            if (null != inputStream)
                try {
                    inputStream.close();
                    inputStream = null;
                }
                catch (Throwable t) {};
        }

        return ret;
    }

    public static boolean writeStringToFile(String path, String value, boolean sync) {

        FileOutputStream wrt = null;

        if (TextUtils.isEmpty(path)) {
            return false;
        }

        try {
            wrt = new FileOutputStream(path);
            byte[] bytes=value.getBytes();
            wrt.write(bytes);

            if(sync)
            {
                wrt.getFD().sync();
            }
            wrt.close();
            wrt = null;

            return true;
        } catch (Throwable t) {
            //SkyLog.e("Exception", t);
        } finally {
            if (null != wrt)
                try {
                    wrt.close();
                } catch (Throwable t) {
                }
            ;
        }

        return false;
    }

    public static void writeBytesCreateFile(String fileName, byte[] content) {
        FileOutputStream writer = null;

        if (TextUtils.isEmpty(fileName)) {
            return;
        }

        File file = new File(fileName);
        if(file.exists())
        {
            file.delete();
        }

        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                Log.d(TAG, "writeStringAddtion create direction faild !");
                return;
            }
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "writeStringAddtion crate file failed: " + fileName);
            e.printStackTrace();
        }

        try {
            writer = new FileOutputStream(file, false);
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeStringAddtion(String fileName, String content) {
        FileWriter writer = null;

        if (TextUtils.isEmpty(fileName)) {
            return;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    Log.i(TAG, "writeStringAddtion create direction faild !");
                    return;
                }
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "writeStringAddtion crate file failed: " + fileName);
                e.printStackTrace();
            }
        }
        try {
            writer = new FileWriter(fileName, true);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String findAFilePath(String[] paths, String fileName) {
        if (paths == null) {
            return null;
        }

        for (String temp : paths) {
            if (temp == null) {
                continue;
            }

            if (isContainFile(temp, fileName)) {
                return temp;
            }
        }
        return null;
    }

    public static boolean isContainFile(String path, String fileName) {
        return containFile(path, fileName);
    }

    public static String[] getDirAllFileName(String dirPath) {
        return getFileNameStrings(dirPath);
    }

    private static boolean containFile(String path, String fileName) {
        File file_list = new File("/" + path);
        File files[] = null;

        if (!file_list.exists()) {
            return false;
        }

        files = file_list.listFiles();

        if (null == files || files.length <= 0) {
            return false;
        }

        for (File f : files) {
            if (f.toString().endsWith(fileName)) {
                return true;
            }
        }

        return false;
    }

    private static String[] getFileNameStrings(String dirPath) {
        File files[] = getDirFiles(dirPath);
        String nameStr = "";

        if (null == files || files.length <= 0) {
            return null;
        }

        for (File f : files) {
            if (f == null) {
                continue;
            }

            nameStr += f.getAbsolutePath().trim() + ",";
        }

        return nameStr.split(",");
    }

    private static File[] getDirFiles(String dirPath) {
        File file_list = null;
        File files[] = null;

        if (!dirPath.trim().startsWith("/")) {
            file_list = new File("/" + dirPath);
        } else {
            file_list = new File(dirPath);
        }

        if (!file_list.exists()) {
            return null;
        }

        files = file_list.listFiles();

        if (null == files || files.length <= 0) {
            return null;
        }

        return files;
    }

    public static void copyfile(InputStream fromFile, File toFile, Boolean rewrite)
    {
        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }

        if (toFile.exists() && rewrite) {
            toFile.delete();
        }

        try {
            InputStream fosfrom = fromFile;

            FileOutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
        } catch (Exception ex) {
            Log.e("readfile", ex.getMessage());
        }

    }

    public static void deleteFile(String path) {
        File file = new File(path);
        deleteFile(file);
    }

    public static void deleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
        }
        file.delete();
    }

    public static String getMd5String(File f) {

        FileInputStream fis = null;
        byte[] rb = null;
        DigestInputStream digestInputStream = null;
        try {
            fis = new FileInputStream(f);
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digestInputStream = new DigestInputStream(fis, md5);
            byte[] buffer = new byte[4096];
            while (digestInputStream.read(buffer) > 0)
                ;
            md5 = digestInputStream.getMessageDigest();
            rb = md5.digest();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fis) {
                    fis.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rb.length; i++) {
            String a = Integer.toHexString(0XFF & rb[i]);
            if (a.length() < 2) {
                a = '0' + a;
            }
            sb.append(a);
        }
        return sb.toString().toUpperCase();
    }

    public static String[] readMGKIDFile(String filePath) {
        String[] result = new String[3];
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            while ((str = bf.readLine()) != null) {
                String[] temp = str.split(",");
                if (temp != null && temp.length == 3) {
                    result[0] = temp[0].trim();
                    result[1] = temp[1].trim();
                    result[2] = temp[2].trim();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
