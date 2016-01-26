package com.idotools.http.utils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public final class MD5 {
    /***
     * MD5
     * @param paramArrayOfByte
     * @param bit   16位    32位
     * @return
     */
    public static final String getMessageDigest(byte[] paramArrayOfByte,int bit) {
        char[] arrayOfChar1 = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            MessageDigest localMessageDigest;
            (localMessageDigest = MessageDigest.getInstance("MD5"))
                    .update(paramArrayOfByte);
            int i;
            char[] arrayOfChar2 = new char[(i = (paramArrayOfByte = localMessageDigest
                    .digest()).length) * 2];
            int j = 0;
            for (int k = 0; k < i; ++k) {
                int l = paramArrayOfByte[k];
                arrayOfChar2[(j++)] = arrayOfChar1[(l >>> 4 & 0xF)];
                arrayOfChar2[(j++)] = arrayOfChar1[(l & 0xF)];
            }
            if(bit==32)
                return new String(arrayOfChar2);
            else
                return new String(arrayOfChar2).substring(8,24);
        } catch (Exception localException) {
        }
        return null;
    }
    
    public static final String getMessageDigest(byte[] paramArrayOfByte) {
        return getMessageDigest(paramArrayOfByte,32);
    }
    
    public static final byte[] getRawDigest(byte[] paramArrayOfByte) {
        try {
            MessageDigest localMessageDigest;
            (localMessageDigest = MessageDigest.getInstance("MD5"))
                    .update(paramArrayOfByte);
            return localMessageDigest.digest();
        } catch (Exception localException) {
        }
        return null;
    }
}