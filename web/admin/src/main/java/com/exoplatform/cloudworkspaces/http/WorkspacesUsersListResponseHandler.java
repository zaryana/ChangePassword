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
package com.exoplatform.cloudworkspaces.http;

import static java.net.HttpURLConnection.HTTP_OK;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.ObjectValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WorkspacesUsersListResponseHandler implements ResponseHandler<Map<String, String>> {

  @Override
  public Map<String, String> handleResponse(HttpResponse response) throws ClientProtocolException,
                                                                  IOException {

    if (response.getStatusLine().getStatusCode() != HTTP_OK) {
      throw new IOException(response.getStatusLine().toString());
    }

    try {
      HashMap<String, String> result = new HashMap<String, String>();
      InputStream io = response.getEntity().getContent();
      JsonParser jsonParser = new JsonParser();
      jsonParser.parse(io);
      ObjectValue responseObj = (ObjectValue) jsonParser.getJsonObject();
      Iterator<String> users = responseObj.getKeys();
      while (users.hasNext()) {
        String userName = users.next();
        String email = responseObj.getElement(userName).getStringValue();
        result.put(userName, email);
      }
      return result;
    } catch (JsonException e) {
      throw new IOException("Error while parsing response to json.", e);
    }
  }

}
