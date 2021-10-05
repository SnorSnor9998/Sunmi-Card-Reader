package com.snor.sunmicardreader.util;

public final class TLV {

    private final String tag;
    private final int length;
    private final String value;

    public TLV(String tag, String value) {
        this.tag = null2UpperCaseString(tag);
        this.value = null2UpperCaseString(value);
        this.length = ByteUtil.hexStr2Bytes(value).length;
    }

    public TLV(String tag, int length, String value) {
        this.tag = null2UpperCaseString(tag);
        this.length = length;
        this.value = null2UpperCaseString(value);
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

    public String recoverToHexStr() {
        return TLVUtil.revertToHexStr(this);
    }

    public byte[] recoverToBytes() {
        return TLVUtil.revertToBytes(this);
    }

    @Override
    public String toString() {
        return "tag=[" + tag + "]," + "length=[" + length + "]," + "value=[" + value + "]";
    }

    private String null2UpperCaseString(String src) {
        return src == null ? "" : src.toUpperCase();
    }


}
