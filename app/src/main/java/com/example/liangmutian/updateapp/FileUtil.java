package com.example.liangmutian.updateapp;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class FileUtil {
	private static final String TAG = "FileUtil";
	/**
	 *
	 */
	public static String ROOT_PATH = null;

	/**
	 * 公共资源下载缓存文件路径
	 */
	public static String RESOURCE_COMMON_CACHE_PATH = null;
	/**
	 * 用户文件资源下载缓存文件路径
	 */
	public static String RESOURCE_USER_FILE_CACHE_PATH = null;
	/**
	 * 用户图片资源下载缓存文件路径
	 */
	public static String RESOURCE_USER_IMAGE_CACHE_PATH = "img";
	/**
	 * 用户DB路径
	 */
	public static String RESOURCE_USER_DB_CACHE_PATH = null;


	/**
	 * 初始化应用存储路径
	 *
	 * @param context
	 */
	public static boolean initAppStoragePath(Context context) {
		FileUtil.ROOT_PATH = getAppStorageDir(context);
		if (FileUtil.ROOT_PATH != null) {
			FileUtil.RESOURCE_COMMON_CACHE_PATH = FileUtil.ROOT_PATH
					+ File.separator + ".resource";
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 用户个人文件
	 *
	 * @param userName
	 */
	public static void initLoginStoragePath(String userName) {
		if (FileUtil.ROOT_PATH != null) {// TODO 资源目录开发中先不建隐藏目录。
			FileUtil.RESOURCE_USER_DB_CACHE_PATH = FileUtil.ROOT_PATH
					+ File.separator + userName + File.separator + "database";
			FileUtil.RESOURCE_USER_FILE_CACHE_PATH = FileUtil.ROOT_PATH
					+ File.separator + userName + File.separator + "file";
			FileUtil.RESOURCE_USER_IMAGE_CACHE_PATH = FileUtil.ROOT_PATH
					+ File.separator + userName + File.separator + "image";
		}
	}

	/**
	 * 获取APP 存储的路径
	 *
	 * @param context
	 * @return
	 */
	public static String getAppStorageDir(Context context) {
		if (existSDCard()) {
			// 获取Android程序在Sd上的保存目录约定 当程序卸载时，系统会自动删除。
			File f = context.getExternalFilesDir(null);
			// 如果约定目录不存在
			if (f == null) {
				// 获取外部存储目录即 SDCard
				String storageDirectory = Environment
						.getExternalStorageDirectory().toString();
				File fDir = new File(storageDirectory);
				// 如果sdcard目录不可用
				if (!fDir.canWrite()) {
					// 获取可用
					storageDirectory = getSDCardDir();
					if (storageDirectory != null) {
						storageDirectory = storageDirectory + File.separator
								+ "mskyhaha" + File.separator
								+ context.getApplicationInfo().packageName;
						return storageDirectory;

					} else {
						return context.getCacheDir().toString();
					}
				} else {
					storageDirectory = storageDirectory + File.separator
							+ "mskyhaha" + File.separator
							+ context.getApplicationInfo().packageName;
					return storageDirectory;
				}
			} else {
				String storageDirectory = f.getAbsolutePath();
				return storageDirectory;
			}
		} else {
			return null;
		}
	}

	/**
	 * 获取一个可用的存储路径（可能是内置的存储路径）
	 *
	 * @return 可用的存储路径
	 */
	private static String getSDCardDir() {
		String pathDir = null;
		// 先获取内置sdcard路径
		File sdfile = Environment.getExternalStorageDirectory();
		// 获取内置sdcard的父路径
		File parentFile = sdfile.getParentFile();
		// 列出该父目录下的所有路径
		File[] listFiles = parentFile.listFiles();
		// 如果子路径可以写 就是拓展卡（包含内置的和外置的）

		long freeSizeMax = 0L;
		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].canWrite()) {
				// listFiles[i]就是SD卡路径
				String tempPathDir = listFiles[i].getAbsolutePath();
				long tempSize = getSDFreeSize(tempPathDir);
				if (tempSize > freeSizeMax) {
					freeSizeMax = tempSize;
					pathDir = tempPathDir;
				}
			}
		}
		return pathDir;
	}

	/**
	 * 获取指定目录剩余空间
	 *
	 * @return
	 * @author EX-LIJINHUA001
	 * @date 2013-6-7
	 */
	public static long getSDFreeSize(String filePath) {

		android.os.StatFs statfs = new android.os.StatFs(filePath);

		long nBlocSize = statfs.getBlockSize(); // 获取SDCard上每个block的SIZE

		long nAvailaBlock = statfs.getAvailableBlocks(); // 获取可供程序使用的Block的数量

		long nSDFreeSize = nAvailaBlock * nBlocSize; // 计算 SDCard
		// 剩余大小B
		return nSDFreeSize;
	}

	/**
	 * 清除缓存文件
	 */
	public static void clearAllData(Context context) {
		File file = new File("/data/data/" + context.getPackageName());
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (!f.getName().equals("lib")) {
				deleteFile(f);
			}
		}
		File file2 = new File(ROOT_PATH);
		deleteFile(file2);
	}

	/**
	 * 删除文件
	 */
	public static void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files.length > 1) {
				for (int i = 0; i < files.length; i++) {

					deleteFile(files[i]);
				}
			}
			file.delete();
		} else {
			file.delete();
		}
	}

	public static interface ClearDataCallback {
		public void clearDatafinish();
	}


	/**
	 * 判断SD是否可以
	 *
	 * @return
	 */
	public static boolean existSDCard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	/**
	 * 创建根目录
	 *
	 * @param path 目录路径
	 */
	public static void createDirFile(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	/**
	 * 创建文件
	 *
	 * @param path 文件路径
	 * @return 创建的文件
	 */
	public static File createNewFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				return null;
			}
		}
		return file;
	}



	/**
	 * 删除文件夹
	 *
	 * @param folderPath 文件夹的路径
	 */
	public static void delFolder(String folderPath) {
		delAllFile(folderPath);
		String filePath = folderPath;
		filePath = filePath.toString();
		File myFilePath = new File(filePath);
		myFilePath.delete();
	}

	/**
	 * 删除文件
	 *
	 * @param path 文件的路径
	 */
	public static void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);
				delFolder(path + "/" + tempList[i]);
			}
		}
	}

	/**
	 * 获取文件的Uri
	 *
	 * @param path 文件的路径
	 * @return
	 */
	public static Uri getUriFromFile(String path) {
		File file = new File(path);
		return Uri.fromFile(file);
	}

	/**
	 * 换算文件大小
	 *
	 * @param size
	 * @return
	 */
	public static String formatFileSize(long size) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "未知大小";
		if (size < 1024) {
			fileSizeString = df.format((double) size) + "B";
		} else if (size < 1048576) {
			fileSizeString = df.format((double) size / 1024) + "K";
		} else if (size < 1073741824) {
			fileSizeString = df.format((double) size / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) size / 1073741824) + "G";
		}
		return fileSizeString;
	}

	public static String getPathFromUri(Context context,Uri data){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			return getPathFromUri2(context,data);
		}else{
			return getPathFromUri1(context,data);
		}
	}

	public static String getPathFromUri1(Context context, Uri data) {
		String filename = "";
		if (data.getScheme().toString().compareTo("content") == 0) {
			Cursor cursor =context.getContentResolver().query(data,
					new String[]{MediaStore.Audio.Media.DATA}, null, null, null);
			if (cursor.moveToFirst()) {
				filename = cursor.getString(0);
			}
		} else if (data.getScheme().toString().compareTo("file") == 0)         //file:///开头的uri
		{
			filename = data.toString();
			filename = data.toString().replace("file://", "");
			//替换file://
			if (!filename.startsWith("/mnt")) {
				//加上"/mnt"头
				filename += "/mnt";
			}
		}
		return filename;
	}

	/**
	 * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
	 */
	public static String getPathFromUri2(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}


	public static File updateDir = null;
	public static File updateFile = null;

	/**
	 * 保存升级APK的目录
	 */
	public static final String mskyApp = "msky";

	public static boolean isCreateFileSucess;

	/**
	 * 方法描述：createFile方法
	 *
	 * @param String app_name
	 * @return
	 * @see FileUtil
	 */
	public static void createFile(String app_name) {

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			isCreateFileSucess = true;

			updateDir = new File(Environment.getExternalStorageDirectory()
					+ "/" + mskyApp + "/");
			updateFile = new File(updateDir + "/" + app_name + ".apk");

			if (!updateDir.exists()) {
				updateDir.mkdirs();
			}
			if (!updateFile.exists()) {
				try {
					updateFile.createNewFile();
				} catch (IOException e) {
					isCreateFileSucess = false;
					e.printStackTrace();
				}
			}

		} else {
			isCreateFileSucess = false;
		}
	}


	public static void createFile(Context context,String app_name) {

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			isCreateFileSucess = true;

			updateDir = new File(context.getApplicationContext().getFilesDir().getAbsolutePath()
					+ "/" + mskyApp + "/");
			updateFile = new File(updateDir + "/" + app_name + ".apk");

			if (!updateDir.exists()) {
				updateDir.mkdirs();
			}
			if (!updateFile.exists()) {
				try {
					updateFile.createNewFile();
				} catch (IOException e) {
					isCreateFileSucess = false;
					e.printStackTrace();
				}
			}

		} else {
			isCreateFileSucess = false;
		}
	}
}
