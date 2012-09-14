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
package com.exoplatform.cloudworkspaces.installer.configuration;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseConfigurationUpdater implements ConfigurationUpdater {

  protected List<BlockEntry> block = new ArrayList<BlockEntry>();

  protected void clearBlock() {
    block.clear();
  }

  protected void addToBlock(Question question, String answer) {
    block.add(new BlockEntry(question, answer));
  }

  protected boolean wasBlockChanged() {
    for (BlockEntry entry : block) {
      if (entry.getAnswer() == null || entry.getAnswer().isEmpty())
        return false;
    }
    return true;
  }

  protected String[][] getChanges() {
    String[][] result = new String[block.size()][2];
    for (int i = 0; i < result.length; i++) {
      result[i][0] = block.get(i).getQuestion().getParameter();
      result[i][1] = block.get(i).getAnswer();
    }
    return result;
  }

  static class BlockEntry {
    private final Question question;

    private final String   answer;

    public BlockEntry(Question question, String answer) {
      this.question = question;
      this.answer = answer;
    }

    public Question getQuestion() {
      return question;
    }

    public String getAnswer() {
      return answer;
    }
  }

}
