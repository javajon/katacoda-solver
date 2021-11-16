package com.katacoda.solver.models;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoUtilsTest {

    @Test
    void crypto() throws Exception {
        File source = new File(Thread.currentThread().getContextClassLoader().getResource("solutions.sh").getFile());

        File encrpyted = File.createTempFile("solutions", "-enc");
        File decrpyted = File.createTempFile("solutions", "-dec");

        String originalMd5 = "";
        String descryptedMd5 = "";

        String key = CryptoUtils.getRandomKey();
        try (InputStream input = new FileInputStream(source); OutputStream output = new FileOutputStream(encrpyted)) {
            CryptoUtils.encrypt(key, input, output);
            originalMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(source));
        }

        try (InputStream input = new FileInputStream(encrpyted); OutputStream output = new FileOutputStream(decrpyted)) {
            CryptoUtils.decrypt(key, input, output);
            descryptedMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(decrpyted));
        }

        assertEquals(originalMd5, descryptedMd5);
    }


    @Test
    void getRandomKey() {
        String key = CryptoUtils.getRandomKey();

        assertEquals(16, key.length());
        assertTrue(key.matches("[A-Z0-9].*"));
    }

    @Test
    public void whenIsEncryptedAndDecrypted_thenDecryptedEqualsOriginal() throws Exception {
        String encryptionKeyString = "thisisa128bitkey";
        String originalMessage = "This is a secret message";
        byte[] encryptionKeyBytes = encryptionKeyString.getBytes();

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(encryptionKeyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedMessageBytes = cipher.doFinal(originalMessage.getBytes());

        secretKey = new SecretKeySpec(encryptionKeyBytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedMessageBytes = cipher.doFinal(encryptedMessageBytes);
        assertEquals(originalMessage, new String(decryptedMessageBytes));
    }
}