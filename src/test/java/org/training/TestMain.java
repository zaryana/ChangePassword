/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide;

import com.codenvy.ide.factory.TraketFactoryForLoginUser;

import org.exoplatform.ide.commons.ParsingResponseException;

import java.io.IOException;

/** @author Musienko Maxim */
public class TestMain {
    static FactoryUtils inst   = new FactoryUtils();
    static String       gitUrl = "https://github.com/exoinvitemain/mavenProjectIDE3.git";
    static TraketFactoryForLoginUser testfactoty;

    public static void main(String[] args) throws IOException, ParsingResponseException {
        System.out.println(testfactoty.getFactoryUrlWithOrgId());
    }

}
