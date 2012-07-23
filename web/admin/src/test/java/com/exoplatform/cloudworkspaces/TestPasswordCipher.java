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
package com.exoplatform.cloudworkspaces;

import com.exoplatform.cloud.admin.CloudAdminException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPasswordCipher {

  private PasswordCipher passwordCipher;

  private String         plainText;

  @BeforeMethod
  public void init() throws CloudAdminException {
    passwordCipher = new PasswordCipher();
    plainText = "Test PasswordCipher 2012";
  }

  @Test
  public void testBasicEncrption() throws CloudAdminException {

    String encrypted = passwordCipher.encrypt(plainText);
    Assert.assertFalse(plainText.equals(encrypted));

    String decrypted = passwordCipher.decrypt(encrypted);
    Assert.assertEquals(plainText, decrypted);
  }

}
