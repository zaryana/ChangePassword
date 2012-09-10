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
import com.exoplatform.cloudworkspaces.installer.configuration.Question;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnswersManager {

  private final File                saveTo;

  private final Map<String, String> answers = new HashMap<String, String>();

  private final List<String>        lines   = new ArrayList<String>();

  public AnswersManager(File saveTo) {
    this.saveTo = saveTo;
  }

  public void addBlockName(String block) {
    lines.add("");
    lines.add("# " + block);
  }

  public void addAnswer(Question question, String answer) throws InstallerException {
    answers.put(question.getParameter(), answer);
    lines.add(question.getParameter() + "=" + answer);
    if (saveTo != null) {
      try {
        saveTo(saveTo, lines);
      } catch (IOException e) {
        throw new InstallerException("Error while saving parameters to file "
            + saveTo.getAbsolutePath(), e);
      }
    }
  }

  public String getAnswer(String parameter) {
    return answers.get(parameter);
  }

  private void saveTo(File saveTo, List<String> answers) throws IOException {
    File save = saveTo.getAbsoluteFile();
    if (!save.exists()) {
      if (save.getParentFile() != null && !save.getParentFile().exists()
          && save.getParentFile().mkdirs())
        throw new IOException("Couldn't create directory for answers file");
      if (!saveTo.createNewFile())
        throw new IOException("Couldn't create file for answers");
    }
    PrintWriter out = new PrintWriter(new FileOutputStream(save));
    for (String line : answers) {
      out.println(line);
    }
    out.close();
  }

}
