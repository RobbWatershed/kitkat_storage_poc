package com.example.platypus.kitkat_storage_poc.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.example.platypus.kitkat_storage_poc.BuildConfig;
import com.example.platypus.kitkat_storage_poc.TestApp;

import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

/**
 * Created by avluis on 08/05/2016.
 * File related utility class
 */
public class FileHelper {
    // Note that many devices will report true (there are no guarantees of this being 'external')
    public static final boolean isSdPresent = getExternalStorageState().equals(MEDIA_MOUNTED);

    private static final String AUTHORIZED_CHARS = "[^a-zA-Z0-9.-]";

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.FileProvider";


    /**
     * Determine if a file is on external sd card. (Kitkat+)
     *
     * @param file The file.
     * @return true if on external sd card.
     */
    public static boolean isOnExtSdCard(final File file) {
        return getExtSdCardFolder(file) != null;
    }

    /**
     * Determine the main folder of the external SD card containing the given file. (Kitkat+)
     *
     * @param file The file.
     * @return The main folder of the external SD card containing this file,
     * if the file is on an SD card. Otherwise, null is returned.
     */
    public static String getExtSdCardFolder(final File file) {
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        } catch (IOException e) {
            return null;
        }

        return null;
    }

    /**
     * Get a list of external SD card paths. (Kitkat+)
     *
     * @return A list of external SD card paths.
     */
    public static String[] getExtSdCardPaths() {
        Context context = TestApp.getAppContext();
        List<String> paths = new ArrayList<>();
        for (File file : ContextCompat.getExternalFilesDirs(context, "external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Timber.w("Unexpected external file dir: %s", file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }

        return paths.toArray(new String[0]);
    }

    /**
     * Check if a file or directory is writable.
     * Detects write issues on external SD card.
     *
     * @param file The file or directory.
     * @return true if the file or directory is writable.
     */
    public static boolean isWritable(@NonNull final File file) {

        if (file.isDirectory()) return isDirectoryWritable(file);
        else return isFileWritable(file);
    }

    /**
     * Check if a directory is writable.
     * Detects write issues on external SD card.
     *
     * @param file The directory.
     * @return true if the directory is writable.
     */
    private static boolean isDirectoryWritable(@NonNull final File file) {
        File testFile = new File(file, "test.txt");

        boolean hasPermission;

        try {
            hasPermission = FileUtil.makeFile(testFile, true);
            if (hasPermission)
                try (OutputStream output = FileHelper.getOutputStream(testFile)) {
                    output.write("test".getBytes());
                    sync(output);
                    output.flush();
                } catch (NullPointerException npe) {
                    Timber.e(npe, "Invalid Stream");
                    hasPermission = false;
                } catch (IOException e) {
                    Timber.e(e, "IOException while checking permissions on %s", file.getAbsolutePath());
                    hasPermission = false;
                }
        } finally {
            if (testFile.exists()) {
                removeFile(testFile);
            }
        }
        return hasPermission;
    }

    /**
     * Check if a file is writable.
     * Detects write issues on external SD card.
     *
     * @param file The file.
     * @return true if the file is writable.
     */
    private static boolean isFileWritable(@NonNull final File file) {
        if (!file.canWrite()) return false;

        // Ensure that it is indeed writable by opening an output stream
        try {
            FileOutputStream output = FileUtils.openOutputStream(file);
            output.close();
        } catch (IOException e) {
            return false;
        }

        // Ensure that file is not created during this process.
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        return true;
    }

    /**
     * Method ensures file creation from stream.
     *
     * @param stream - OutputStream
     * @return true if all OK.
     */
    static boolean sync(@NonNull final OutputStream stream) {
        return (stream instanceof FileOutputStream) && FileUtil.sync((FileOutputStream) stream);
    }

    /**
     * Get OutputStream from file.
     *
     * @param target The file.
     * @return FileOutputStream.
     */
    static OutputStream getOutputStream(@NonNull final File target) throws IOException {
        return FileUtil.getOutputStream(target);
    }

    static OutputStream getOutputStream(@NonNull final DocumentFile target) throws IOException {
        return FileUtil.getOutputStream(target);
    }

    static InputStream getInputStream(@NonNull final File target) throws IOException {
        return FileUtil.getInputStream(target);
    }

    /**
     * Create a folder.
     *
     * @param file The folder to be created.
     * @return true if creation was successful or the folder already exists
     */
    public static boolean createDirectory(@NonNull File file) {
        return FileUtil.makeDir(file);
    }

    /**
     * Delete a file.
     *
     * @param target The file.
     * @return true if deleted successfully.
     */
    public static boolean removeFile(File target) {
        return FileUtil.deleteFile(target);
    }

    /**
     * Delete files in a target directory.
     *
     * @param target The folder.
     * @return true if cleaned successfully.
     */
    private static boolean cleanDirectory(@NonNull File target) {
        try {
            return tryCleanDirectory(target);
        } catch (Exception e) {
            Timber.e(e, "Failed to clean directory");
            return false;
        }
    }

    /**
     * Cleans a directory without deleting it.
     * <p>
     * Custom substitute for commons.io.FileUtils.cleanDirectory that supports devices without File.toPath
     *
     * @param directory directory to clean
     * @return true if directory has been successfully cleaned
     * @throws IOException in case cleaning is unsuccessful
     */
    private static boolean tryCleanDirectory(@NonNull File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) throw new IOException("Failed to list content of " + directory);

        boolean isSuccess = true;

        for (File file : files) {
            if (file.isDirectory() && !tryCleanDirectory(file)) isSuccess = false;
            if (!file.delete() && file.exists()) isSuccess = false;
        }

        return isSuccess;
    }

    public static boolean checkAndSetRootFolder(String folder) {
        return checkAndSetRootFolder(folder, false);
    }

    public static boolean checkAndSetRootFolder(String folder, boolean notify) {
        Context context = TestApp.getAppContext();

        // Validate folder
        File file = new File(folder);
        if (!file.exists() && !file.isDirectory() && !FileUtil.makeDir(file)) {
            return false;
        }

        File nomedia = new File(folder, ".nomedia");
        boolean hasPermission;
        // Clean up (if any) nomedia file
        if (nomedia.exists()) {
            boolean deleted = FileUtil.deleteFile(nomedia);
            if (deleted) {
                Timber.d(".nomedia file deleted");
            }
        }
        // Re-create nomedia file to confirm write permissions
        hasPermission = FileUtil.makeFile(nomedia, true);

        if (!hasPermission) {
            return false;
        }

        return true;
    }

    public static File getDefaultDir(Context context, String dir) {
        File file;
        try {
            file = new File(getExternalStorageDirectory() + File.separator
                    + Consts.DEFAULT_LOCAL_DIRECTORY + File.separator + dir);
        } catch (Exception e) {
            file = context.getDir("", Context.MODE_PRIVATE);
            file = new File(file, File.separator + Consts.DEFAULT_LOCAL_DIRECTORY);
        }

        if (!file.exists() && !FileUtil.makeDir(file)) {
            file = context.getDir("", Context.MODE_PRIVATE);
            file = new File(file, File.separator + Consts.DEFAULT_LOCAL_DIRECTORY + File.separator + dir);
            if (!file.exists()) {
                FileUtil.makeDir(file);
            }
        }

        return file;
    }

    /**
     * Recursively search for files of a given type from a base directory
     *
     * @param workingDir the base directory
     * @return list containing all files with matching extension
     */
    public static List<File> findFilesRecursively(File workingDir, String extension) {
        return findFilesRecursively(workingDir, extension, 0);
    }

    private static List<File> findFilesRecursively(File workingDir, String extension, int depth) {
        List<File> files = new ArrayList<>();
        File[] baseDirs = workingDir.listFiles(pathname -> (pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(extension)));

        for (File entry : baseDirs) {
            if (entry.isDirectory()) {
                if (depth < 6)
                    files.addAll(findFilesRecursively(entry, extension, depth + 1)); // Hard recursive limit to avoid catastrophes
            } else {
                files.add(entry);
            }
        }
        return files;
    }

    /**
     * Open the given file using the device's app(s) of choice
     *
     * @param context Context
     * @param aFile   File to be opened
     */
    public static void openFile(Context context, File aFile) {
        File file = new File(aFile.getAbsolutePath());
        Intent myIntent = new Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(context, AUTHORITY, file));
        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Timber.e(e, "Activity not found to open %s", aFile.getAbsolutePath());
        }
    }

    public static String getFileProviderAuthority() {
        return AUTHORITY;
    }

    /**
     * Returns the extension of the given filename
     *
     * @param fileName Filename
     * @return Extension of the given filename
     */
    public static String getExtension(String fileName) {
        return fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.getDefault()) : "";
    }

    public static String getFileNameWithoutExtension(String fileName) {
        return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
    }


    /**
     * Save the given binary content in the given file
     *
     * @param file          File to save content on
     * @param binaryContent Content to save
     * @throws IOException If any IOException occurs
     */
    public static void saveBinaryInFile(File file, byte[] binaryContent) throws IOException {
        byte[] buffer = new byte[1024];
        int count;

        try (InputStream input = new ByteArrayInputStream(binaryContent)) {
            try (BufferedOutputStream output = new BufferedOutputStream(FileHelper.getOutputStream(file))) {

                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                output.flush();
            }
        }
    }

    /**
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
     * </ul>
     * <p>
     * Custom substitute for commons.io.FileUtils.deleteQuietly that works with devices that doesn't support File.toPath
     *
     * @param file file or directory to delete, can be {@code null}
     * @return {@code true} if the file or directory was deleted, otherwise
     * {@code false}
     */
    private static boolean deleteQuietly(final File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                tryCleanDirectory(file);
            }
        } catch (final Exception ignored) {
        }

        try {
            return file.delete();
        } catch (final Exception ignored) {
            return false;
        }
    }

    public static boolean renameDirectory(File srcDir, File destDir) {
        try {
            FileUtils.moveDirectory(srcDir, destDir);
            return true;
        } catch (Exception e) {
            Timber.e(e);
        }
        return false;
    }
}
