/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.exoplatform.cloudworkspaces.installer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

  public static void copyFile(File from, File to) throws IOException {
    FileInputStream fin = new FileInputStream(from);
    FileOutputStream fout = new FileOutputStream(to);
    try {
      byte[] buf = new byte[100 * 1024];
      int length = 0;
      while (length >= 0) {
        fout.write(buf, 0, length);
        length = fin.read(buf);
      }
    } finally {
      fin.close();
      fout.close();
    }
  }

  public static void copyDirs(File from, File to) throws IOException {
    for (File file : from.listFiles()) {
      if (file.isDirectory()) {
        File toDir = new File(to, file.getName());
        if (!toDir.mkdir())
          throw new IOException("Couldn't create directory " + file.getAbsolutePath());
        copyDirs(file, toDir);
      } else {
        copyFile(file, new File(to, file.getName()));
      }
    }
  }

  public static void deleteDir(File dir) throws IOException {
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        deleteDir(file);
      } else {
        if (!file.delete())
          throw new IOException("File " + file.getAbsolutePath() + " couldn't be deleted");
      }
    }
    if (!dir.delete())
      throw new IOException("Directory " + dir.getAbsolutePath() + " couldn't be deleted");
  }

  public static void unzipTo(File zip, File toDir) throws IOException, InterruptedException {
    if (!toDir.exists() && !toDir.mkdirs()) {
      throw new IOException("Couldn't create directory for unzipping");
    }
    StringBuilder zipCmd = new StringBuilder();
    zipCmd.append("unzip -d ");
    zipCmd.append(toDir.getAbsolutePath());
    zipCmd.append(' ');
    zipCmd.append(zip.getAbsolutePath());
    System.out.println(zipCmd.toString());
    Process process = Runtime.getRuntime().exec(zipCmd.toString());
    int result = process.waitFor();
    if (result != 0) {
      throw new IOException("Unzipping archive failed with code " + result);
    }
  }

}
