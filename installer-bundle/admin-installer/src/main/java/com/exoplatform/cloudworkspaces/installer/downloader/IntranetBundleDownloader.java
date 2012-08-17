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
package com.exoplatform.cloudworkspaces.installer.downloader;

import com.exoplatform.cloudworkspaces.installer.InstallerException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class IntranetBundleDownloader implements BundleDownloader {

  public void downloadAdminTo(String url, String username, String password, File to) throws InstallerException {
    try {
      URI uri = new URI(url);
      DefaultHttpClient client = new DefaultHttpClient();

      client.getCredentialsProvider().setCredentials(new AuthScope(uri.getHost(), uri.getPort()),
                                                     new UsernamePasswordCredentials(username,
                                                                                     password));

      HttpGet request = new HttpGet(url);

      HttpResponse response = null;
      try {
        response = client.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
          throw new InstallerException("Couldn't download admin bundle from url: " + url
              + " Response status code - " + response.getStatusLine().getStatusCode());
        }
        InputStream in = response.getEntity().getContent();
        FileOutputStream out = new FileOutputStream(to);
        try {
          byte[] buf = new byte[100 * 1024];
          int length = 0;

          while (length >= 0) {
            out.write(buf, 0, length);
            length = in.read(buf);
          }
        } finally {
          out.close();
        }
      } finally {
        if (response != null) {
          response.getEntity().getContent().close();
        }
      }
    } catch (IOException e) {
      throw new InstallerException(e);
    } catch (URISyntaxException e) {
      throw new InstallerException(e);
    }
  }

}
