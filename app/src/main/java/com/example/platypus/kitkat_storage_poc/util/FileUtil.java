package com.example.platypus.kitkat_storage_poc.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.example.platypus.kitkat_storage_poc.TestApp;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

/**
 * Created by avluis on 08/25/2016.
 * Methods for use by FileHelper
 */
class FileUtil {


    /**
     * Method ensures file creation from stream.
     *
     * @param stream - FileOutputStream.
     * @return true if all OK.
     */
    static boolean sync(@NonNull final FileOutputStream stream) {
        try {
            stream.getFD().sync();
            return true;
        } catch (IOException e) {
            Timber.e(e, "IO Error");
        }

        return false;
    }

    static OutputStream getOutputStream(@NonNull final DocumentFile target) throws FileNotFoundException {
        Context context = TestApp.getAppContext();
        return context.getContentResolver().openOutputStream(target.getUri());
    }


    static InputStream getInputStream(@NonNull final File target) throws IOException {
        return FileUtils.openInputStream(target);
    }

    /**
     * Get OutputStream from file.
     *
     * @param target The file.
     * @return FileOutputStream.
     */
    static OutputStream getOutputStream(@NonNull final File target) throws IOException {
        try {
            return FileUtils.openOutputStream(target);
        } catch (IOException e) {
            Timber.d("Could not open file (expected)");
        }

        throw new IOException("Error while attempting to get file : " + target.getAbsolutePath());
    }


    /**
     * Create a file.
     *
     * @param file       The file to be created.
     * @param forceWrite Force writing operation even if an existing file is found (useful for I/O tests)
     * @return true if creation was successful.
     */
    static boolean makeFile(@NonNull final File file, boolean forceWrite) {
        if (file.exists()) {
            // nothing to create.
            return !file.isDirectory();
        }

        // Try the normal way
        try {
            return file.createNewFile();
        } catch (IOException e) {
            Timber.e(e,"aaa");
        }

        return false;
    }

    /**
     * Create a folder.
     *
     * @param file The folder to be created.
     * @return true if creation was successful or the folder already exists
     */
    static boolean makeDir(@NonNull final File file) {
        if (file.exists()) {
            // nothing to create.
            return file.isDirectory();
        }

        // Try the normal way
        if (file.mkdirs()) {
            return true;
        }

        return false;
    }

    /**
     * Delete a file.
     *
     * @param file The file to be deleted.
     * @return true if successfully deleted or if the file does not exist.
     */
    static boolean deleteFile(@NonNull final File file) {
        return !file.exists() || FileUtils.deleteQuietly(file);
    }
}
