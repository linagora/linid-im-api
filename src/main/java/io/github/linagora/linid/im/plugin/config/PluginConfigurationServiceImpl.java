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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.config.dto.AuthorizationConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.EntityConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.ProviderConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.RootConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.RouteConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.TaskConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.ValidationConfiguration;
import io.github.linagora.linid.im.corelib.plugin.entity.AttributeDescription;
import io.github.linagora.linid.im.corelib.plugin.entity.EntityDescription;
import io.github.linagora.linid.im.corelib.plugin.route.RouteDescription;
import io.github.linagora.linid.im.corelib.plugin.route.RoutePlugin;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link PluginConfigurationService} responsible for loading and managing the plugin configuration from a
 * YAML file. This service loads the configuration at startup and watches the configuration file for changes to reload it
 * dynamically.
 *
 * <p>
 * The configuration file path is configurable via the property {@code config.entities.path} (default:
 * {@code config/configuration.yaml}).
 *
 * <p>
 * When the configuration file changes, the watcher triggers a reload and logs a notification message.
 * changes.
 */
@Slf4j
@Service
public class PluginConfigurationServiceImpl implements PluginConfigurationService {

  /**
   * Service responsible for watching configuration file changes and triggering corresponding actions.
   */
  private final PluginConfigurationWatcher watcher;

  /**
   * Registry of available {@link RoutePlugin} instances. Used to resolve the correct plugin based on the incoming request.
   */
  private final PluginRegistry<RoutePlugin, String> routeRegistry;

  /**
   * Path to the YAML configuration file for entities. Configurable via the property {@code config.entities.path}.
   */
  private final String configFilePath;

  /**
   * Root configuration loaded from the YAML file. Contains all configurations for entities, providers, routes, and tasks.
   */
  private RootConfiguration root;


  /**
   * Constructs a new PluginConfigurationServiceImpl.
   *
   * @param watcher the watcher service that monitors configuration file changes
   * @param configFilePath the path to the configuration YAML file
   */
  @Autowired
  public PluginConfigurationServiceImpl(final PluginConfigurationWatcher watcher,
                                        final PluginRegistry<RoutePlugin, String> routeRegistry,
                                        final @Value("${configuration.path}") String configFilePath) {
    this.watcher = watcher;
    this.routeRegistry = routeRegistry;
    this.configFilePath = configFilePath;
  }

  /**
   * Initializes the service by loading the initial configuration and starting the watcher to listen for configuration file
   * changes. Reloads configuration automatically when a change is detected.
   */
  @PostConstruct
  public void init() {
    this.root = loadConfiguration();

    watcher.watch(Paths.get(configFilePath), () -> {
      log.info("Configuration change detected");
      this.root = this.loadConfiguration();
    });
  }

  /**
   * Loads the plugin configuration from the YAML file specified by {@code configFilePath}. If loading fails, logs an error and
   * returns an empty {@link RootConfiguration}.
   *
   * @return the loaded {@link RootConfiguration} or an empty configuration if loading failed
   */
  public RootConfiguration loadConfiguration() {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper.findAndRegisterModules();
      return mapper.readValue(new File(configFilePath), RootConfiguration.class);
    } catch (IOException e) {
      log.error("Failed to load configuration file: {}", configFilePath, e);
    }
    return null;
  }

  @Override
  public Optional<EntityConfiguration> getEntityConfiguration(String name) {
    if (root == null) {
      return Optional.empty();
    }

    return root.getEntities()
        .stream()
        .filter(entity -> entity.getRoute().equals(name))
        .findAny();
  }

  @Override
  public Optional<ProviderConfiguration> getProviderConfiguration(String name) {
    if (root == null) {
      return Optional.empty();
    }
    return root.getProviders()
        .stream()
        .filter(provider -> provider.getName().equals(name))
        .findAny();
  }

  @Override
  public List<RouteConfiguration> getRoutesConfiguration() {
    if (root == null) {
      return List.of();
    }

    return root.getRoutes();
  }

  @Override
  public Optional<TaskConfiguration> getTaskConfiguration(String name) {
    if (root == null) {
      return Optional.empty();
    }
    return root.getTasks()
        .stream()
        .filter(route -> route.getName().equals(name))
        .findAny();
  }

  @Override
  public Optional<ValidationConfiguration> getValidationConfiguration(String name) {
    if (root == null) {
      return Optional.empty();
    }
    return root.getValidations()
        .stream()
        .filter(route -> route.getName().equals(name))
        .findAny();
  }

  @Override
  public List<RouteDescription> getRouteDescriptions() {
    List<RouteDescription> routeDescriptions = new ArrayList<>();
    routeDescriptions.add(new RouteDescription("GET", "/actuator/health", null, List.of()));
    routeDescriptions.add(new RouteDescription("GET", "/i18n/languages", null, List.of()));
    routeDescriptions.add(new RouteDescription("GET", "/i18n/{language}.json", null, List.of("language")));
    routeDescriptions.add(new RouteDescription("GET", "/metadata/routes", null, List.of()));
    routeDescriptions.add(new RouteDescription("GET", "/metadata/entities", null, List.of()));

    final String defaultRoutePattern = "/api/%s";
    final String routeWithIdPattern = "/api/%s/{id}";
    final String validateAttributePattern = "/api/%s/validate/{attributeName}";

    this.root.getEntities().forEach(entity -> {
      routeDescriptions.add(
          new RouteDescription("GET", String.format("/metadata/entities/%s", entity.getName()), entity.getName(),
              List.of()));

      if (!entity.getDisabledRoutes().contains("create")) {
        routeDescriptions.add(new RouteDescription("POST", String.format(
            defaultRoutePattern,
            entity.getRoute()),
            entity.getName(),
            List.of()
        ));
      }

      if (!entity.getDisabledRoutes().contains("findAll")) {
        routeDescriptions.add(
            new RouteDescription("GET", String.format(defaultRoutePattern, entity.getRoute()), entity.getName(), List.of()));
      }

      if (!entity.getDisabledRoutes().contains("findById")) {
        routeDescriptions.add(
            new RouteDescription("GET", String.format(routeWithIdPattern, entity.getRoute()), entity.getName(), List.of("id")));
      }

      if (!entity.getDisabledRoutes().contains("update")) {
        routeDescriptions.add(
            new RouteDescription("PUT", String.format(routeWithIdPattern, entity.getRoute()), entity.getName(), List.of("id")));
      }

      if (!entity.getDisabledRoutes().contains("patch")) {
        routeDescriptions.add(
            new RouteDescription("PATCH", String.format(routeWithIdPattern, entity.getRoute()), entity.getName(), List.of("id")));
      }

      if (!entity.getDisabledRoutes().contains("delete")) {
        routeDescriptions.add(
            new RouteDescription("DELETE", String.format(
                routeWithIdPattern,
                entity.getRoute()),
                entity.getName(),
                List.of("id")
            ));
      }

      if (!entity.getDisabledRoutes().contains("validate")) {
        routeDescriptions.add(
            new RouteDescription(
                "POST",
                String.format(validateAttributePattern, entity.getRoute()),
                entity.getName(),
                List.of("attributeName")
            ));
      }
    });

    this.routeRegistry.getPlugins().forEach(plugin -> routeDescriptions
        .addAll(plugin.getRoutes(root.getEntities())));

    routeDescriptions.sort(Comparator.comparing(RouteDescription::path));

    return routeDescriptions;
  }

  @Override
  public List<EntityDescription> getEntityDescriptions() {
    return this.root.getEntities()
        .stream()
        .map(EntityConfiguration::getRoute)
        .map(this::getEntityDescription)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  @Override
  public Optional<EntityDescription> getEntityDescription(String name) {
    var opt = this.getEntityConfiguration(name);
    if (opt.isEmpty()) {
      return Optional.empty();
    }

    var configuration = opt.get();

    return Optional.of(new EntityDescription(
        configuration.getName(),
        configuration.getAttributes()
            .stream()
            .map(attribute -> new AttributeDescription(
                attribute.getName(),
                attribute.getType(),
                attribute.getRequired(),
                !attribute.getValidations().isEmpty(),
                attribute.getInput(),
                attribute.getInputSettings()
            ))
            .toList()
    ));
  }

  @Override
  public Optional<AuthorizationConfiguration> getAuthorizationConfiguration() {
    return Optional.ofNullable(this.root.getAuthorization());
  }
}
