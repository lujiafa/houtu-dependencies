package io.github.lujiafa.houtu.data.security.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 安全加解密处理器
 * <p>
 *     默认实现为空，需要实现时，重写对应方法即可。
 * </p>
 */
public interface SecurityProcessor {

    /**
     * 单个加密处理方法
     * <p>
     *     注意：在某一方法代理加密时，同一明文多次加密后密文可以不同，但不同明文生成的加密密文值必须不同，否则可能影响数据恢复。
     *     <br>
     *     如：仅做Hash时，MD5就可能多个明文对应一个密文（不可取），建议换SHA
     *     <br>
     * </p>
     * @param method 代理方法
     * @param original 原始数据
     * @return 加密数据
     */
    default String encrypt(Method method, String original) {
        return original;
    }

    /**
     * 批量加密处理方法
     * @param method 代理方法
     * @param originals 原始数据集合
     * @return 加密数据集合
     */
    default Map<String, String> encrypt(Method method, Set<String> originals) {
        Map<String, String> encrypted = new HashMap<String, String>();
        if (originals != null) {
            for (String original : originals) {
                encrypted.put(original, encrypt(method, original));
            }
        }
        return encrypted;
    }

    /**
     * 单个解密处理方法
     * @param method 代理方法
     * @param encrypted 加密数据
     * @return 解密数据
     */
    default String decrypt(Method method, String encrypted) {
        return encrypted;
    }

    /**
     * 批量解密处理方法
     * @param method 代理方法
     * @param encrypted 加密数据集合
     * @return 解密数据集合
     */
    default Map<String, String> decrypt(Method method, Set<String> encrypted) {
        Map<String, String> decrypted = new HashMap<String, String>();
        if (encrypted != null) {
            for (String original : encrypted) {
                decrypted.put(original, decrypt(method, original));
            }
        }
        return decrypted;
    }

}
