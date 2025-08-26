/*
 * Copyright (C) 2020-2025 Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version, provided you comply with the Additional Terms applicable for LinID Identity Manager software by
 * LINAGORA pursuant to Section 7 of the GNU Affero General Public License, subsections (b), (c), and (e), pursuant to
 * which these Appropriate Legal Notices must notably (i) retain the display of the "LinID™" trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source and free version of LinID™, powered by
 * Linagora © 2009–2013. Contribute to LinID R&D by subscribing to an Enterprise offer!” infobox and in the e-mails
 * sent with the Program, notice appended to any type of outbound messages (e.g. e-mail and meeting requests) as well
 * as in the LinID Identity Manager user interface, (ii) retain all hypertext links between LinID Identity Manager
 * and https://linid.org/, as well as between LINAGORA and LINAGORA.com, and (iii) refrain from infringing LINAGORA
 * intellectual property rights over its trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License and its applicable Additional Terms for
 * LinID Identity Manager along with this program. If not, see <http://www.gnu.org/licenses/> for the GNU Affero
 * General Public License version 3 and <http://www.linagora.com/licenses/> for the Additional Terms applicable to the
 * LinID Identity Manager software.
 */

package io.github.linagora.linid.im.plugin.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.linagora.linid.im.corelib.exception.ApiException;
import io.github.linagora.linid.im.corelib.plugin.authorization.AllowAllAuthorizationPlugin;
import io.github.linagora.linid.im.corelib.plugin.authorization.AuthorizationFactory;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.config.dto.EntityConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.ProviderConfiguration;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import io.github.linagora.linid.im.corelib.plugin.provider.ProviderFactory;
import io.github.linagora.linid.im.corelib.plugin.provider.ProviderPlugin;
import io.github.linagora.linid.im.corelib.plugin.task.TaskEngine;
import io.github.linagora.linid.im.corelib.plugin.validation.ValidationEngine;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test class: DynamicEntityServiceImpl")
class DynamicEntityServiceImplTest {
  @Mock
  private ProviderFactory providerFactory;
  @Mock
  private PluginConfigurationService configurationService;
  @Mock
  private ValidationEngine validationEngine;
  @Mock
  private TaskEngine taskEngine;
  @Mock
  private AuthorizationFactory factory;

  @InjectMocks
  private DynamicEntityServiceImpl service;

  @Test
  @DisplayName("test handleCreate: should call valid services with valid values")
  void testHandleCreate() {
    var entityConfiguration = new EntityConfiguration();
    entityConfiguration.setProvider("test");
    var providerConfiguration = new ProviderConfiguration();
    var provider = Mockito.mock(ProviderPlugin.class);
    var request = Mockito.mock(HttpServletRequest.class);
    var authPlugin = Mockito.mock(AllowAllAuthorizationPlugin.class);

    Mockito.when(factory.getAuthorizationPlugin()).thenReturn(authPlugin);
    Mockito.when(configurationService.getEntityConfiguration(Mockito.anyString())).thenReturn(Optional.of(entityConfiguration));
    Mockito.when(configurationService.getProviderConfiguration(Mockito.any()))
        .thenReturn(Optional.of(providerConfiguration));
    Mockito.when(providerFactory.getProviderByName(Mockito.anyString())).thenReturn(Optional.of(provider));
    Mockito.doNothing().when(taskEngine).execute(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doNothing().when(validationEngine).validate(Mockito.any(), Mockito.any());
    Mockito.when(provider.create(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

    service.handleCreate(request, "test", Map.of());

    Mockito.verify(authPlugin, Mockito.times(1)).validateToken(Mockito.any(), Mockito.any());
    Mockito.verify(authPlugin, Mockito.times(1)).isAuthorized(Mockito.any(), Mockito.any(), Mockito.eq("CREATE"), Mockito.any());
    Mockito.verify(validationEngine, Mockito.times(1)).validate(Mockito.any(), Mockito.eq("beforeCreate"));
    Mockito.verify(provider, Mockito.times(1)).create(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), Mockito.anyString());

    ArgumentCaptor<String> phaseCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), phaseCaptor.capture());

    List<String> phasesCalled = phaseCaptor.getAllValues();

    assertEquals("beforeTokenValidationCreate", phasesCalled.get(0));
    assertEquals("afterTokenValidationCreate", phasesCalled.get(1));
    assertEquals("beforePermissionValidationCreate", phasesCalled.get(2));
    assertEquals("afterPermissionValidationCreate", phasesCalled.get(3));
    assertEquals("beforeValidationCreate", phasesCalled.get(4));
    assertEquals("afterValidationCreate", phasesCalled.get(5));
    assertEquals("beforeCreate", phasesCalled.get(6));
    assertEquals("afterCreate", phasesCalled.get(7));
  }

  @Test
  @DisplayName("test handleUpdate: should call valid services with valid values")
  void testHandleUpdate() {
    var entityConfiguration = new EntityConfiguration();
    entityConfiguration.setProvider("test");
    var providerConfiguration = new ProviderConfiguration();
    var provider = Mockito.mock(ProviderPlugin.class);
    var request = Mockito.mock(HttpServletRequest.class);
    var authPlugin = Mockito.mock(AllowAllAuthorizationPlugin.class);

    Mockito.when(factory.getAuthorizationPlugin()).thenReturn(authPlugin);
    Mockito.when(configurationService.getEntityConfiguration(Mockito.anyString())).thenReturn(Optional.of(entityConfiguration));
    Mockito.when(configurationService.getProviderConfiguration(Mockito.any()))
        .thenReturn(Optional.of(providerConfiguration));
    Mockito.when(providerFactory.getProviderByName(Mockito.anyString())).thenReturn(Optional.of(provider));
    Mockito.doNothing().when(taskEngine).execute(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doNothing().when(validationEngine).validate(Mockito.any(), Mockito.any());
    Mockito.when(provider.update(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

    service.handleUpdate(request, "test", "id", Map.of());

    Mockito.verify(authPlugin, Mockito.times(1)).validateToken(Mockito.any(), Mockito.any());
    Mockito.verify(authPlugin, Mockito.times(1)).isAuthorized(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.eq(
            "UPDATE"),
        Mockito.any());
    Mockito.verify(validationEngine, Mockito.times(1)).validate(Mockito.any(), Mockito.eq("beforeUpdate"));
    Mockito.verify(provider, Mockito.times(1)).update(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), Mockito.anyString());

    ArgumentCaptor<String> phaseCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), phaseCaptor.capture());

    List<String> phasesCalled = phaseCaptor.getAllValues();

    assertEquals("beforeTokenValidationUpdate", phasesCalled.get(0));
    assertEquals("afterTokenValidationUpdate", phasesCalled.get(1));
    assertEquals("beforePermissionValidationUpdate", phasesCalled.get(2));
    assertEquals("afterPermissionValidationUpdate", phasesCalled.get(3));
    assertEquals("beforeValidationUpdate", phasesCalled.get(4));
    assertEquals("afterValidationUpdate", phasesCalled.get(5));
    assertEquals("beforeUpdate", phasesCalled.get(6));
    assertEquals("afterUpdate", phasesCalled.get(7));
  }

  @Test
  @DisplayName("test handlePatch: should call valid services with valid values")
  void testHandlePatch() {
    var entityConfiguration = new EntityConfiguration();
    entityConfiguration.setProvider("test");
    var providerConfiguration = new ProviderConfiguration();
    var provider = Mockito.mock(ProviderPlugin.class);
    var request = Mockito.mock(HttpServletRequest.class);
    var authPlugin = Mockito.mock(AllowAllAuthorizationPlugin.class);

    Mockito.when(factory.getAuthorizationPlugin()).thenReturn(authPlugin);
    Mockito.when(configurationService.getEntityConfiguration(Mockito.anyString())).thenReturn(Optional.of(entityConfiguration));
    Mockito.when(configurationService.getProviderConfiguration(Mockito.any()))
        .thenReturn(Optional.of(providerConfiguration));
    Mockito.when(providerFactory.getProviderByName(Mockito.anyString())).thenReturn(Optional.of(provider));
    Mockito.doNothing().when(taskEngine).execute(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doNothing().when(validationEngine).validate(Mockito.any(), Mockito.any());
    Mockito.when(provider.patch(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

    service.handlePatch(request, "test", "id", Map.of());

    Mockito.verify(authPlugin, Mockito.times(1)).validateToken(Mockito.any(), Mockito.any());
    Mockito.verify(authPlugin, Mockito.times(1)).isAuthorized(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.eq(
            "UPDATE"),
        Mockito.any());
    Mockito.verify(validationEngine, Mockito.times(1)).validate(Mockito.any(), Mockito.eq("beforePatch"));
    Mockito.verify(provider, Mockito.times(1)).patch(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), Mockito.anyString());

    ArgumentCaptor<String> phaseCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), phaseCaptor.capture());

    List<String> phasesCalled = phaseCaptor.getAllValues();

    assertEquals("beforeTokenValidationPatch", phasesCalled.get(0));
    assertEquals("afterTokenValidationPatch", phasesCalled.get(1));
    assertEquals("beforePermissionValidationPatch", phasesCalled.get(2));
    assertEquals("afterPermissionValidationPatch", phasesCalled.get(3));
    assertEquals("beforeValidationPatch", phasesCalled.get(4));
    assertEquals("afterValidationPatch", phasesCalled.get(5));
    assertEquals("beforePatch", phasesCalled.get(6));
    assertEquals("afterPatch", phasesCalled.get(7));
  }

  @Test
  @DisplayName("test handleDelete: should call valid services with valid values")
  void testHandleDelete() {
    var entityConfiguration = new EntityConfiguration();
    entityConfiguration.setProvider("test");
    var providerConfiguration = new ProviderConfiguration();
    var provider = Mockito.mock(ProviderPlugin.class);
    var request = Mockito.mock(HttpServletRequest.class);
    var authPlugin = Mockito.mock(AllowAllAuthorizationPlugin.class);

    Mockito.when(factory.getAuthorizationPlugin()).thenReturn(authPlugin);
    Mockito.when(configurationService.getEntityConfiguration(Mockito.anyString())).thenReturn(Optional.of(entityConfiguration));
    Mockito.when(configurationService.getProviderConfiguration(Mockito.any()))
        .thenReturn(Optional.of(providerConfiguration));
    Mockito.when(providerFactory.getProviderByName(Mockito.anyString())).thenReturn(Optional.of(provider));
    Mockito.doNothing().when(taskEngine).execute(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doNothing().when(validationEngine).validate(Mockito.any(), Mockito.any());
    Mockito.when(provider.delete(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

    service.handleDelete(request, "test", "id");

    Mockito.verify(authPlugin, Mockito.times(1)).validateToken(Mockito.any(), Mockito.any());
    Mockito.verify(authPlugin, Mockito.times(1)).isAuthorized(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.eq(
            "DELETE"),
        Mockito.any());
    Mockito.verify(validationEngine, Mockito.times(1)).validate(Mockito.any(), Mockito.eq("beforeDelete"));
    Mockito.verify(provider, Mockito.times(1)).delete(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), Mockito.anyString());

    ArgumentCaptor<String> phaseCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), phaseCaptor.capture());

    List<String> phasesCalled = phaseCaptor.getAllValues();

    assertEquals("beforeTokenValidationDelete", phasesCalled.get(0));
    assertEquals("afterTokenValidationDelete", phasesCalled.get(1));
    assertEquals("beforePermissionValidationDelete", phasesCalled.get(2));
    assertEquals("afterPermissionValidationDelete", phasesCalled.get(3));
    assertEquals("beforeValidationDelete", phasesCalled.get(4));
    assertEquals("afterValidationDelete", phasesCalled.get(5));
    assertEquals("beforeDelete", phasesCalled.get(6));
    assertEquals("afterDelete", phasesCalled.get(7));
  }

  @Test
  @DisplayName("test handleFindById: should call valid services with valid values")
  void testHandleFindById() {
    var entityConfiguration = new EntityConfiguration();
    entityConfiguration.setProvider("test");
    var providerConfiguration = new ProviderConfiguration();
    var provider = Mockito.mock(ProviderPlugin.class);
    var request = Mockito.mock(HttpServletRequest.class);
    var authPlugin = Mockito.mock(AllowAllAuthorizationPlugin.class);

    Mockito.when(factory.getAuthorizationPlugin()).thenReturn(authPlugin);
    Mockito.when(configurationService.getEntityConfiguration(Mockito.anyString())).thenReturn(Optional.of(entityConfiguration));
    Mockito.when(configurationService.getProviderConfiguration(Mockito.any()))
        .thenReturn(Optional.of(providerConfiguration));
    Mockito.when(providerFactory.getProviderByName(Mockito.anyString())).thenReturn(Optional.of(provider));
    Mockito.doNothing().when(taskEngine).execute(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doNothing().when(validationEngine).validate(Mockito.any(), Mockito.any());
    Mockito.when(provider.findById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

    service.handleFindById(request, "test", "id");

    Mockito.verify(authPlugin, Mockito.times(1)).validateToken(Mockito.any(), Mockito.any());
    Mockito.verify(authPlugin, Mockito.times(1)).isAuthorized(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.eq(
            "READ"),
        Mockito.any());
    Mockito.verify(validationEngine, Mockito.times(1)).validate(Mockito.any(), Mockito.eq("beforeFindById"));
    Mockito.verify(provider, Mockito.times(1)).findById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), Mockito.anyString());

    ArgumentCaptor<String> phaseCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), phaseCaptor.capture());

    List<String> phasesCalled = phaseCaptor.getAllValues();

    assertEquals("beforeTokenValidationFindById", phasesCalled.get(0));
    assertEquals("afterTokenValidationFindById", phasesCalled.get(1));
    assertEquals("beforePermissionValidationFindById", phasesCalled.get(2));
    assertEquals("afterPermissionValidationFindById", phasesCalled.get(3));
    assertEquals("beforeValidationFindById", phasesCalled.get(4));
    assertEquals("afterValidationFindById", phasesCalled.get(5));
    assertEquals("beforeFindById", phasesCalled.get(6));
    assertEquals("afterFindById", phasesCalled.get(7));
  }

  @Test
  @DisplayName("test handleFindAll: should call valid services with valid values")
  void testHandleFindAll() {
    var entityConfiguration = new EntityConfiguration();
    entityConfiguration.setProvider("test");
    var providerConfiguration = new ProviderConfiguration();
    var provider = Mockito.mock(ProviderPlugin.class);
    var request = Mockito.mock(HttpServletRequest.class);
    var authPlugin = Mockito.mock(AllowAllAuthorizationPlugin.class);

    Mockito.when(factory.getAuthorizationPlugin()).thenReturn(authPlugin);
    Mockito.when(configurationService.getEntityConfiguration(Mockito.anyString())).thenReturn(Optional.of(entityConfiguration));
    Mockito.when(configurationService.getProviderConfiguration(Mockito.any()))
        .thenReturn(Optional.of(providerConfiguration));
    Mockito.when(providerFactory.getProviderByName(Mockito.anyString())).thenReturn(Optional.of(provider));
    Mockito.doNothing().when(taskEngine).execute(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doNothing().when(validationEngine).validate(Mockito.any(), Mockito.any());
    Mockito.when(provider.findAll(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

    service.handleFindAll(request, "test", MultiValueMap.fromMultiValue(Map.of()), null);

    Mockito.verify(authPlugin, Mockito.times(1)).validateToken(Mockito.any(), Mockito.any());
    Mockito.verify(authPlugin, Mockito.times(1)).isAuthorized(Mockito.any(), Mockito.any(), Mockito.any(MultiValueMap.class),
        Mockito.eq("READ"),
        Mockito.any());
    Mockito.verify(validationEngine, Mockito.times(1)).validate(Mockito.any(), Mockito.eq("beforeFindAll"));
    Mockito.verify(provider, Mockito.times(1)).findAll(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), Mockito.anyString());

    ArgumentCaptor<String> phaseCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(taskEngine, Mockito.times(8)).execute(Mockito.any(), Mockito.any(), phaseCaptor.capture());

    List<String> phasesCalled = phaseCaptor.getAllValues();

    assertEquals("beforeTokenValidationFindAll", phasesCalled.get(0));
    assertEquals("afterTokenValidationFindAll", phasesCalled.get(1));
    assertEquals("beforePermissionValidationFindAll", phasesCalled.get(2));
    assertEquals("afterPermissionValidationFindAll", phasesCalled.get(3));
    assertEquals("beforeValidationFindAll", phasesCalled.get(4));
    assertEquals("afterValidationFindAll", phasesCalled.get(5));
    assertEquals("beforeFindAll", phasesCalled.get(6));
    assertEquals("afterFindAll", phasesCalled.get(7));
  }

  @Test
  @DisplayName("test updateEntityConfiguration: should throw exception without configuration")
  void testUpdateEntityConfiguration() {
    Mockito.when(configurationService.getEntityConfiguration(Mockito.anyString())).thenReturn(Optional.empty());

    var entity = new DynamicEntity();

    ApiException ex =
        assertThrows(ApiException.class, () -> service.updateEntityConfiguration(entity, "test"));
    assertEquals("error.entity.unknown", ex.getError().key());
    assertEquals(Map.of("entity", "test"), ex.getError().context());
    assertEquals(404, ex.getStatusCode());
  }

  @Test
  @DisplayName("test getProvider: should throw exception without provider")
  void testGetProvider() {
    Mockito.when(providerFactory.getProviderByName(Mockito.anyString())).thenReturn(Optional.empty());

    var entityConfiguration = new EntityConfiguration();
    entityConfiguration.setProvider("provider");
    entityConfiguration.setName("name");
    var entity = new DynamicEntity();
    entity.setConfiguration(entityConfiguration);

    ApiException ex = assertThrows(ApiException.class, () -> service.getProvider(entity));
    assertEquals("error.provider.unknown", ex.getError().key());
    assertEquals(Map.of("entity", "name", "provider", "provider"), ex.getError().context());
    assertEquals(500, ex.getStatusCode());
  }

  @Test
  @DisplayName("test getProviderConfiguration: should throw exception without configuration")
  void testGetProviderConfiguration() {
    Mockito.when(configurationService.getProviderConfiguration(Mockito.anyString())).thenReturn(Optional.empty());

    var entityConfiguration = new EntityConfiguration();
    entityConfiguration.setProvider("provider");
    entityConfiguration.setName("name");
    var entity = new DynamicEntity();
    entity.setConfiguration(entityConfiguration);

    ApiException ex = assertThrows(ApiException.class, () -> service.getProviderConfiguration(entity));
    assertEquals("error.provider.unknown", ex.getError().key());
    assertEquals(Map.of("entity", "name", "provider", "provider"), ex.getError().context());
    assertEquals(500, ex.getStatusCode());
  }
}
