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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.linagora.linid.dmapicore.plugin.authorization.AllowAllAuthorizationPlugin;
import org.linagora.linid.dmapicore.plugin.authorization.AuthorizationPlugin;
import org.linagora.linid.dmapicore.plugin.authorization.DenyAllAuthorizationPlugin;
import org.linagora.linid.dmapicore.plugin.provider.ProviderPlugin;
import org.linagora.linid.dmapicore.plugin.route.RoutePlugin;
import org.linagora.linid.dmapicore.plugin.task.TaskPlugin;
import org.linagora.linid.dmapicore.plugin.validation.ValidationPlugin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.lang.NonNull;
import org.springframework.plugin.core.Plugin;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

/**
 * Service responsible for dynamically loading and registering plugins from JAR files located in a configured directory at
 * application startup.
 *
 * <p>
 * This service scans the specified directory for plugin JAR files, then for each JAR:
 * <ul>
 *   <li>Creates a dedicated class loader</li>
 *   <li>Searches for Spring-annotated classes (@Component, @Service, @Repository, @Configuration)</li>
 *   <li>Loads configuration properties (application.properties or application.yml) from the JAR into the Spring environment</li>
 *   <li>Initializes a child Spring application context for the plugin with the main context as parent</li>
 *   <li>Registers discovered plugin beans implementing known plugin interfaces (ProviderPlugin, RoutePlugin, TaskPlugin, ValidationPlugin)</li>
 * </ul>
 *
 * <p>
 * This allows the main application to discover and use plugins without them being part of the main classpath.
 * Plugins are isolated by separate class loaders and have their own Spring contexts, but inherit configuration
 * and beans from the main application context.
 *
 * <p>
 * The plugin directory path is configured by the {@code plugin.loader.directory} property.
 *
 * <p>
 * Implements {@link ApplicationContextAware} to obtain the main Spring context,
 * and {@link CommandLineRunner} to trigger plugin loading during application startup.
 */
@Service
@Slf4j
public class PluginLoaderService implements ApplicationContextAware, CommandLineRunner {

  /**
   * Property source name used for properties loaded from plugin application.properties files.
   */
  private static final String PROPERTY_SOURCE_PROPERTIES = "pluginProperties";

  /**
   * Property source name used for properties loaded from plugin application.yml files.
   */
  private static final String PROPERTY_SOURCE_YAML = "pluginYamlProperties";

  /**
   * List of loaded provider plugins implementing {@link ProviderPlugin}.
   */
  private final List<ProviderPlugin> providerPlugins = new ArrayList<>();

  /**
   * List of loaded route plugins implementing {@link RoutePlugin}.
   */
  private final List<RoutePlugin> routePlugins = new ArrayList<>();

  /**
   * List of loaded task plugins implementing {@link TaskPlugin}.
   */
  private final List<TaskPlugin> taskPlugins = new ArrayList<>();

  /**
   * List of loaded validation plugins implementing {@link ValidationPlugin}.
   */
  private final List<ValidationPlugin> validationPlugins = new ArrayList<>();

  /**
   * List of loaded authorization plugins implementing {@link AuthorizationPlugin}.
   */
  private final List<AuthorizationPlugin> authorizationPlugins = new ArrayList<>();

  /**
   * The directory path where plugin JAR files are located. Injected from configuration property {@code plugin.loader.directory}.
   */
  @Value("${plugin.loader.path}")
  private String pluginDirectoryPath;

  /**
   * Flag indicating whether the service should accept any authorization header. Injected from configuration property
   * {@code authorization.accept.allow.all}.
   */
  @Value("${authorization.accept.allow.all}")
  private boolean acceptAllowAll;

  /**
   * The main Spring application context, used as parent context for plugin contexts.
   */
  private ConfigurableApplicationContext mainContext;

  /**
   * Returns an unmodifiable list of all loaded provider plugins.
   *
   * @return list of loaded provider plugins
   */
  public List<ProviderPlugin> getProviderPlugins() {
    return Collections.unmodifiableList(providerPlugins);
  }

  /**
   * Returns an unmodifiable list of all loaded route plugins.
   *
   * @return list of loaded route plugins
   */
  public List<RoutePlugin> getRoutePlugins() {
    return Collections.unmodifiableList(routePlugins);
  }

  /**
   * Returns an unmodifiable list of all loaded task plugins.
   *
   * @return list of loaded task plugins
   */
  public List<TaskPlugin> getTaskPlugins() {
    return Collections.unmodifiableList(taskPlugins);
  }

  /**
   * Returns an unmodifiable list of all loaded validation plugins.
   *
   * @return list of loaded validation plugins
   */
  public List<ValidationPlugin> getValidationPlugins() {
    return Collections.unmodifiableList(validationPlugins);
  }

  /**
   * Returns an unmodifiable list of all loaded authorization plugins.
   *
   * @return list of loaded authorization plugins
   */
  public List<AuthorizationPlugin> getAuthorizationPlugins() {
    return Collections.unmodifiableList(authorizationPlugins);
  }

  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
    this.mainContext = (ConfigurableApplicationContext) applicationContext;
  }

  @Override
  public void run(String... args) {
    loadPluginsFromDirectory(new File(pluginDirectoryPath));
  }

  /**
   * Loads plugins from the specified directory. Scans for JAR files and loads plugin beans from each valid JAR.
   *
   * @param pluginDir the directory containing plugin JAR files
   */
  public void loadPluginsFromDirectory(File pluginDir) {
    if (pluginDir == null || !pluginDir.isDirectory()) {
      log.warn("Plugin directory is invalid or does not exist: {}", pluginDir);
      return;
    }

    File[] jars = Optional.ofNullable(pluginDir.listFiles((dir, name) -> name.endsWith(".jar")))
        .orElse(new File[0]);

    Stream.of(jars).forEach(this::loadAndRegisterPluginBeans);

    this.authorizationPlugins.add(new DenyAllAuthorizationPlugin());
    if (acceptAllowAll) {
      log.warn("The 'allow-all' authorization plugin is permitted by configuration — this may allow bypassing access controls.");
      this.authorizationPlugins.add(new AllowAllAuthorizationPlugin());
    }

  }

  /**
   * Loads and registers Spring plugin beans found in the specified plugin JAR file. This method:
   * <ul>
   *   <li>Creates a class loader for the JAR</li>
   *   <li>Finds all Spring-annotated classes in the JAR</li>
   *   <li>Creates a child application context and scans those classes</li>
   *   <li>Loads plugin configuration properties from the JAR into the environment</li>
   *   <li>Registers beans implementing known plugin interfaces</li>
   * </ul>
   *
   * @param jarFile the plugin JAR file to load
   */
  public void loadAndRegisterPluginBeans(File jarFile) {
    URLClassLoader pluginClassLoader = createClassLoader(jarFile);
    if (pluginClassLoader == null) {
      return;
    }

    Set<Class<?>> annotatedClasses = findAnnotatedClasses(jarFile, pluginClassLoader);
    if (annotatedClasses.isEmpty()) {
      log.warn("⚠️ No annotated Spring classes found in plugin: {}", jarFile.getName());
      return;
    }

    Set<String> basePackages = annotatedClasses.stream()
        .map(clazz -> clazz.getPackage().getName())
        .collect(Collectors.toSet());

    AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext();
    pluginContext.setParent(mainContext);
    pluginContext.setClassLoader(pluginClassLoader);

    loadPropertiesIntoEnvironment(jarFile, pluginContext.getEnvironment());

    basePackages.forEach(pluginContext::scan);
    pluginContext.refresh();

    registerPlugins(pluginContext);
  }

  /**
   * Creates a URLClassLoader for the given plugin JAR file.
   *
   * @param jarFile the plugin JAR file
   * @return a URLClassLoader instance or {@code null} if an error occurs
   */
  public URLClassLoader createClassLoader(File jarFile) {
    try {
      URL[] urls = new URL[] {jarFile.toURI().toURL()};
      return new URLClassLoader(urls, this.getClass().getClassLoader());
    } catch (MalformedURLException e) {
      log.error("Failed to create class loader for plugin jar: {}", jarFile.getName(), e);
      return null;
    }
  }

  /**
   * Loads property sources (application.properties or application.yml) from the plugin JAR into the given Spring environment.
   *
   * @param jarFile the plugin JAR file
   * @param environment the Spring environment to add property sources to
   */
  public void loadPropertiesIntoEnvironment(File jarFile, ConfigurableEnvironment environment) {
    MutablePropertySources propertySources = environment.getPropertySources();

    try (JarFile jar = new JarFile(jarFile)) {
      Pattern pattern = Pattern.compile("application\\.(properties|ya?ml)");

      Collections.list(jar.entries()).stream()
          .filter(entry -> pattern.matcher(entry.getName()).matches())
          .forEach(entry -> loadSinglePropertySource(jarFile, entry, propertySources));
    } catch (IOException e) {
      log.error("Error reading jar file for properties: {}", jarFile.getName(), e);
    }
  }

  /**
   * Loads a single property source from a jar entry into the given property sources. Supports both properties and YAML files.
   *
   * @param jarFile the plugin JAR file
   * @param entry the JarEntry for the properties or yaml file
   * @param propertySources the collection of property sources to add to
   */
  public void loadSinglePropertySource(File jarFile, JarEntry entry, MutablePropertySources propertySources) {
    String name = entry.getName();
    String extension = name.substring(name.lastIndexOf('.') + 1);

    try {
      URL jarUrl = jarFile.toURI().toURL();
      URL resourceUrl = URI.create("jar:" + jarUrl + "!/" + name).toURL();

      try (InputStream inputStream = resourceUrl.openStream()) {
        if ("properties".equalsIgnoreCase(extension)) {
          Properties props = new Properties();
          props.load(inputStream);
          propertySources.addLast(new PropertiesPropertySource(PROPERTY_SOURCE_PROPERTIES, props));
        } else {
          Map<String, Object> yamlMap = new Yaml().load(inputStream);
          if (yamlMap != null) {
            propertySources.addLast(new MapPropertySource(PROPERTY_SOURCE_YAML, yamlMap));
          }
        }
        log.info("✅ Loaded {} from plugin: {}", name, jarFile.getName());
      }
    } catch (IOException e) {
      log.warn("Failed to load property source '{}' from plugin: {}", name, jarFile.getName(), e);
    }
  }

  /**
   * Scans the given plugin JAR file to find all classes annotated with Spring stereotype annotations: {@code @Component},
   * {@code @Service}, {@code @Repository}, or {@code @Configuration}.
   *
   * @param jarFile the plugin JAR file to scan
   * @param loader the class loader to use for loading classes
   * @return a set of annotated classes found in the plugin
   */
  public Set<Class<?>> findAnnotatedClasses(File jarFile, ClassLoader loader) {
    Set<Class<?>> annotatedClasses = new HashSet<>();

    try (JarFile jar = new JarFile(jarFile)) {
      Collections.list(jar.entries()).stream()
          .filter(entry -> !entry.isDirectory())
          .filter(entry -> entry.getName().endsWith(".class"))
          .filter(entry -> !entry.getName().contains("module-info"))
          .map(entry -> entry.getName().replace('/', '.').replace('\\', '.'))
          .map(className -> className.substring(0, className.length() - ".class".length()))
          .map(className -> {
            try {
              return Class.forName(className, false, loader);
            } catch (ClassNotFoundException e) {
              log.debug("Class not found: {}", className);
              return null;
            }
          })
          .filter(Objects::nonNull)
          .filter(clazz -> clazz.isAnnotationPresent(Component.class)
              || clazz.isAnnotationPresent(Service.class)
              || clazz.isAnnotationPresent(Repository.class)
              || clazz.isAnnotationPresent(Configuration.class))
          .forEach(annotatedClasses::add);
    } catch (IOException e) {
      log.error("Failed to read jar file to find annotated classes: {}", jarFile.getName(), e);
    }

    return annotatedClasses;
  }

  /**
   * Registers plugin beans implementing known plugin interfaces found in the given plugin context. Plugins are added to the
   * corresponding internal plugin lists and logged.
   *
   * @param pluginContext the Spring context containing plugin beans
   */
  public void registerPlugins(AnnotationConfigApplicationContext pluginContext) {
    pluginContext.getBeansOfType(Plugin.class).forEach((name, plugin) -> {
      boolean loaded = true;

      if (plugin instanceof ProviderPlugin providerPlugin) {
        providerPlugins.add(providerPlugin);
      } else if (plugin instanceof RoutePlugin routePlugin) {
        routePlugins.add(routePlugin);
      } else if (plugin instanceof TaskPlugin taskPlugin) {
        taskPlugins.add(taskPlugin);
      } else if (plugin instanceof ValidationPlugin validationPlugin) {
        validationPlugins.add(validationPlugin);
      } else if (plugin instanceof AuthorizationPlugin authorizationPlugin) {
        authorizationPlugins.add(authorizationPlugin);
      } else {
        loaded = false;
      }

      if (loaded) {
        log.info("✅ Loaded plugin: {} => {}", name, plugin.getClass());
      }
    });
  }
}
