package io.github.lujiafa.houtu.util.common;

import io.github.lujiafa.houtu.util.data.ByteUtils;
import io.github.lujiafa.houtu.util.data.HexUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CodecData {

    private byte[] bytes;

    CodecData(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes can not be null");
        }
        this.bytes = bytes;
    }

    public byte[] bytes() {
        return bytes;
    }

    public String base64() {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String hex() {
        return HexUtils.toHex(bytes);
    }

    public String utf8() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String ascii() {
        return new String(bytes, StandardCharsets.US_ASCII);
    }


    public static CodecData base64(String base64) {
        return new CodecData(Base64.getDecoder().decode(base64));
    }

    public static CodecData hex(String hex) {
        return new CodecData(ByteUtils.toBinary(hex));
    }

    public static CodecData utf8(String utf8String) {
        return new CodecData(utf8String.getBytes(StandardCharsets.UTF_8));
    }

    public static CodecData ascii(String asciiString) {
        return new CodecData(asciiString.getBytes(StandardCharsets.US_ASCII));
    }

    public static CodecData bytes(byte[] bytes) {
        return new CodecData(bytes);
    }

}
