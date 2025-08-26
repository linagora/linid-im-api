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

package io.github.linagora.linid.im.plugin.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.linagora.linid.im.corelib.exception.ApiException;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.config.dto.EntityConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.TaskConfiguration;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import io.github.linagora.linid.im.corelib.plugin.task.TaskExecutionContext;
import io.github.linagora.linid.im.corelib.plugin.task.TaskPlugin;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test class: TaskEngineImpl")
class TaskEngineImplTest {

  @Mock
  private PluginRegistry<TaskPlugin, String> taskRegistry;

  @Mock
  private PluginConfigurationService configurationService;

  @InjectMocks
  private TaskEngineImpl taskEngine;

  @Test
  @DisplayName("test mergeConfigurationWithGlobal: should return provided configuration without global")
  void testMergeWithoutGlobal() {
    var configuration = new TaskConfiguration();
    configuration.setName("custom");
    configuration.setType("myType");

    Mockito.when(configurationService.getTaskConfiguration("custom")).thenReturn(Optional.empty());

    var result = taskEngine.mergeConfigurationWithGlobal(configuration);

    assertEquals(configuration, result);
  }

  @Test
  @DisplayName("test mergeConfigurationWithGlobal: should return configuration without override global information")
  void testMergeWithGlobal() {
    var global = new TaskConfiguration();
    global.setName("mytask");
    global.setType("global-type");
    global.addOption("a", "1");
    global.setPhases(List.of("phase1", "phase2"));

    var local = new TaskConfiguration();
    local.setName("mytask");
    local.addOption("b", "2");

    Mockito.when(configurationService.getTaskConfiguration("mytask")).thenReturn(Optional.of(global));

    var merged = taskEngine.mergeConfigurationWithGlobal(local);

    assertEquals("global-type", merged.getType());
    assertEquals(List.of("phase1", "phase2"), merged.getPhases());
    assertEquals("1", merged.getOptions().get("a"));
    assertEquals("2", merged.getOptions().get("b"));
  }

  @Test
  @DisplayName("test mergeConfigurationWithGlobal: should return configuration without empty phases")
  void testMergeWithGlobalWithoutEmpty() {
    var global = new TaskConfiguration();
    global.setName("mytask");
    global.setType("global-type");
    global.addOption("a", "1");
    global.setPhases(List.of("phase1", "phase2"));

    var local = new TaskConfiguration();
    local.setName("mytask");
    local.addOption("b", "2");
    local.setPhases(List.of("phase3"));

    Mockito.when(configurationService.getTaskConfiguration("mytask")).thenReturn(Optional.of(global));

    var merged = taskEngine.mergeConfigurationWithGlobal(local);

    assertEquals("global-type", merged.getType());
    assertEquals(List.of("phase3"), merged.getPhases());
    assertEquals("1", merged.getOptions().get("a"));
    assertEquals("2", merged.getOptions().get("b"));
  }

  @Test
  @DisplayName("test getPlugin: should throw exception with invalid plugin")
  void testGetPluginWithInvalidPlugin() {
    TaskConfiguration config = new TaskConfiguration();
    config.setName("nonexistent");
    config.setType("missing-type");

    Mockito.when(taskRegistry.getPlugins()).thenReturn(List.of());

    ApiException ex = assertThrows(ApiException.class, () -> taskEngine.getPlugin(config));
    assertEquals("error.plugin.unknown", ex.getError().key());
    assertEquals(Map.of("type", "missing-type"), ex.getError().context());
    assertEquals(400, ex.getStatusCode());
  }

  @Test
  @DisplayName("test getPlugin: should return plugin with valid plugin")
  void testGetPluginWithValidPlugin() {
    TaskConfiguration config = new TaskConfiguration();
    config.setName("mytask");

    var plugin = new DummyPlugin();
    Mockito.when(taskRegistry.getPlugins()).thenReturn(List.of(plugin));

    var result = taskEngine.getPlugin(config);
    assertNotNull(result);
    assertEquals(plugin, result);
  }

  @Test
  @DisplayName("test execute: should execute plugin")
  void testExecutePlugin() {
    var plugin = Mockito.spy(new DummyPlugin());

    var config = new TaskConfiguration();
    config.setName("mytask");
    config.setPhases(List.of("myphase"));

    var entityConfig = new EntityConfiguration();
    entityConfig.setTasks(List.of(config));

    var entity = Mockito.mock(DynamicEntity.class);
    Mockito.when(entity.getConfiguration()).thenReturn(entityConfig);

    Mockito.when(configurationService.getTaskConfiguration("mytask")).thenReturn(Optional.empty());
    Mockito.when(taskRegistry.getPlugins()).thenReturn(List.of(plugin));

    taskEngine.execute(entity, new TaskExecutionContext(), "myphase");

    Mockito.verify(plugin).execute(Mockito.any(), Mockito.eq(entity), Mockito.any());
  }

  public static class DummyPlugin implements TaskPlugin {
    @Override
    public boolean supports(@NonNull String name) {
      return "mytask".equals(name);
    }

    @Override
    public void execute(TaskConfiguration configuration, DynamicEntity dynamicEntity, TaskExecutionContext context) {
      // do nothing
    }
  }
}