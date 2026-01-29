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

package io.github.linagora.linid.im.plugin.task;

import io.github.linagora.linid.im.corelib.exception.ApiException;
import io.github.linagora.linid.im.corelib.i18n.I18nMessage;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.config.dto.TaskConfiguration;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import io.github.linagora.linid.im.corelib.plugin.task.TaskEngine;
import io.github.linagora.linid.im.corelib.plugin.task.TaskExecutionContext;
import io.github.linagora.linid.im.corelib.plugin.task.TaskPlugin;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link TaskEngine} interface responsible for executing task plugins based on a dynamic entity's
 * configuration.
 *
 * <p>
 * This engine finds and runs all {@link TaskPlugin} instances that match the given execution {@code phase}, using their merged
 * configurations (global + entity-specific).
 *
 * <p>
 * Plugins are resolved using a {@link PluginRegistry} and configured using data from the {@link PluginConfigurationService}.
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskEngineImpl implements TaskEngine {
  /**
   * Registry of available task plugins.
   */
  private final PluginRegistry<TaskPlugin, String> taskRegistry;
  /**
   * Provides access to global plugin configurations.
   */
  private final PluginConfigurationService configurationService;

  @Override
  public void execute(DynamicEntity dynamicEntity, TaskExecutionContext context, String phase) {
    var configuration = dynamicEntity.getConfiguration();
    if (configuration == null) {
      return;
    }
    configuration.getTasks()
        .stream()
        .filter(task -> task.getPhases().contains(phase))
        .map(this::mergeConfigurationWithGlobal)
        .forEach(task -> getPlugin(task)
            .execute(task, dynamicEntity, context));
  }

  /**
   * Retrieves the plugin corresponding to the task configuration.
   *
   * @param configuration the task configuration
   * @return the matching task plugin
   * @throws ApiException if no matching plugin is found
   */
  public TaskPlugin getPlugin(TaskConfiguration configuration) {
    return taskRegistry.getPlugins()
        .stream()
        .filter(taskPlugin -> taskPlugin.supports(configuration.getType()))
        .findFirst()
        .orElseThrow(() -> new ApiException(400, I18nMessage.of(
            "error.plugin.unknown",
            Map.of("type", configuration.getType())
        )));
  }

  /**
   * Merges the task configuration defined on the entity with the global configuration (if any).
   *
   * <p>
   * Options defined at the entity level override or supplement those from the global configuration.
   *
   * @param taskConfiguration the configuration to merge
   * @return the merged configuration
   */
  public TaskConfiguration mergeConfigurationWithGlobal(TaskConfiguration taskConfiguration) {
    var opt = configurationService.getTaskConfiguration(taskConfiguration.getName());

    if (opt.isEmpty()) {
      return taskConfiguration;
    }

    var globalConfiguration = opt.get();
    var result = new TaskConfiguration();
    result.setName(globalConfiguration.getName());
    result.setType(globalConfiguration.getType());
    if (taskConfiguration.getPhases().isEmpty()) {
      result.setPhases(globalConfiguration.getPhases());
    } else {
      result.setPhases(taskConfiguration.getPhases());
    }

    globalConfiguration.getOptions().forEach(result::addOption);
    taskConfiguration.getOptions().forEach(result::addOption);

    return result;
  }
}
