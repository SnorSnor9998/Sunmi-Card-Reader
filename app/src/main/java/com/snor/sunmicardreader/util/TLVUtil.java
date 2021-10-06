package com.snor.sunmicardreader.util;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;


public class TLVUtil {


    public static Map<String, TLV> hexStrToTLVMap(String hexStr) {
        hexStr = hexStr.toUpperCase();
        Map<String, TLV> map = new HashMap<>();
        int position = 0;
        while (hexStr.length() > position) {
            // get tag  取得子域Tag标, Tag标签不仅包含1个字节, 2个字节, 还包含3个字节。
            String tag;
            String tagStr = hexStr.substring(position, position + 2);
            int tagInt = Integer.parseInt(tagStr, 16);
            String secondStr = hexStr.substring(position + 2, position + 4);
            int j = Integer.parseInt(secondStr, 16);
            // b5~b1如果全为1, 则说明这个tag下面还有一个子字节, EMV里的tag最多占两个字节
            int temp = tagInt & 0x1F;
            if (temp == 0x1F) {
                temp = j & 0x80;
                if (temp == 0x80) { // 除Tag标签首字节外，tag中其他字节最高位为：1-表示后续还有字节；0-表示为最后一个字节。
                    tag = hexStr.substring(position, position + 6); // 3Bytes的tag
                } else {
                    tag = hexStr.substring(position, position + 4); // 2Bytes的tag
                }
            } else {
                tag = hexStr.substring(position, position + 2);
            }
            boolean b = TextUtils.isEmpty(tag) || TextUtils.equals("00", tag);
            if (b) break;
            position += tag.length();

            // get length
            String lengthStr = hexStr.substring(position, position + 2);
            int lengthInt = Integer.parseInt(lengthStr, 16);
            // Length域的编码比较简单 , 最多有四个字节 , 如果第一个字节的最高位b8为0 , b7~b1的值就是value域的长度 , 如果b8为1, b7~b1的值指示了下面有几个子字节 , 下面子字节的值就是value域的长度
            temp = (lengthInt >> 7) & 1;
            if (temp == 0) {
                position = position + 2;
            } else {
                // 当最左侧的bit位为1的时候 取得后7bit的值
                int var = lengthInt & 127; // 127的二进制 0111 1111
                position = position + 2;
                lengthStr = hexStr.substring(position, position + var * 2);
                // position表示第一个字节，后面的表示有多少个字节来表示后面的Value值
                position = position + var * 2;
            }
            int length = Integer.parseInt(lengthStr, 16);

            // get value
            String value = hexStr.substring(position, position + length * 2);
            position = position + value.length();


            TLV tlv = new TLV(tag, length, value);
            map.put(tag, tlv);
        }
        return map;
    }

    /***
     * 将TLV转换成16进制字符串
     */
    public static String recoverToHexStr(TLV tlv) {
        StringBuilder sb = new StringBuilder();
        String length = TLVValueLengthToHexString(tlv.length);
        sb.append(tlv.tag);
        sb.append(length);
        sb.append(tlv.value);
        return sb.toString();
    }

    /**
     * 将TLV数据反转成字节数组
     */
    public static byte[] recoverToBytes(TLV tlv) {
        String hex = recoverToHexStr(tlv);
        return ByteUtil.hexStr2Bytes(hex);
    }

    /**
     * 将TLV中数据长度转化成16进制字符串
     */
    public static String TLVValueLengthToHexString(int length) {
        if (length < 0) {
            throw new RuntimeException("不符要求的长度");
        }
        if (length <= 0x7f) {
            return String.format("%02x", length);
        } else if (length <= 0xff) {
            return "81" + String.format("%02x", length);
        } else if (length <= 0xffff) {
            return "82" + String.format("%04x", length);
        } else if (length <= 0xffffff) {
            return "83" + String.format("%06x", length);
        } else {
            throw new RuntimeException("TLV 长度最多4个字节");
        }
    }

}