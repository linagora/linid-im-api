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

package io.github.linagora.linid.im.plugin.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.linagora.linid.im.corelib.plugin.authorization.AuthorizationPlugin;
import io.github.linagora.linid.im.corelib.plugin.provider.ProviderPlugin;
import io.github.linagora.linid.im.corelib.plugin.route.RoutePlugin;
import io.github.linagora.linid.im.corelib.plugin.task.TaskPlugin;
import io.github.linagora.linid.im.corelib.plugin.validation.ValidationPlugin;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.plugin.core.PluginRegistry;

@DisplayName("Test class: PluginRegistryConfiguration")
public class PluginRegistryConfigurationTest {

  private PluginLoaderService pluginLoaderService;
  private PluginRegistryConfiguration configuration;

  @BeforeEach
  void setUp() {
    pluginLoaderService = Mockito.mock(PluginLoaderService.class);
    configuration = new PluginRegistryConfiguration(pluginLoaderService);
  }

  @Test
  @DisplayName("Should return registry containing all loaded ProviderPlugins")
  void providerRegistry_shouldContainLoadedProviderPlugins() {
    ProviderPlugin plugin1 = Mockito.mock(ProviderPlugin.class);
    ProviderPlugin plugin2 = Mockito.mock(ProviderPlugin.class);
    Mockito.when(pluginLoaderService.getProviderPlugins()).thenReturn(List.of(plugin1, plugin2));

    PluginRegistry<ProviderPlugin, String> registry = configuration.providerRegistry();

    assertEquals(List.of(plugin1, plugin2), registry.getPlugins());
  }

  @Test
  @DisplayName("Should return registry containing all loaded RoutePlugins")
  void routeRegistry_shouldContainLoadedRoutePlugins() {
    RoutePlugin plugin1 = Mockito.mock(RoutePlugin.class);
    RoutePlugin plugin2 = Mockito.mock(RoutePlugin.class);
    Mockito.when(pluginLoaderService.getRoutePlugins()).thenReturn(List.of(plugin1, plugin2));

    PluginRegistry<RoutePlugin, String> registry = configuration.routeRegistry();

    assertEquals(List.of(plugin1, plugin2), registry.getPlugins());
  }

  @Test
  @DisplayName("Should return registry containing all loaded TaskPlugins")
  void taskRegistry_shouldContainLoadedTaskPlugins() {
    TaskPlugin plugin1 = Mockito.mock(TaskPlugin.class);
    TaskPlugin plugin2 = Mockito.mock(TaskPlugin.class);
    Mockito.when(pluginLoaderService.getTaskPlugins()).thenReturn(List.of(plugin1, plugin2));

    PluginRegistry<TaskPlugin, String> registry = configuration.taskRegistry();

    assertEquals(List.of(plugin1, plugin2), registry.getPlugins());
  }

  @Test
  @DisplayName("Should return registry containing all loaded ValidationPlugins")
  void validationRegistry_shouldContainLoadedValidationPlugins() {
    ValidationPlugin plugin1 = Mockito.mock(ValidationPlugin.class);
    ValidationPlugin plugin2 = Mockito.mock(ValidationPlugin.class);
    Mockito.when(pluginLoaderService.getValidationPlugins()).thenReturn(List.of(plugin1, plugin2));

    PluginRegistry<ValidationPlugin, String> registry = configuration.validationRegistry();

    assertEquals(List.of(plugin1, plugin2), registry.getPlugins());
  }

  @Test
  @DisplayName("Should return registry containing all loaded AuthorizationPlugin")
  void validationRegistry_shouldContainLoadedAuthorizationPlugins() {
    AuthorizationPlugin plugin1 = Mockito.mock(AuthorizationPlugin.class);
    AuthorizationPlugin plugin2 = Mockito.mock(AuthorizationPlugin.class);
    Mockito.when(pluginLoaderService.getAuthorizationPlugins()).thenReturn(List.of(plugin1, plugin2));

    PluginRegistry<AuthorizationPlugin, String> registry = configuration.authorizationRegistry();

    assertEquals(List.of(plugin1, plugin2), registry.getPlugins());
  }
}
