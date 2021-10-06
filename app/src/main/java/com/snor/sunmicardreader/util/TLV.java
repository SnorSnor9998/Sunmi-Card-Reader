package com.snor.sunmicardreader.util;

public class TLV {

    public String tag;
    public int length;
    public String value;

    public TLV(String tag, String value) {
        this.tag = tag;
        this.value = value;
        this.length = ByteUtil.hexStr2Bytes(value).length;
    }

    public TLV(String tag, int length, String value) {
        this.length = length;
        this.tag = tag;
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public int getLength() {
        return length;
    }

    public String getValue() {
        return value;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /***
     * 将TLV恢复成字符串
     */
    public String recoverToHexStr() {
        return TLVUtil.recoverToHexStr(this);
    }

    /***
     * 将TLV恢复成字节数组
     */
    public byte[] recoverToBytes() {
        return TLVUtil.recoverToBytes(this);
    }

}
