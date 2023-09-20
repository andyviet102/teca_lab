package vn.teca.digitization.file.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Constants {

    public static final String X_TOTAL_COUNT = "X-Total-Count";

    public static byte[] fileToByteArray(File file) throws IOException {
        byte []buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try {
            ios = new FileInputStream(file);
            if (ios.read(buffer) == -1) {
                throw new IOException("EOF reached while trying to read the whole file");
            }
        } finally {
            try {
                if (ios != null)
                    ios.close();
            } catch (IOException ignored) {
            }
        }
        return buffer;
    }

    public static String getBase64Value(String input){
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String getMD5Value(String input){
        String md5 = "";
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }

            md5 = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            System.err.println("I'm sorry, but MD5 is not a valid message digest algorithm");
        }
        return md5;
    }
}
