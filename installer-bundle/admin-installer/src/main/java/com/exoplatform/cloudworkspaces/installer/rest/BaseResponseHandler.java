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
package com.exoplatform.cloudworkspaces.installer.rest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BaseResponseHandler {

  public HttpEntity handleResponse(HttpResponse response) throws AdminException {
    if (response.getStatusLine().getStatusCode() != 200) {
      throw createException(response);
    }
    return response.getEntity();
  }

  public JsonValue parseAsJson(HttpEntity entity) throws IOException {
    InputStream in = entity.getContent();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    byte[] buf = new byte[100 * 1024];
    int length = 0;
    while (length >= 0) {
      out.write(buf, 0, length);
      length = in.read(buf);
    }
    try {
      JsonParser jsonParser = new JsonParser();
      jsonParser.parse(new ByteArrayInputStream(out.toByteArray()));
      JsonValue jsonValue = jsonParser.getJsonObject();
      return jsonValue;
    } catch (JsonException e) {
      throw new IOException("Expected JSON but receive html or some-thing else: "
          + e.getLocalizedMessage() + " \n" + new String(out.toByteArray()), e);
    } finally {
      EntityUtils.consume(entity);
    }
  }

  public AdminException createException(HttpResponse response) throws AdminException {
    StringBuilder builder = new StringBuilder();
    builder.append("Server returns response with status ");
    int code = response.getStatusLine().getStatusCode();
    builder.append(code);
    builder.append(" and message: ");
    try {
      InputStream in = response.getEntity().getContent();
      try {
        builder.append(streamToString(in));
        return new AdminException(builder.toString());
      } finally {
        in.close();
      }
    } catch (IllegalStateException e) {
      throw new AdminException("Error happened when handling response with status " + code, e);
    } catch (IOException e) {
      throw new AdminException("Error happened when handling response with status " + code, e);
    }
  }

  public String streamToString(InputStream stream) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try {
      byte[] buf = new byte[100 * 1024];
      int length = 0;
      while (length >= 0) {
        bout.write(buf, 0, length);
        length = stream.read(buf);
      }
    } finally {
      bout.close();
    }
    return new String(bout.toByteArray());
  }

  public List<String> parseJsonToList(JsonValue json) {
    List<String> result = new ArrayList<String>();
    Iterator<JsonValue> iter = json.getElements();
    while (iter.hasNext()) {
      result.add(iter.next().getStringValue());
    }
    return result;
  }

  public Map<String, String> parseJsonToMap(JsonValue json) {
    Map<String, String> result = new HashMap<String, String>();
    Iterator<String> iter = json.getKeys();
    while (iter.hasNext()) {
      String key = iter.next();
      String value = json.getElement(key).getStringValue();
      result.put(key, value);
    }
    return result;
  }

  public Map<String, Integer> parseJsonToIntMap(JsonValue json) {
    Map<String, Integer> result = new HashMap<String, Integer>();
    Iterator<String> iter = json.getKeys();
    while (iter.hasNext()) {
      String key = iter.next();
      Integer value = json.getElement(key).getIntValue();
      result.put(key, value);
    }
    return result;
  }

  public Map<String, List<String>> parseJsonToMapList(JsonValue json) {
    Map<String, List<String>> result = new HashMap<String, List<String>>();
    Iterator<String> iter = json.getKeys();
    while (iter.hasNext()) {
      String key = iter.next();
      List<String> value = parseJsonToList(json.getElement(key));
      result.put(key, value);
    }
    return result;
  }

  public Map<String, Map<String, String>> parseJsonToMapMap(JsonValue json) {
    Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
    Iterator<String> iter = json.getKeys();
    while (iter.hasNext()) {
      String key = iter.next();
      Map<String, String> value = parseJsonToMap(json.getElement(key));
      result.put(key, value);
    }
    return result;
  }

  public Object parseJsonToObject(JsonValue json) {
    if (json.isNull()) {
      return null;
    }
    if (json.isBoolean()) {
      return json.getBooleanValue();
    }
    if (json.isDouble()) {
      return json.getDoubleValue();
    }
    if (json.isLong()) {
      return json.getLongValue();
    }
    if (json.isNumeric()) {
      return json.getIntValue();
    }
    if (json.isString()) {
      return json.getStringValue();
    }
    if (json.isArray()) {
      List<Object> array = new ArrayList<Object>();
      Iterator<JsonValue> iter = json.getElements();
      while (iter.hasNext()) {
        array.add(parseJsonToObject(iter.next()));
      }
      return array;
    }
    if (json.isObject()) {
      Map<String, Object> map = new HashMap<String, Object>();
      Iterator<String> iter = json.getKeys();
      while (iter.hasNext()) {
        String key = iter.next();
        map.put(key, parseJsonToObject(json.getElement(key)));
      }
      return map;
    }
    // NEVER HAPPENS
    throw new RuntimeException("Couldn't parse json to object");
  }

  public Map<String, Object> parseJsonToMapObject(JsonValue json) {
    Map<String, Object> result = new HashMap<String, Object>();
    Iterator<String> iter = json.getKeys();
    while (iter.hasNext()) {
      String key = iter.next();
      Object value = parseJsonToObject(json.getElement(key));
      result.put(key, value);
    }
    return result;
  }

  public Map<String, Map<String, Object>> parseJsonToMapMapObject(JsonValue json) {
    Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
    Iterator<String> iter = json.getKeys();
    while (iter.hasNext()) {
      String key = iter.next();
      Map<String, Object> value = parseJsonToMapObject(json.getElement(key));
      result.put(key, value);
    }
    return result;
  }

  public List<String> parseToList(HttpEntity entity) throws IOException {
    return parseJsonToList(parseAsJson(entity));
  }

  public Map<String, String> parseToMap(HttpEntity entity) throws IOException {
    return parseJsonToMap(parseAsJson(entity));
  }

  public Map<String, Integer> parseToIntMap(HttpEntity entity) throws IOException {
    return parseJsonToIntMap(parseAsJson(entity));
  }

  public Map<String, List<String>> parseToMapList(HttpEntity entity) throws IOException {
    return parseJsonToMapList(parseAsJson(entity));
  }

  public Map<String, Object> parseToMapObject(HttpEntity entity) throws IOException {
    return parseJsonToMapObject(parseAsJson(entity));
  }

  public Map<String, Map<String, String>> parseToMapMap(HttpEntity entity) throws IOException {
    return parseJsonToMapMap(parseAsJson(entity));
  }

  public Map<String, Map<String, Object>> parseToMapMapObject(HttpEntity entity) throws IOException {
    return parseJsonToMapMapObject(parseAsJson(entity));
  }

}
