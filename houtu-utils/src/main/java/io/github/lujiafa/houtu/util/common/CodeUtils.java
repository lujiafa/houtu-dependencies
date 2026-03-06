package io.github.lujiafa.houtu.util.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * @author lujiafa
 * @date 2018年4月17日
 * @Description 64进制规则字符串工具类
 */
public class CodeUtils {
    private static final Logger logger = LoggerFactory.getLogger(CodeUtils.class);
    private static final char[] CHAR64 = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '_', '-'};
    private static final String DATA_REGEX = "^[-_0-9A-Za-z]{32}$";
    private static final byte[] DEFAULT_SALT = {0x1A, 0x2B, 0x3C, 0x4D};
    private static final byte[] MAC = new byte[6];
    private static final int PROCESS_ID;
    private static final ReentrantLock Lock = new ReentrantLock();

    static {
        List<byte[]> macByteList = SystemUtils.getMacByteList();
        if (macByteList.size() > 0) {
            byte[] bytes = macByteList.get(0);
            System.arraycopy(bytes, 0, MAC, 0, 6);
        }
        // 限定最大PID为32位。实际参考/proc/sys/kernel/pid_max
        PROCESS_ID = SystemUtils.getProcessId() & 0xFFFFFFFF;
    }

    static volatile AtomicIncr incr = new AtomicIncr(System.currentTimeMillis());

    static long millisecondIncr(long millisecond) {
        long val = incr.incr(millisecond);
        if (val > 0)
            return val;
        Lock.lock();
        try {
            if ((val = incr.incr(millisecond)) > 0)
                return val;
            return (incr = new AtomicIncr(millisecond)).incr(millisecond);
        } finally {
            Lock.unlock();
        }
    }

    static byte[] generateVerifyBytes(byte[] sourceBytes, String slat) {
        byte[] verifyBytes = new byte[4];
        System.arraycopy(sourceBytes, 0, verifyBytes, 0, 4);
        long verifyCode = (verifyBytes[0] & 0xFF) << 24
                | (verifyBytes[1] & 0xFF) << 16
                | (verifyBytes[2] & 0xFF) << 8
                | verifyBytes[3] & 0xFF;
        for (int i = 1; i <= 4; i++) {
            int n = i * 4;
            verifyCode = ~(verifyCode ^ ((sourceBytes[n] & 0xFF) << 24 | (sourceBytes[n + 1] & 0xFF) << 16 | (sourceBytes[n + 2] & 0xFF) << 8 | (sourceBytes[n + 3] & 0xFF)));
        }
        byte[] slatBytes = DEFAULT_SALT;
        if (slat != null && slat.length() > 0) {
            slatBytes = slat.getBytes(StandardCharsets.UTF_8);
            int m = slatBytes.length % 4;
            if (m > 0) {
                byte[] temp = slatBytes;
                slatBytes = new byte[slatBytes.length + 4 - m];
                System.arraycopy(temp, 0, slatBytes, 0, temp.length);
                System.arraycopy(DEFAULT_SALT, m, slatBytes, temp.length, 4 - m);
            }
        }
        int slatRound = slatBytes.length / 4; // slat分段计算轮次
        for (int i = 0; i < slatRound; i++) {
            int n = i * 4;
            verifyCode = ~(verifyCode ^ ((slatBytes[n] & 0xFF) << 24 | (slatBytes[n + 1] & 0xFF) << 16 | (slatBytes[n + 2] & 0xFF) << 8 | (slatBytes[n + 3] & 0xFF)));
        }
        verifyBytes[0] = (byte) (verifyCode >> 24 & 0xFF);
        verifyBytes[1] = (byte) (verifyCode >> 16 & 0xFF);
        verifyBytes[2] = (byte) (verifyCode >> 8 & 0xFF);
        verifyBytes[3] = (byte) (verifyCode & 0xFF);
        return verifyBytes;
    }

    /**
     * <p>生成64进制唯一携带规则编码，具备唯一性、校验性、自主性</p>
     * <p>规则编码由32字符组成（即192 bit=24 byte），其生成规则：</p>
     * 192 bit = 44(timestamp) + 36bit(incr) + 48bit(mac) + 32bit(procId) + 32bit(verify_code)
     *
     * @return 唯一编码
     */
    public static String get() {
        return get(null);
    }

    /**
     * <p>生成64进制唯一携带规则编码，具备唯一性、校验性、自主性</p>
     * <p>规则编码由32字符组成（即192 bit=24 byte），其生成规则：</p>
     * 192 bit = 44(timestamp) + 36bit(incr) + 48bit(mac) + 32bit(procId) + 32bit(verify_code)
     *
     * @param slat 密钥、盐
     * @return 唯一编码
     */
    public static String get(String slat) {
        long timestamp = 0;
        long incr = 0;
        for (int i = 0; i < 100; i++) {
            timestamp = System.currentTimeMillis();
            incr = millisecondIncr(timestamp);
            if (incr <= 0xFFFFFFFFFL)
                break;
            if (i == 99)
                throw new RuntimeException("incr overflow");
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        int processId = PROCESS_ID;
        byte[] b = new byte[24];
        b[0] = (byte) (timestamp >> 36);
        b[1] = (byte) (timestamp >> 28);
        b[2] = (byte) (timestamp >> 20);
        b[3] = (byte) (timestamp >> 12);
        b[4] = (byte) (timestamp >> 4);
        b[5] = (byte) (timestamp << 4 | incr >> 32);
        b[6] = (byte) (incr >> 24);
        b[7] = (byte) (incr >> 16);
        b[8] = (byte) (incr >> 8);
        b[9] = (byte) incr;
        System.arraycopy(MAC, 0, b, 10, 6);
        b[16] = (byte) (processId >> 24);
        b[17] = (byte) (processId >> 16);
        b[18] = (byte) (processId >> 8);
        b[19] = (byte) processId;
        System.arraycopy(generateVerifyBytes(b, slat), 0, b, 20, 4);
        byte[] confuse = {b[12], b[18]};
        b[12] = b[21];
        b[18] = b[22];
        System.arraycopy(confuse, 0, b, 21, 2);
        char[] chars = new char[32];
        IntStream.range(0, 8).parallel().forEach(i -> {
            int m = i * 3;
            int n = i * 4;
            chars[n] = CHAR64[b[m] >> 2 & 0x3F];
            chars[n + 1] = CHAR64[((b[m] << 4 | (b[m + 1] & 0xFF) >> 4) & 0x3F)];
            chars[n + 2] = CHAR64[((b[m + 1] << 2 | (b[m + 2] & 0xFF) >> 6) & 0x3F)];
            chars[n + 3] = CHAR64[b[m + 2] & 0x3F];
        });
        return new String(chars);
    }

    /**
     * 解析并验证字符串
     *
     * @param rule 唯一编码
     * @return 解析结果
     */
    public static RuleData parse(String rule) {
        return parse(rule, null);
    }

    /**
     * 解析并验证字符串
     *
     * @param rule 唯一编码
     * @param slat 密钥、盐
     * @return 解析结果
     */
    public static RuleData parse(String rule, String slat) {
        if (rule == null || !rule.matches(DATA_REGEX))
            return new RuleData();
        char[] chars = rule.toCharArray();
        int[] charIndexArray = new int[32];
        IntStream.range(0, 32).parallel().forEach(i -> {
            charIndexArray[i] = IntStream.range(0, 64).parallel().filter(j -> CHAR64[j] == chars[i]).findAny().getAsInt();
        });
        byte[] b = new byte[24];
        IntStream.range(0, 8).parallel().forEach(i -> {
            int m = i * 3;
            int n = i * 4;
            b[m] = (byte) (charIndexArray[n] << 2 | charIndexArray[n + 1] >> 4);
            b[m + 1] = (byte) (charIndexArray[n + 1] << 4 | charIndexArray[n + 2] >> 2);
            b[m + 2] = (byte) (charIndexArray[n + 2] << 6 | charIndexArray[n + 3]);
        });
        byte[] resume = {b[12], b[18]};
        b[12] = b[21];
        b[18] = b[22];
        System.arraycopy(resume, 0, b, 21, 2);
        byte[] verifyBytes = generateVerifyBytes(b, slat);
        if (b[20] != verifyBytes[0] || b[21] != verifyBytes[1] || b[22] != verifyBytes[2] || b[23] != verifyBytes[3])
            return new RuleData();
        long timestamp = (b[0] & 0xFF) << 36 | (b[1] & 0xFF) << 28 | (b[2] & 0xFF) << 20 | (b[3] & 0xFF) << 12 | (b[4] & 0xFF) << 4 | (b[5] & 0xFF) >> 4;
        long incr = (b[5] & 0x0F) << 32 | (b[6] & 0xFF) << 24 | (b[7] & 0xFF) << 16 | (b[8] & 0xFF) << 8 | (b[9] & 0xFF);
        byte[] mac = new byte[6];
        System.arraycopy(b, 10, mac, 0, 6);
        long processId = (b[16] & 0xFF) << 24 | (b[17] & 0xFF) << 16 | (b[18] & 0xFF) << 8 | (b[19] & 0xFF);
        return new RuleData(true, timestamp, incr, mac, (int) processId);
    }

    static class AtomicIncr {
        public AtomicLong incr = new AtomicLong(0);
        private long timestamp;

        AtomicIncr(long timestamp) {
            this.timestamp = timestamp;
        }

        public long incr(long timestamp) {
            if (this.timestamp == timestamp)
                return incr.incrementAndGet();
            return 0L;
        }
    }

    public static class RuleData {
        RuleData() {}

        RuleData(boolean success, Long timestamp, Long incr, byte[] mac, Integer processId) {
            this.success = success;
            this.timestamp = timestamp;
            this.incr = incr;
            this.mac = mac;
            this.processId = processId;
        }

        private boolean success;
        private long timestamp;
        private Long incr;
        private byte[] mac;
        private Integer processId;

        public boolean isSuccess() {
            return success;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Long getIncr() {
            return incr;
        }

        public byte[] getMac() {
            return mac;
        }

        public Integer getProcessId() {
            return processId;
        }
    }
}