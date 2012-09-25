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
package com.exoplatform.cloudworkspaces.installer.interaction;

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousQuestion;
import com.exoplatform.cloudworkspaces.installer.configuration.Question;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class InteractionManagerWithAnswers extends StreamInteractionManager {

  private final Map<String, String> answers;

  public InteractionManagerWithAnswers(File answers, Map<String, String> priorityAnswers) throws InstallerException {
    super();
    try {
      this.answers = readAnswers(answers);
      for (String key : priorityAnswers.keySet()) {
        this.answers.put(key, priorityAnswers.get(key));
      }
    } catch (IOException e) {
      throw new InstallerException("File with answers " + answers.getAbsolutePath() + " not found");
    }
  }

  public InteractionManagerWithAnswers(InputStream in, OutputStream out, File answers) throws InstallerException {
    super(in, out);
    try {
      this.answers = readAnswers(answers);
    } catch (IOException e) {
      throw new InstallerException("File with answers " + answers.getAbsolutePath() + " not found");
    }
  }

  private Map<String, String> readAnswers(File answers) throws FileNotFoundException, IOException {
    HashMap<String, String> results = new HashMap<String, String>();
    Properties properties = new Properties();
    properties.load(new FileInputStream(answers));
    for (String key : properties.stringPropertyNames()) {
      results.put(key, properties.getProperty(key));
    }
    return results;
  }

  @Override
  public String ask(Question question) {
    if (question instanceof PreviousQuestion) {
      return "no";
    }
    if (answers.containsKey(question.getParameter())) {
      println(question.getParameter() + "=" + answers.get(question.getParameter()));
      return answers.get(question.getParameter());
    }
    return super.ask(question);
  }

}
