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
package com.exoplatform.cloudworkspaces.installer.rest;

import java.util.List;
import java.util.Map;

public interface CloudAdminServices {

  public String serverStart(String cloudType) throws AdminException;

  public void serverStop(String alias) throws AdminException;

  public Map<String, String> serverStates() throws AdminException;

  public String serverState(String alias) throws AdminException;

  public Map<String, Map<String, String>> getTypes() throws AdminException;

  public Map<String, Map<String, Object>> describeInstances() throws AdminException;

  public Map<String, Object> describeInstance(String instanceId) throws AdminException;

  public Map<String, String> databaseConfig(String dbAlias) throws AdminException;

  public void allowAutoscaling() throws AdminException;

  public void blockAutoscaling() throws AdminException;

  public Map<String, String> createTenant(String tenant, String email) throws AdminException;

  public Map<String, String> tenantStatus(String tenant) throws AdminException;

  public void tenantEnable(String tenant) throws AdminException;

  public void tenantDisable(String tenant) throws AdminException;

  public void tenantStart(String tenant) throws AdminException;

  public void tenantStop(String tenant) throws AdminException;

  public void tenantRemove(String tenant) throws AdminException;

  public void tenantRestartCreation(String tenant) throws AdminException;

  public List<String> tenantList() throws AdminException;

  public Map<String, List<String>> tenantListOrderAs() throws AdminException;

  public Map<String, List<String>> tenantListOrderState() throws AdminException;

  public int tenantNumber() throws AdminException;

  public Map<String, Integer> tenantNumberOrderAs() throws AdminException;

  public Map<String, Integer> tenantNumberOrderState() throws AdminException;

}
