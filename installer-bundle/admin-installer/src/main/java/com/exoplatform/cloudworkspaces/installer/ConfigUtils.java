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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigUtils {

  public static void replace(File confDir, String confFile, String from, String to) throws IOException {
    File conf = new File(confDir.getAbsolutePath() + "/" + confFile);
    replace(conf, from, to);
  }

  public static void replace(File conf, String from, String to) throws IOException {
    String content = readFile(conf);
    Pattern pattern = Pattern.compile(from, Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(content);
    content = matcher.replaceAll(to);
    writeFile(conf, content);
  }

  public static String find(File confDir, String confFile, String regexp) throws IOException {
    File conf = new File(confDir.getAbsolutePath() + "/" + confFile);
    return find(conf, regexp);
  }

  public static String find(File conf, String regexp) throws IOException {
    if (!conf.exists())
      return null;
    String content = readFile(conf);
    Pattern pattern = Pattern.compile(regexp, Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(content);
    if (!matcher.find())
      return null;
    return matcher.group(1);
  }

  public static String findProperty(File confDir, String confFile, String key) throws IOException {
    return findProperty(new File(confDir, confFile), key);
  }

  public static String findProperty(File confFile, String key) throws IOException {
    return find(confFile, "^" + key + "=[\"]?([^\"\\n]*)[\"]?$");
  }

  public static void writeProperty(File confDir, String confFile, String key, String value) throws IOException {
    writeProperty(new File(confDir, confFile), key, value);
  }

  public static void writeProperty(File confFile, String key, String value) throws IOException {
    replace(confFile, "^" + key + "=[\"]?[^\"\\n]*[\"]?$", key + "=" + value + "");
  }

  public static void writeQuotedProperty(File confDir, String confFile, String key, String value) throws IOException {
    replace(confDir, confFile, "^" + key + "=[\"]?[^\"\\n]*[\"]?$", key + "=\"" + value + "\"");
  }

  public static String readFile(File file) throws IOException {
    FileReader reader = new FileReader(file);
    StringBuilder builder = new StringBuilder();
    try {
      char[] cbuf = new char[100 * 1024];
      int length = 0;
      while (length >= 0) {
        builder.append(cbuf, 0, length);
        length = reader.read(cbuf);
      }
    } finally {
      reader.close();
    }
    return builder.toString();
  }

  public static void writeFile(File file, String string) throws IOException {
    FileWriter writer = new FileWriter(file);
    try {
      writer.write(string.toCharArray());
    } finally {
      writer.close();
    }
  }

}
