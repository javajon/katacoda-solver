package com.katacoda.solver.models;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilsTest {

    @Test
    void crypto() throws Exception {
        File source = new File(getClass().getClassLoader().getResource("solutions.sh").getFile());

        File encrpyted = File.createTempFile("solutions", "-enc");
        File decrpyted = File.createTempFile("solutions", "-dec");
        String key = CryptoUtils.getRandomKey();
        CryptoUtils.encrypt(key, source, encrpyted);
        CryptoUtils.decrypt(key, encrpyted, decrpyted);

        String originalMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(source));
        String descryptedMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(decrpyted));

        assertEquals(originalMd5, descryptedMd5);
    }


    @Test
    void getRandomKey() {
        String key = CryptoUtils.getRandomKey();

        assertEquals(16, key.length());
        assertTrue(key.matches("[A-Z0-9].*"));
    }
}