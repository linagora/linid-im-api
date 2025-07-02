/*
 * Copyright (C) 2020-2025 Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version, provided you comply with the Additional Terms applicable for LinID Directory Manager software by
 * LINAGORA pursuant to Section 7 of the GNU Affero General Public License, subsections (b), (c), and (e), pursuant to
 * which these Appropriate Legal Notices must notably (i) retain the display of the "LinID™" trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source and free version of LinID™, powered by
 * Linagora © 2009–2013. Contribute to LinID R&D by subscribing to an Enterprise offer!” infobox and in the e-mails
 * sent with the Program, notice appended to any type of outbound messages (e.g. e-mail and meeting requests) as well
 * as in the LinID Directory Manager user interface, (ii) retain all hypertext links between LinID Directory Manager
 * and https://linid.org/, as well as between LINAGORA and LINAGORA.com, and (iii) refrain from infringing LINAGORA
 * intellectual property rights over its trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License and its applicable Additional Terms for
 * LinID Directory Manager along with this program. If not, see <http://www.gnu.org/licenses/> for the GNU Affero
 * General Public License version 3 and <http://www.linagora.com/licenses/> for the Additional Terms applicable to the
 * LinID Directory Manager software.
 */

package org.linagora.linid.dmapi.plugin.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.linagora.linid.dmapicore.plugin.config.dto.AttributeConfiguration;
import org.linagora.linid.dmapicore.plugin.config.dto.AuthorizationConfiguration;
import org.linagora.linid.dmapicore.plugin.config.dto.EntityConfiguration;
import org.linagora.linid.dmapicore.plugin.config.dto.ProviderConfiguration;
import org.linagora.linid.dmapicore.plugin.config.dto.RootConfiguration;
import org.linagora.linid.dmapicore.plugin.config.dto.RouteConfiguration;
import org.linagora.linid.dmapicore.plugin.config.dto.TaskConfiguration;
import org.linagora.linid.dmapicore.plugin.config.dto.ValidationConfiguration;
import org.linagora.linid.dmapicore.plugin.entity.AttributeDescription;
import org.linagora.linid.dmapicore.plugin.entity.EntityDescription;
import org.linagora.linid.dmapicore.plugin.route.RouteDescription;
import org.linagora.linid.dmapicore.plugin.route.RoutePlugin;
import org.mockito.Mockito;
import org.springframework.plugin.core.PluginRegistry;

@DisplayName("Test class: PluginConfigurationServiceImpl")
public class PluginConfigurationServiceImplTest {

  private PluginConfigurationServiceImpl service;
  private PluginConfigurationWatcher watcher;
  private PluginRegistry<RoutePlugin, String> routeRegistry;
  private RoutePlugin mockRoutePlugin;
  private String testConfigFilePath;

  @BeforeEach
  public void setUp() throws Exception {
    watcher = Mockito.mock(PluginConfigurationWatcher.class);
    routeRegistry = Mockito.mock(PluginRegistry.class);
    mockRoutePlugin = Mockito.mock(RoutePlugin.class);
    Mockito.when(routeRegistry.getPlugins()).thenReturn(List.of(mockRoutePlugin));

    // Crée un fichier YAML temporaire avec contenu minimal
    Path tempFile = Files.createTempFile("test-config", ".yaml");
    testConfigFilePath = tempFile.toAbsolutePath().toString();

    Files.writeString(tempFile, """
          entities: []
          providers: []
          routes: []
          validations: []
          tasks: []
        """);

    service = new PluginConfigurationServiceImpl(watcher, routeRegistry, testConfigFilePath);
  }

  private void injectRootConfiguration(RootConfiguration config) {
    try {
      Field field = PluginConfigurationServiceImpl.class.getDeclaredField("root");
      field.setAccessible(true);
      field.set(service, config);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to inject root configuration", e);
    }
  }

  private RootConfiguration buildTestRootConfiguration() {
    EntityConfiguration entity = new EntityConfiguration();
    entity.setRoute("users");

    ProviderConfiguration provider = new ProviderConfiguration();
    provider.setName("ldapProvider");

    RouteConfiguration route = new RouteConfiguration();
    route.setName("users");

    TaskConfiguration task = new TaskConfiguration();
    task.setName("syncTask");

    ValidationConfiguration validation = new ValidationConfiguration();
    validation.setName("validation");

    RootConfiguration root = new RootConfiguration();
    root.setEntities(List.of(entity));
    root.setProviders(List.of(provider));
    root.setRoutes(List.of(route));
    root.setTasks(List.of(task));
    root.setValidations(List.of(validation));
    return root;
  }

  private RootConfiguration getPrivateRoot(PluginConfigurationServiceImpl service) {
    try {
      var field = PluginConfigurationServiceImpl.class.getDeclaredField("root");
      field.setAccessible(true);
      return (RootConfiguration) field.get(service);
    } catch (Exception e) {
      return null;
    }
  }

  @Test
  public void shouldInitializeAndRegisterWatcher() {
    service.init();

    Mockito.verify(watcher, Mockito.times(1))
        .watch(Mockito.eq(Paths.get(testConfigFilePath)), Mockito.any(Runnable.class));

    RootConfiguration config = getPrivateRoot(service);
    assertNotNull(config);
    assertNotNull(config.getEntities());
    assertTrue(config.getEntities().isEmpty());
  }

  @Test
  public void shouldReturnEntityByName() {
    injectRootConfiguration(buildTestRootConfiguration());
    Optional<EntityConfiguration> entity = service.getEntityConfiguration("users");
    assertTrue(entity.isPresent());
    assertEquals("users", entity.get().getRoute());
  }

  @Test
  public void shouldReturnProviderByName() {
    injectRootConfiguration(buildTestRootConfiguration());
    Optional<ProviderConfiguration> provider = service.getProviderConfiguration("ldapProvider");
    assertTrue(provider.isPresent());
    assertEquals("ldapProvider", provider.get().getName());
  }

  @Test
  public void shouldReturnRouteConfigurations() {
    injectRootConfiguration(buildTestRootConfiguration());
    List<RouteConfiguration> routes = service.getRoutesConfiguration();
    assertEquals(1, routes.size());
    assertEquals("users", routes.getFirst().getName());
  }

  @Test
  public void shouldReturnTaskByName() {
    injectRootConfiguration(buildTestRootConfiguration());
    Optional<TaskConfiguration> task = service.getTaskConfiguration("syncTask");
    assertTrue(task.isPresent());
    assertEquals("syncTask", task.get().getName());
  }

  @Test
  public void shouldReturnValidationByName() {
    injectRootConfiguration(buildTestRootConfiguration());
    Optional<ValidationConfiguration> validation = service.getValidationConfiguration("validation");
    assertTrue(validation.isPresent());
    assertEquals("validation", validation.get().getName());
  }

  @Test
  public void shouldLoadConfigurationSuccessfully() {
    RootConfiguration config = service.loadConfiguration();
    assertNotNull(config, "Root configuration should not be null");

    assertNotNull(config.getEntities(), "Entities list should not be null");
    assertTrue(config.getEntities().isEmpty(), "Entities list should be empty");

    assertNotNull(config.getProviders(), "Providers list should not be null");
    assertTrue(config.getProviders().isEmpty(), "Providers list should be empty");

    assertNotNull(config.getRoutes(), "Routes list should not be null");
    assertTrue(config.getRoutes().isEmpty(), "Routes list should be empty");

    assertNotNull(config.getTasks(), "Tasks list should not be null");
    assertTrue(config.getTasks().isEmpty(), "Tasks list should be empty");
  }

  @Test
  public void shouldReturnEmptyWhenConfigIsNull() {
    Optional<EntityConfiguration> entity = service.getEntityConfiguration("test");
    assertTrue(entity.isEmpty());

    Optional<ProviderConfiguration> provider = service.getProviderConfiguration("provider");
    assertTrue(provider.isEmpty());

    Optional<TaskConfiguration> task = service.getTaskConfiguration("task");
    assertTrue(task.isEmpty());

    List<RouteConfiguration> routes = service.getRoutesConfiguration();
    assertNotNull(routes);
    assertTrue(routes.isEmpty());
  }

  @Test
  public void shouldReturnRouteDescriptionsIncludingStaticAndDynamicRoutes() {
    EntityConfiguration entity = new EntityConfiguration();
    entity.setName("User");
    entity.setRoute("users");

    RootConfiguration root = new RootConfiguration();
    root.setEntities(List.of(entity));
    injectRootConfiguration(root);

    List<RouteDescription> pluginRoutes = List.of(
        new RouteDescription("GET", "/custom/plugin", "User", List.of())
    );
    Mockito.when(mockRoutePlugin.getRoutes(root.getEntities())).thenReturn(pluginRoutes);

    List<RouteDescription> descriptions = service.getRouteDescriptions();

    assertTrue(descriptions.stream().anyMatch(r -> r.path().equals("/actuator/health")));
    assertTrue(descriptions.stream().anyMatch(r -> r.path().equals("/api/users/{id}")));
    assertTrue(descriptions.stream().anyMatch(r -> r.path().equals("/custom/plugin")));
  }

  @Test
  public void shouldReturnExcludeDisabledRoutes() {
    EntityConfiguration entity = new EntityConfiguration();
    entity.setName("User");
    entity.setRoute("users");
    entity.setDisabledRoutes(List.of("create", "update", "patch", "delete", "findById", "findAll"));

    RootConfiguration root = new RootConfiguration();
    root.setEntities(List.of(entity));
    injectRootConfiguration(root);

    List<RouteDescription> pluginRoutes = List.of(
        new RouteDescription("GET", "/custom/plugin", "User", List.of())
    );
    Mockito.when(mockRoutePlugin.getRoutes(root.getEntities())).thenReturn(pluginRoutes);

    List<RouteDescription> descriptions = service.getRouteDescriptions();

    assertTrue(descriptions.stream().anyMatch(r -> r.path().equals("/actuator/health")));
    assertTrue(descriptions.stream().noneMatch(r -> r.path().startsWith("/api/users")));
    assertTrue(descriptions.stream().anyMatch(r -> r.path().equals("/custom/plugin")));
  }

  @Test
  public void shouldReturnEntityDescriptionsFromConfiguration() {
    AttributeConfiguration attr = new AttributeConfiguration();
    attr.setName("uid");
    attr.setType("string");
    attr.setRequired(true);
    attr.setValidations(List.of(new ValidationConfiguration()));
    attr.setInput("text");
    attr.setInputSettings(Map.of());

    EntityConfiguration entity = new EntityConfiguration();
    entity.setName("User");
    entity.setRoute("users");
    entity.setAttributes(List.of(attr));

    RootConfiguration root = new RootConfiguration();
    root.setEntities(List.of(entity));
    injectRootConfiguration(root);

    List<EntityDescription> result = service.getEntityDescriptions();

    assertEquals(1, result.size());
    EntityDescription desc = result.getFirst();
    assertEquals("User", desc.name());

    AttributeDescription attribute = desc.attributes().getFirst();
    assertEquals("uid", attribute.name());
    assertEquals("string", attribute.type());
    assertTrue(attribute.required());
    assertTrue(attribute.hasValidations());
    assertEquals("text", attribute.input());
  }

  @Test
  public void shouldReturnEntityDescriptionByName() {
    AttributeConfiguration attr = new AttributeConfiguration();
    attr.setName("email");
    attr.setType("string");
    attr.setRequired(false);
    attr.setValidations(List.of());
    attr.setInput("email");
    attr.setInputSettings(Map.of("placeholder", "user@example.com"));

    EntityConfiguration entity = new EntityConfiguration();
    entity.setName("Account");
    entity.setRoute("accounts");
    entity.setAttributes(List.of(attr));

    RootConfiguration root = new RootConfiguration();
    root.setEntities(List.of(entity));
    injectRootConfiguration(root);

    Optional<EntityDescription> opt = service.getEntityDescription("accounts");

    assertTrue(opt.isPresent());
    EntityDescription desc = opt.get();
    assertEquals("Account", desc.name());
    AttributeDescription attribute = desc.attributes().getFirst();
    assertEquals("email", attribute.name());
    assertFalse(attribute.required());
    assertFalse(attribute.hasValidations());
    assertEquals("email", attribute.input());
  }

  @Test
  public void shouldReturnAuthorizationConfigurationIfPresent() {
    AuthorizationConfiguration authConfig = new AuthorizationConfiguration();
    authConfig.setType("mockAuth");

    RootConfiguration root = new RootConfiguration();
    root.setAuthorization(authConfig);
    injectRootConfiguration(root);

    Optional<AuthorizationConfiguration> result = service.getAuthorizationConfiguration();

    assertTrue(result.isPresent());
    assertEquals("mockAuth", result.get().getType());
  }

  @Test
  public void shouldReturnEmptyIfAuthorizationConfigAbsent() {
    injectRootConfiguration(new RootConfiguration());
    Optional<AuthorizationConfiguration> result = service.getAuthorizationConfiguration();
    assertTrue(result.isEmpty());
  }
}
