package com.exoplatform.cloudworkspaces.cloudlogin.data;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * <ul>
 * <li>NONE = no JCR folder, never login</li>
 * <li>INITIATED = Screens process currently running, JCR folder exists with value "INITIATED"</li>
 * <li>DISPLAYED = Screens displayed and viewed by user, end of process, JCR folder exists with value "DISPLAYED"</li>
 * </ul>
 * 
 * @author Clement
 *
 */
public enum CloudLoginStatus implements Value {
  NONE,
  INITIATED,
  DISPLAYED;
  
  public static CloudLoginStatus getCloudLoginStatus(String str) throws ValueFormatException, IllegalStateException, RepositoryException {
    CloudLoginStatus cloudLoginStatus = NONE;
    
    if(str != null && str.length() > 0) {
      for(CloudLoginStatus status : CloudLoginStatus.values()) {
        if(status.getString().equals(str)) {
          cloudLoginStatus = status;
          break;
        }
      }
    }
    
    return cloudLoginStatus;
  }

  @Override
  public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
    return this.toString();
  }

  @Override
  public InputStream getStream() throws IllegalStateException, RepositoryException {
    return null;
  }

  @Override
  public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
    return 0;
  }

  @Override
  public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
    return 0;
  }

  @Override
  public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
    return null;
  }

  @Override
  public boolean getBoolean() throws ValueFormatException, IllegalStateException, RepositoryException {
   return false;
  }

  @Override
  public int getType() {
    return 0;
  }
}
