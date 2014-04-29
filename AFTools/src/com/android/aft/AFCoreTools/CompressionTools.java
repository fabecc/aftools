package com.android.aft.AFCoreTools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CompressionTools {

    /**
     * Unzip zip file format
     *
     * @param zip       Path the zip file
     * @param dest      Path to the destination file
     *
     * @return True if success
     */
    public static boolean unzip(File zip, File dest)
    {
        if (!zip.exists()) {
            DebugTools.e("Cannot extract data. Input zip file does not exist: " + zip.getAbsolutePath());
            return false;
        }

        if (dest.exists())
            FileTools.deleteFile(dest.getAbsolutePath());
        dest.mkdirs();

        try
        {
             InputStream is = new FileInputStream(zip);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
             ZipEntry ze;
             byte[] buffer = new byte[1024];
             int count;

             while ((ze = zis.getNextEntry()) != null)
             {
                 String full_filename = dest + "/" + ze.getName();
                 DebugTools.v("Extract " + full_filename);

                 // Need to create directories if not exists, or
                 // it will generate an Exception...
                 if (ze.isDirectory()) {
                    File fmd = new File(full_filename);
                    fmd.mkdirs();
                    continue;
                 }

                 FileOutputStream fout = new FileOutputStream(full_filename);

                 while ((count = zis.read(buffer)) != -1)
                     fout.write(buffer, 0, count);

                 fout.close();
                 zis.closeEntry();
             }

             zis.close();
         }
         catch(IOException e)
         {
             DebugTools.e("Cannot extract zip data", e);
             return false;
         }

        return true;
    }

}
