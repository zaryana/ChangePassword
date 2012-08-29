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
package com.exoplatform.cloudworkspaces.installer.upgrade;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class VersionsManager {

  private final Map<VersionEntry, AdminUpgradeAlgorithm> versions        = new HashMap<VersionEntry, AdminUpgradeAlgorithm>();

  private final Map<String, AdminUpgradeAlgorithm>       installVersions = new HashMap<String, AdminUpgradeAlgorithm>();

  public VersionsManager(InputStream versionsList) throws FileNotFoundException,
      ClassNotFoundException,
      InstantiationException,
      IllegalAccessException {
    Scanner in = new Scanner(versionsList);
    try {
      while (in.hasNext()) {
        String from = in.next().trim();
        String to = in.next().trim();
        if (to.equals("->"))
          to = in.next().trim();
        String algorithm = in.next().trim();
        Class<? extends AdminUpgradeAlgorithm> clazz = (Class<? extends AdminUpgradeAlgorithm>) getClass().getClassLoader()
                                                                                                          .loadClass(algorithm);
        versions.put(new VersionEntry(from, to), clazz.newInstance());
        installVersions.put(to, clazz.newInstance());
      }
    } finally {
      in.close();
    }
  }

  public AdminUpgradeAlgorithm getAlgorithm(String fromVersion, String toVersion) {
    return versions.get(new VersionEntry(fromVersion, toVersion));
  }

  public AdminUpgradeAlgorithm getAlgorithm(String toVersion) {
    return installVersions.get(toVersion);
  }

  static class VersionEntry {
    final String from;

    final String to;

    public VersionEntry(String from, String to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public int hashCode() {
      return (from + to).hashCode();
    }

    @Override
    public boolean equals(Object q) {
      VersionEntry o = (VersionEntry) q;
      return (from + to).equals(o.from + o.to);
    }
  }

}
