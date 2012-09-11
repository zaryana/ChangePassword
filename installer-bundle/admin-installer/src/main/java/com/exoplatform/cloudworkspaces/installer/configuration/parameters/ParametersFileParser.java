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
package com.exoplatform.cloudworkspaces.installer.configuration.parameters;

import com.exoplatform.cloudworkspaces.installer.InstallerException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ParametersFileParser {

  protected final Map<String, Class<? extends ConfigurationParameter>> classes;

  protected final Map<String, Map<String, String>>                     methods;

  public ParametersFileParser() {
    this.classes = new HashMap<String, Class<? extends ConfigurationParameter>>();
    this.methods = new HashMap<String, Map<String, String>>();
  }

  public void addClass(String key, Class<? extends ConfigurationParameter> clazz) throws InstallerException {
    classes.put(key, clazz);
    methods.put(key, new HashMap<String, String>());
  }

  public void addMethod(String parentKey, String key, String methodName) throws InstallerException {
    methods.get(parentKey).put(key, methodName);
  }

  public ConfigurationParametersSet readConfiguration(InputStream stream) throws InstallerException {
    ConfigurationParametersSet result = new ConfigurationParametersSet();
    Scanner in = new Scanner(stream);

    int lineNum = 0;
    String container = null;
    String type = null;
    ConfigurationParameter parameter = null;
    while (in.hasNext()) {
      String line = in.nextLine();
      lineNum++;
      if (line.trim().isEmpty())
        continue;
      int level = getLevel(line);
      switch (level) {
      case 0: {
        container = line.trim().substring(1, line.length() - 1).trim();
        break;
      }
      case 1: {
        String[] strs = line.trim().split("\\s+");
        type = strs[0].trim().substring(1, strs[0].length() - 1).trim();
        String[] args = new String[strs.length - 1];
        for (int i = 0; i < args.length; i++)
          args[i] = strs[i + 1];
        try {
          parameter = newInstance(type, args);
          result.addParameter(container, type, parameter);
        } catch (ParametersParseException e) {
          throw new InstallerException("Error while parsing configuration parameters file on line "
              + lineNum, e);
        }
        break;
      }
      case 2: {
        String[] strs = line.trim().split("\\s+");
        String method = strs[0].trim().substring(1, strs[0].length() - 1).trim();
        try {
          String[] args = new String[strs.length - 1];
          for (int i = 0; i < args.length; i++)
            args[i] = strs[i + 1];
          callMethod(parameter, type, method, args);
        } catch (ParametersParseException e) {
          throw new InstallerException("Error while parsing configuration parameters file on line "
              + lineNum, e);
        }
        break;
      }
      }
    }
    return result;
  }

  protected int getLevel(String line) {
    int level = 0;
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) == ' ') {
        level++;
      } else {
        break;
      }
    }
    // use 2 spaces
    return level / 2;
  }

  protected ConfigurationParameter newInstance(String type, String... args) throws ParametersParseException {
    Class<? extends ConfigurationParameter> clazz = classes.get(type);
    try {
      Class<?>[] types = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        types[i] = String.class;
      }
      return clazz.getConstructor(types).newInstance(args);
    } catch (InstantiationException e) {
      throw new ParametersParseException("Couldn't use class " + clazz.getName() + " for type "
          + type, e);
    } catch (IllegalArgumentException e) {
      throw new ParametersParseException("Couldn't use class " + clazz.getName() + " for type "
          + type, e);
    } catch (SecurityException e) {
      throw new ParametersParseException("Couldn't use class " + clazz.getName() + " for type "
          + type, e);
    } catch (IllegalAccessException e) {
      throw new ParametersParseException("Couldn't use class " + clazz.getName() + " for type "
          + type, e);
    } catch (InvocationTargetException e) {
      throw new ParametersParseException("Couldn't use class " + clazz.getName() + " for type "
          + type, e);
    } catch (NoSuchMethodException e) {
      throw new ParametersParseException("Couldn't use class " + clazz.getName() + " for type "
          + type, e);
    }
  }

  protected void callMethod(ConfigurationParameter param, String type, String name, String... args) throws ParametersParseException {
    Class<? extends ConfigurationParameter> clazz = classes.get(type);
    try {

      Class<?>[] types = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        types[i] = String.class;
      }
      clazz.getMethod(methods.get(type).get(name), types).invoke(param, args);
    } catch (NoSuchMethodException e) {
      throw new ParametersParseException("Couldn't add method with name " + name + " for type "
          + type + " with arguments count " + args.length, e);
    } catch (IllegalArgumentException e) {
      throw new ParametersParseException("Couldn't add method with name " + name + " for type "
          + type + " with arguments count " + args.length, e);
    } catch (SecurityException e) {
      throw new ParametersParseException("Couldn't add method with name " + name + " for type "
          + type + " with arguments count " + args.length, e);
    } catch (IllegalAccessException e) {
      throw new ParametersParseException("Couldn't add method with name " + name + " for type "
          + type + " with arguments count " + args.length, e);
    } catch (InvocationTargetException e) {
      throw new ParametersParseException("Couldn't add method with name " + name + " for type "
          + type + " with arguments count " + args.length, e);
    }
  }

  private static class ParametersParseException extends Exception {

    public ParametersParseException(String message, Exception cause) {
      super(message, cause);
    }

  }

  public static void main(String[] args) throws InstallerException, FileNotFoundException {
    ParametersFileParser currAdminParametersParser = new ParametersFileParser();
    currAdminParametersParser.addClass("properties", PropertiesConfigurationParameter.class);
    currAdminParametersParser.addMethod("properties", "source", "addSource");
    currAdminParametersParser.addClass("tomcat-users", TomcatUserConfigurationParameter.class);
    ConfigurationParametersSet parameters = currAdminParametersParser.readConfiguration(new FileInputStream("src/main/resources/versions/1.1.0-Beta09.parameters"));
  }

}
