/*
 * Copyright (C) 2020-2026 Linagora
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

import io.github.linagora.linid.im.corelib.plugin.authorization.AuthorizationPlugin;
import io.github.linagora.linid.im.corelib.plugin.provider.ProviderPlugin;
import io.github.linagora.linid.im.corelib.plugin.route.RoutePlugin;
import io.github.linagora.linid.im.corelib.plugin.task.TaskPlugin;
import io.github.linagora.linid.im.corelib.plugin.validation.ValidationPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.PluginRegistry;

/**
 * Configuration class responsible for creating and exposing {@link PluginRegistry} beans for different plugin types.
 *
 * <p>
 * It uses the {@link PluginLoaderService} to obtain the collections of provider, route, task, and validation plugins, then
 * creates registries wrapping those plugins keyed by their unique identifiers.
 */
@Configuration
public class PluginRegistryConfiguration {
  /**
   * Service responsible for loading all available plugin implementations. Used to retrieve collections of plugins for
   * registration.
   */
  private final PluginLoaderService pluginLoaderService;

  /**
   * Constructs a new PluginRegistryConfiguration with the given PluginLoaderService.
   *
   * @param pluginLoaderService the service used to load various plugin implementations
   */
  public PluginRegistryConfiguration(PluginLoaderService pluginLoaderService) {
    this.pluginLoaderService = pluginLoaderService;
  }

  /**
   * Creates a {@link PluginRegistry} containing all loaded {@link ProviderPlugin} instances.
   *
   * @return a plugin registry of provider plugins keyed by their identifier
   */
  @Bean
  public PluginRegistry<ProviderPlugin, String> providerRegistry() {
    return PluginRegistry.of(pluginLoaderService.getProviderPlugins());
  }

  /**
   * Creates a {@link PluginRegistry} containing all loaded {@link RoutePlugin} instances.
   *
   * @return a plugin registry of route plugins keyed by their identifier
   */
  @Bean
  public PluginRegistry<RoutePlugin, String> routeRegistry() {
    return PluginRegistry.of(pluginLoaderService.getRoutePlugins());
  }

  /**
   * Creates a {@link PluginRegistry} containing all loaded {@link TaskPlugin} instances.
   *
   * @return a plugin registry of task plugins keyed by their identifier
   */
  @Bean
  public PluginRegistry<TaskPlugin, String> taskRegistry() {
    return PluginRegistry.of(pluginLoaderService.getTaskPlugins());
  }

  /**
   * Creates a {@link PluginRegistry} containing all loaded {@link ValidationPlugin} instances.
   *
   * @return a plugin registry of validation plugins keyed by their identifier
   */
  @Bean
  public PluginRegistry<ValidationPlugin, String> validationRegistry() {
    return PluginRegistry.of(pluginLoaderService.getValidationPlugins());
  }

  /**
   * Creates a {@link PluginRegistry} containing all loaded {@link AuthorizationPlugin} instances.
   *
   * @return a plugin registry of authorization plugins keyed by their identifier
   */
  @Bean
  public PluginRegistry<AuthorizationPlugin, String> authorizationRegistry() {
    return PluginRegistry.of(pluginLoaderService.getAuthorizationPlugins());
  }
}
