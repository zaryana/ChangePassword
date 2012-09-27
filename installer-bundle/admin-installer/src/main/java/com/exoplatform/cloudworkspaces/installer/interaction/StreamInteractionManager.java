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

import com.exoplatform.cloudworkspaces.installer.configuration.Question;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class StreamInteractionManager implements InteractionManager {

  protected final Map<String, String> answers = new HashMap<String, String>();

  private final Scanner               in;

  private final PrintStream           out;

  public StreamInteractionManager() {
    this.in = new Scanner(System.in);
    this.out = System.out;
  }

  public StreamInteractionManager(InputStream in, OutputStream out) {
    this.in = new Scanner(in);
    this.out = new PrintStream(out);
  }

  @Override
  public Map<Question, String> askQuestions(Question[] questions) {
    Map<Question, String> answers = new HashMap<Question, String>();
    for (Question question : questions) {
      answers.put(question, ask(question));
    }
    return answers;
  }

  @Override
  public void setAnswer(String key, String value) {
    answers.put(key, value);
  }

  @Override
  public String ask(Question question) {
    out.println();
    out.println(question.getDescription());
    String answer = "";
    while (answer.isEmpty() || !answer.matches(question.getValidateRegexp())) {
      out.print(question.getParameter());
      if (question.getDefaults() != null) {
        out.print(" [");
        out.print(question.getDefaults());
        out.print("]: ");
      } else {
        out.print(": ");
      }
      answer = in.nextLine().trim();
      if (answer.isEmpty() && question.getDefaults() != null) {
        answer = question.getDefaults();
      }
      if (answer.isEmpty()) {
        System.out.println("Current property hasn't default property");
      } else if (!answer.matches(question.getValidateRegexp())) {
        System.out.println(question.getValidateMessage());
      }
    }
    return answer;
  }

  @Override
  public boolean askGroup(Question... questions) {
    out.println();
    out.println("Detected previous settings:\n");
    for (Question question : questions) {
      out.print(question.getParameter());
      out.print('=');
      if (answers.containsKey(question.getParameter())) {
        out.println(answers.get(question.getParameter()));
      } else {
        out.println(question.getDefaults());
      }
    }
    String answer = "";
    while (answer.isEmpty() || !answer.matches("yes|no")) {
      out.print("Do you want to use this settings? [yes]: ");
      answer = in.nextLine().trim();
      if (answer.isEmpty()) {
        answer = "yes";
      }
      if (!answer.matches("yes|no")) {
        System.out.println("Write yes or no");
      }
    }
    return answer.equals("yes");
  }

  @Override
  public void print(String message) {
    out.print(message);
  }

  @Override
  public void println(String message) {
    out.println(message);
  }

}
