package com.myki.inspector.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;

@Service
public class TotpService {
    // RFC 6238 defaults for Google Authenticator compatibility
    private static final int TOTP_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final String HMAC_ALG = "HmacSHA1"; // GA default

    // Base32 alphabet (no padding) used by otpauth
    private static final char[] BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    public String generateSecret() {
        byte[] bytes = new byte[20]; // 160-bit secret
        new SecureRandom().nextBytes(bytes);
        return base32Encode(bytes);
    }

    public boolean verifyCode(String base32Secret, int inputCode) {
        byte[] key = base32Decode(base32Secret);
        long currentCounter = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        // allow small time drift: previous, current, next
        for (long i = -1; i <= 1; i++) {
            int code = generateTotp(key, currentCounter + i);
            if (code == inputCode) return true;
        }
        return false;
    }

    public String buildOtpAuthURL(String issuer, String accountName, String secret) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                urlEncode(issuer), urlEncode(accountName), secret, urlEncode(issuer), TOTP_DIGITS, TIME_STEP_SECONDS);
    }

    private int generateTotp(byte[] key, long counter) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(key, HMAC_ALG));
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int mod = (int) Math.pow(10, TOTP_DIGITS);
            return binary % mod;
        } catch (Exception e) {
            throw new IllegalStateException("TOTP generation failed", e);
        }
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    // ---- Base32 encode/decode (RFC 4648, no padding) ----
    private String base32Encode(byte[] bytes) {
        StringBuilder result = new StringBuilder((bytes.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                bitsLeft -= 5;
                result.append(BASE32[index]);
            }
        }
        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            result.append(BASE32[index]);
        }
        return result.toString();
    }

    private byte[] base32Decode(String base32) {
        String s = base32.trim().replace("=", "").replaceAll("\\s+", "").toUpperCase();
        int buffer = 0;
        int bitsLeft = 0;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(s.length() * 5 / 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int val = charToBase32(c);
            if (val < 0) continue; // skip invalid
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out.write((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return out.toByteArray();
    }

    private int charToBase32(char c) {
        if (c >= 'A' && c <= 'Z') return c - 'A';
        if (c >= '2' && c <= '7') return 26 + (c - '2');
        return -1;
    }
}
