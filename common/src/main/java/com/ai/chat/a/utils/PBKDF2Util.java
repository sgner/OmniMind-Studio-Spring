package com.ai.chat.a.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
public class PBKDF2Util {
    private static final int SALT_LENGTH = 16; // 盐值长度
    private static final int HASH_LENGTH = 64; // 生成的密钥长度（字节数）
    private static final int ITERATIONS = 10000; // 迭代次数
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256"; // 使用SHA-256作为哈希算法

    /**
     * 生成一个随机盐值
     *
     * @return 随机盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用PBKDF2算法生成加密后的密码
     *
     * @param password 明文密码
     * @param salt     盐值
     * @return 加密后的密码
     */
    public static String hashPassword(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), ITERATIONS, HASH_LENGTH * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing password", e);
        }
    }

    /**
     * 验证输入的密码是否与加密后的密码匹配
     *
     * @param originalPassword 明文密码
     * @param storedHash       存储的加密密码
     * @param salt             存储的盐值
     * @return 密码是否匹配
     */
    public static boolean validatePassword(String originalPassword, String storedHash, String salt) {
        String newHash = hashPassword(originalPassword, salt);
        return newHash.equals(storedHash);
    }

}
