package com.exoplatform.cloudworkspaces.cloudlogin;

import com.exoplatform.cloudworkspaces.cloudlogin.data.CloudLoginStatus;

public interface CloudLoginService {

  /**
   * Returns the @CloudLoginStatus depending on the status of the JCR node
   * <p>
   * There is a specificity about status: IF there is no node created, the status is NONE
   * 
   * @param user
   * @return
   */
  public CloudLoginStatus getStatus(String userName);
  
  /**
   * Set the status to the cloud login node
   * 
   * @param user
   * @param status
   * @return
   */
  public CloudLoginStatus setStatus(String userName, CloudLoginStatus status);
}
