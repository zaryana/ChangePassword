package com.exoplatform.cloudworkspaces.cloudlogin.impl;

import org.exoplatform.test.BasicTestCase;

public class TestCloudLoginService extends BasicTestCase {

  protected CloudLoginServiceImpl cloudLoginService;

  public void setUp() throws Exception {

    // We just need an instance of the service
    cloudLoginService = new CloudLoginServiceImpl(null, null);
  }
  
  public void testExtractDomainFromEmail() {
    assertEquals(cloudLoginService.extractDomainFromEmail("toto@toto.com"), "toto.com");
    assertEquals(cloudLoginService.extractDomainFromEmail("toto.toto.com"), "");
    assertEquals(cloudLoginService.extractDomainFromEmail(""), "");
    assertEquals(cloudLoginService.extractDomainFromEmail(null), "");
    assertEquals(cloudLoginService.extractDomainFromEmail("toto@toto.d"), "");
  }
}
