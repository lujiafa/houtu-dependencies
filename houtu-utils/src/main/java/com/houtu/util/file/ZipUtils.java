package com.houtu.util.file;

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
	 * @Description:解压ZIP文件，解压到当前zip相同目录下
	 * @param zipFilePath
	 *            zip文件路径
	 */
	public static void decompressZip(String zipFilePath) {
		decompressZip(zipFilePath, null, null);
	}
	
	/**
	 * @Description:解压ZIP文件，解压到指定目录
	 * @param zipFilePath
	 *            zip文件路径
	 * @param targetPath
	 *            解压缩到的位置，如果为null或空字符串则默认解压缩到跟zip包同目录跟zip包同名的文件夹下
	 */
	public static void decompressZip(String zipFilePath, String targetPath) {
		decompressZip(zipFilePath, targetPath, null);
	}

	/**
	 * @Description:解压ZIP文件，解压到指定目录
	 * @param zipFilePath
	 *            zip文件路径
	 * @param targetPath
	 *            解压缩到的位置，如果为null或空字符串则默认解压缩到跟zip包同目录跟zip包同名的文件夹下
	 * @param charset
	 *            编码方式
	 */
	@SuppressWarnings("resource")
	public static void decompressZip(String zipFilePath, String targetPath, Charset charset) {
		InputStream is = null;
		OutputStream os = null;
		try {
			charset = charset == null ? StandardCharsets.UTF_8 : charset;
			File zip = new File(zipFilePath);
			// 创建ZipFile对象并指定编码
			ZipFile zipFile = new ZipFile(zip, charset);
			if (targetPath == null || "".equals(targetPath)) {
				targetPath = zip.getParent() + File.separator;
			} else if (!targetPath.endsWith(File.separator)) {
				targetPath += File.separator;
			}
			Enumeration<?> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				File file = new File(targetPath + entry.getName());
				if (entry.isDirectory()) {
					// 若目录不存在，则创建
					if (!file.exists())
						file.mkdirs();
				} else {
					File parent = file.getParentFile();
					// 若上级目录不存在，则创建
					if (!parent.exists())
						parent.mkdirs();
					is = zipFile.getInputStream(entry);
					os = new FileOutputStream(file);
					byte[] b = new byte[1024];
					int len = 0;
					while ((len = is.read(b)) > 0) {
						os.write(b, 0, len);
					}
					os.flush();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * @Description:文件压缩 ，采用第三方jar，解决压缩文件中含中文名文件时乱码问题
	 * @param targetZipFilePath
	 *            压缩为zip后的文件路径
	 * @param sourceFilePaths
	 *            需压缩的文件路径
	 */
	public static void compressZip(String targetZipFilePath, String... sourceFilePaths) {
		compressZip(targetZipFilePath, StandardCharsets.UTF_8, true, sourceFilePaths);
	}
	
	/**
	 * @Description:文件压缩 ，采用第三方jar，解决压缩文件中含中文名文件时乱码问题
	 * @param targetZipFilePath
	 *            压缩为zip后的文件路径
	 * @param sourceRootName 是否包含源文件根目录结构
	 * @param sourceFilePaths
	 *            需压缩的文件路径
	 */
	public static void compressZip(String targetZipFilePath, boolean sourceRootName, String... sourceFilePaths) {
		compressZip(targetZipFilePath, StandardCharsets.UTF_8, sourceRootName, sourceFilePaths);
	}
	
	/**
	 * @Description:文件压缩 ，采用第三方jar，解决压缩文件中含中文名文件时乱码问题
	 * @param sourceFilePaths
	 *            需压缩的文件路径
	 * @param charset
	 *            编码方式
	 * @param sourceRootName 是否包含源文件根目录结构
	 * @param targetZipFilePath
	 *            压缩为zip后的文件路径
	 */
	public static void compressZip(String targetZipFilePath, Charset charset, boolean sourceRootName, String... sourceFilePaths) {
		ZipOutputStream zos = null;
		try {
			if (sourceFilePaths != null) {
				List<File> sourceFileList = Arrays.stream(sourceFilePaths)
						.filter(p -> p != null)
						.map(p -> new File(p))
						.filter(p -> p.exists())
						.collect(Collectors.toList());
				charset = charset == null ? StandardCharsets.UTF_8 : charset;
				if (sourceFileList.size() > 0) {
					zos = new ZipOutputStream(new FileOutputStream(targetZipFilePath), charset);
					for (File file : sourceFileList) {
						compressZipHelper(zos, file, sourceRootName ? file.getName() : "");// 调用压缩递归方法
					}
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				if (zos != null) {
					zos.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 文件或文件夹压缩工具方法
	 * 
	 * @param zos
	 *            ZipOutputStream
	 * @param file
	 *            要压缩的文件对象
	 * @param basePath
	 *            相对压缩文件zip的位置
	 * @throws Exception
	 */
	private static void compressZipHelper(ZipOutputStream zos, File file, String basePath) {
		try {
			if (file.isDirectory()) {// 判断是否为目录
				File[] files = file.listFiles();
				if (basePath.length() != 0) {
					basePath += "/";
					zos.putNextEntry(new ZipEntry(basePath));
				}
				for (File f : files) {
					compressZipHelper(zos, f, basePath + f.getName());
				}
			} else if (file.isFile()) {
				zos.putNextEntry(new ZipEntry(basePath));
				InputStream is = new FileInputStream(file);
				byte[] b = new byte[1024];
				int len = 0;
				while ((len = is.read(b)) > 0) {
					zos.write(b, 0, len);
				}
				is.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}