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

import java.security.InvalidKeyException;

import org.apache.commons.codec.binary.Base64;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Advanced Encryption Standard (AES). A simple text cipher to encrypt/decrypt a
 * password.
 */
public class PasswordCipher {
  private static byte[]       linebreak = {};

  private static String       secret    = "tvnw63wfg9gh5392";

  private static SecretKey    key;

  private static Cipher       cipher;

  private static Base64       coder;

  private static final Logger LOG       = LoggerFactory.getLogger(PasswordCipher.class);

  public PasswordCipher() {
    try {
      key = new SecretKeySpec(secret.getBytes(), "AES");
      cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
      coder = new Base64(32, linebreak, true);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public synchronized String encrypt(String plainText) throws CloudAdminException {
    String encrypt = "";
    try {
      cipher.init(Cipher.ENCRYPT_MODE, key);
      byte[] cipherText = cipher.doFinal(plainText.getBytes());
      encrypt = new String(coder.encode(cipherText));
    } catch (InvalidKeyException e) {
      throw new CloudAdminException("Cannot encrypt password", e);
    } catch (IllegalBlockSizeException e) {
      throw new CloudAdminException("Cannot encrypt password", e);
    } catch (BadPaddingException e) {
      throw new CloudAdminException("Cannot encrypt password", e);
    }

    return encrypt;
  }

  public synchronized String decrypt(String codedText) throws CloudAdminException {
    String decrypt = "";
    try {
      byte[] encypted = coder.decode(codedText.getBytes());
      cipher.init(Cipher.DECRYPT_MODE, key);
      byte[] decrypted = cipher.doFinal(encypted);
      decrypt = new String(decrypted);
    } catch (InvalidKeyException e) {
      throw new CloudAdminException("Cannot decrypt password", e);
    } catch (IllegalBlockSizeException e) {
      throw new CloudAdminException("Cannot decrypt password", e);
    } catch (BadPaddingException e) {
      throw new CloudAdminException("Cannot decrypt password", e);
    }
    return decrypt;

  }
}
