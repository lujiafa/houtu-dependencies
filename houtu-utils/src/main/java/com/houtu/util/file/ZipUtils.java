package com.houtu.util.file;

import com.houtu.util.constant.CharConstant;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author lujiafa
 * @Description:zip文件压缩、解压工具类
 */
public final class ZipUtils {

    /**
     * @param zipFilePath zip文件路径【M】
     * @Description:解压ZIP文件，解压到当前zip相同目录下
     */
    public static void decompressZip(String zipFilePath) throws IOException {
        decompressZip(zipFilePath, null, null);
    }

    /**
     * @param zipFilePath         zip文件路径【M】
     * @param targetDirectoryPath 解压缩到的位置，如果为null或空字符串则默认解压缩到跟zip包同目录跟zip包同名的文件夹下【O】
     * @Description:解压ZIP文件，解压到指定目录
     */
    public static void decompressZip(String zipFilePath, String targetDirectoryPath) throws IOException {
        decompressZip(zipFilePath, targetDirectoryPath, null);
    }

    /**
     * @param zipFilePath         zip文件路径【M】
     * @param targetDirectoryPath 解压缩到的位置，如果为null或空字符串则默认解压缩到跟zip包同目录跟zip包同名的文件夹下【O】
     * @param charset             编码方式
     * @Description:解压ZIP文件，解压到指定目录
     */
    @SuppressWarnings("resource")
    public static void decompressZip(String zipFilePath, String targetDirectoryPath, Charset charset) throws IOException {
            charset = charset == null ? StandardCharsets.UTF_8 : charset;
            File zip = new File(zipFilePath);
            Assert.isTrue(zip.exists(), "zipFilePath is not exists");
            Assert.isTrue(zip.isFile(), "zipFilePath is not a zip file path");
            // 创建ZipFile对象并指定编码
            ZipFile zipFile = new ZipFile(zip, charset);
            if (StringUtils.isEmpty(targetDirectoryPath)) {
                targetDirectoryPath = zip.getParent();
            }
            File targetDirectoryFile = new File(targetDirectoryPath);
            if (targetDirectoryFile.exists()) {
                Assert.isTrue(targetDirectoryFile.isDirectory(), "targetDirectoryPath is not a directory");
            } else {
                targetDirectoryFile.mkdirs();
            }
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File file = new File(FilePathUtils.concat(targetDirectoryPath, entry.getName()));
                if (entry.isDirectory()) {
                    // 若目录不存在，则创建
                    if (!file.exists())
                        file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    // 若上级目录不存在，则创建
                    if (!parent.exists())
                        parent.mkdirs();
                    try (InputStream is = zipFile.getInputStream(entry); OutputStream os = new FileOutputStream(file)) {
                        is.transferTo(os);
                        os.flush();
                    }
                }
            }
    }

    /**
     * @param sourceFilePaths   待压缩的文件路径（文件或文件目录）【M】
     * @param targetZipFilePath 压缩为zip后的文件路径【M】
     * @Description:文件压缩 ，采用第三方jar，解决压缩文件中含中文名文件时乱码问题
     */
    public static void compressZip(String targetZipFilePath, String... sourceFilePaths) throws IOException {
        compressZip(targetZipFilePath, StandardCharsets.UTF_8, true, sourceFilePaths);
    }

    /**
     * @param sourceFilePaths   待压缩的文件路径（文件或文件目录）【M】
     * @param sourceRootName    是否包含源文件根目录结构【M】
     * @param targetZipFilePath 压缩为zip后的文件路径【M】
     * @Description:文件压缩 ，采用第三方jar，解决压缩文件中含中文名文件时乱码问题
     */
    public static void compressZip(String targetZipFilePath, boolean sourceRootName, String... sourceFilePaths) throws IOException {
        compressZip(targetZipFilePath, StandardCharsets.UTF_8, sourceRootName, sourceFilePaths);
    }

    /**
     * @param sourceFilePaths   待压缩的文件路径（文件或文件目录）【M】
     * @param charset           编码方式【O】
     * @param sourceRootName    是否包含源文件根目录结构【M】
     * @param targetZipFilePath 压缩为zip后的文件路径【M】
     * @Description:文件压缩 ，采用第三方jar，解决压缩文件中含中文名文件时乱码问题
     */
    public static void compressZip(String targetZipFilePath, Charset charset, boolean sourceRootName, String... sourceFilePaths) throws IOException {
        Assert.isTrue(!StringUtils.isEmpty(targetZipFilePath), "targetZipFilePath is null or empty");
        Assert.isTrue(sourceFilePaths != null && sourceFilePaths.length > 0, "sourceFilePaths is null or empty");
        List<File> sourceFileList = Arrays.stream(sourceFilePaths)
                .filter(p -> p != null)
                .map(p -> new File(p))
                .filter(p -> p.exists())
                .collect(Collectors.toList());
        Assert.isTrue(!sourceFileList.isEmpty(), "sourceFilePaths is not empty");
        charset = charset == null ? StandardCharsets.UTF_8 : charset;
        File targetZipFile = new File(targetZipFilePath);
        File targetZipFileParent = targetZipFile.getParentFile();
        if (!targetZipFileParent.exists())
            targetZipFileParent.mkdirs();
        if (!targetZipFile.exists())
            targetZipFile.createNewFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetZipFile), charset)) {
            for (File f : sourceFileList) {
                compressZip(zos, f, null);// 调用压缩递归方法
            }
        }
    }

    /**
     * 递归压缩
     * @param zos 输出流【M】
     * @param file 待压文件或文件夹【M】
     * @param basePath 压缩文件前缀目录【O】
     * @throws IOException
     */
    static void compressZip(ZipOutputStream zos, File file, String basePath) throws IOException {
        if (file.isDirectory()) {// 判断是否为目录
            zos.putNextEntry(new ZipEntry(basePath = FilePathUtils.concat(basePath == null ? CharConstant.EMPTY : basePath, file.getName(), CharConstant.SLASH)));
            File[] files = file.listFiles();
            for (File f : files) {
                compressZip(zos, f, basePath);
            }
        } else if (file.isFile()) {
            zos.putNextEntry(new ZipEntry(basePath = FilePathUtils.concat(basePath == null ? CharConstant.EMPTY : basePath, file.getName())));
            try (InputStream is = new FileInputStream(file)) {
                byte[] b = new byte[1024];
                int len = 0;
                while ((len = is.read(b)) > 0) {
                    zos.write(b, 0, len);
                }
            }
        }
    }

}