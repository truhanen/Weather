/*******************************************************************************
 * Copyright (C) 2015 Tuukka Ruhanen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package fi.tuukka.weather.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FileUtils {

    public static final String HISTORYSTART = "history";
    public static final String RAINSTART = "rain";

    public static boolean hasFile(String name, Context context) {
        File dir = context.getFilesDir();
        return new File(dir, name).exists();
    }

    public static void saveBitmap(String name, Bitmap bmp, Context context) {
        File dir = context.getFilesDir();
        File file = new File(dir, name);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static long lastModified(String name, Context context) {
        File dir = context.getFilesDir();
        File file = new File(dir, name);
        if (file.isFile())
            return file.lastModified();
        return 0l;
    }

    public static Bitmap openBitmap(String name, Context context) {
        File dir = context.getFilesDir();
        File file = new File(dir, name);
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
        return bmp;
    }

    public static void removeOldFiles(String nameStart, Context context) {
        File dir = context.getFilesDir();
        String[] names = dir.list();
        for (int i = 0; i < names.length; i++) {
            if (names[i].startsWith(nameStart)) {
                new File(dir, names[i]).delete();
            }
        }
    }

    public static String[] getFileNames(String nameStart, Context context) {
        ArrayList<String> fileNames = new ArrayList<String>();
        File dir = context.getFilesDir();
        String[] names = dir.list();
        for (int i = 0; i < names.length; i++) {
            if (names[i].startsWith(nameStart)) {
                fileNames.add(names[i]);
            }
        }
        return fileNames.toArray(new String[fileNames.size()]);
    }

    public static void deleteFile(String name, Context context) {
        File dir = context.getFilesDir();
        new File(dir, name).delete();
    }

    public static void serializeObject(File file, Object obj) {
        FileOutputStream fileOut = null;
        ObjectOutputStream out = null;
        try {
            fileOut = new FileOutputStream(file);
            out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
        } catch (IOException i) {
            i.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileOut != null)
                        fileOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Object deserializeObject(File file) {
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        Object obj = null;
        try {
            fileIn = new FileInputStream("/tmp/employee.ser");
            in = new ObjectInputStream(fileIn);
            obj = in.readObject();
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileIn != null)
                        fileIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
}
