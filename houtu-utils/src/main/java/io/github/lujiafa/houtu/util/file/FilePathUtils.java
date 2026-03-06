package io.github.lujiafa.houtu.util.file;

import io.github.lujiafa.houtu.util.constant.CharConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 文件名、路径相关工具类
 */
public class FilePathUtils {

    /**
     * 标准化输出文件路径，以正斜杠“/”作为目录分隔符
     * 示例：
     * "d:\office\\work/project//resources/" -> "d:/office/work/project/resources/"
     * "d:\office\\work/project//resources" -> "d:/office/work/project/resources"
     *
     * @param path 文件路径
     * @return 标准路径
     */
    public static String standard(String path) {
        if (path != null) {
            path = path.replaceAll("\\\\+", CharConstant.SLASH).replaceAll("/+", CharConstant.SLASH);
        }
        return path;
    }

    /**
     * 拼接文件路径
     * 注：
     * 1. 不能所有参数都为null
     * 2. 保留参数所有结构，仅将反斜杠"\"转为斜杠"/"
     * 3. 去除前后多余空白字符和中间多余斜杠分隔符，仅中间的多余斜杠
     *
     * @param basePath 基础路径
     * @param subPaths 子路径数组
     * @return 拼接路径
     */
    public static String concat(String basePath, String... subPaths) {
        if (basePath == null
                && (subPaths == null || subPaths.length == 0 || Arrays.stream(subPaths).allMatch(sp -> sp == null))) {
            throw new IllegalArgumentException("参数不能同时为空");
        }
        final StringBuilder targetPath = new StringBuilder(StringUtils.isBlank(basePath) ? CharConstant.EMPTY : basePath.trim());
        Arrays.stream(subPaths).forEach(sp -> {
            if (sp != null) {
                if (targetPath.length() > 0) {
                    targetPath.append(CharConstant.SLASH);
                }
                targetPath.append(sp);
            }
        });
        return standard(targetPath.toString());
    }

    /**
     * 解析文件路径字符串为数组，数组每个索引存储单元对应一级目录，按顺序返回
     * 注：会去除尾部空路径/。如"e:/a/b/c/"实际会按"e:/a/b/c"解析得到["e:", "a", "b", "c"]
     *
     * @param path 文件路径
     * @return 解析文件目录数组，按顺序排列
     */
    public static String[] parse(String path) {
        if (path == null) {
            return new String[0];
        }
        String standardPath = standard(path.trim());
        if (standardPath.endsWith(CharConstant.SLASH)) {
            standardPath = standardPath.substring(0, standardPath.length() - 1);
        }
        return standardPath.split(CharConstant.SLASH);
    }

}
