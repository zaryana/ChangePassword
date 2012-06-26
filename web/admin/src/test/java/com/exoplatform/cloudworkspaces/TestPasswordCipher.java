package com.exoplatform.cloudworkspaces;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestPasswordCipher {

  private PasswordCipher passwordCipher;

  private String         plainText;

  @BeforeMethod
  public void init() {
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
