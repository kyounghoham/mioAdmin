package com.mio.admin.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.GeneralSecurityException;
import java.security.SignatureException;

public class Signatures {

    private static final String PROVIDER = "BC";
    private static final String HMAC_SHA256 = "HmacSHA256";

    public static String of(String timestamp, String method, String resource, String key) throws SignatureException {

        return of(timestamp + "." + method + "." + resource, key);
    }

    public static String of(String data, String key) throws SignatureException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));

            return DatatypeConverter.printBase64Binary(mac.doFinal(data.getBytes()));
        } catch (GeneralSecurityException e) {
            throw new SignatureException("Failed to generate signature.", e);
        }
    }
}