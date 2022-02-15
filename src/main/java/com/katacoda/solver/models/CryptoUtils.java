package com.katacoda.solver.models;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * A utility class that encrypts or decrypts inputs into outputs.
 *
 * @author www.codejava.net
 * https://www.codejava.net/coding/file-encryption-and-decryption-simple-example
 */
public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding"; // to work with openssl -d "aes-128-ecb"

    public static void encrypt(String key, InputStream input, OutputStream output) throws CryptoException {
        doCrypto(Cipher.ENCRYPT_MODE, key, input, output);
    }

    public static void decrypt(String key, InputStream input, OutputStream output) throws CryptoException {
        doCrypto(Cipher.DECRYPT_MODE, key, input, output);
    }

    private static void doCrypto(int cipherMode, String key, InputStream input,
                                 OutputStream output) throws CryptoException {
        Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(cipherMode, secretKey);
            output.write(cipher.doFinal(input.readAllBytes()));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException e) {
            throw new CryptoException("Error " + (cipherMode == Cipher.ENCRYPT_MODE ? " encrypting" : "decrypting") + " file because: " + e.getMessage(), e);
        }
    }

    public static String getRandomKey() {
        String SOURCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder key = new StringBuilder();
        Random rnd = new Random();
        while (key.length() < 16) {
            int index = (int) (rnd.nextFloat() * SOURCE.length());
            key.append(SOURCE.charAt(index));
        }
        return key.toString();
    }

    public static class CryptoException extends Exception {
        public CryptoException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
