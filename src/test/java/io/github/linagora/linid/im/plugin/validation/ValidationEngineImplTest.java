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

package io.github.linagora.linid.im.plugin.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.linagora.linid.im.corelib.exception.ApiException;
import io.github.linagora.linid.im.corelib.i18n.I18nMessage;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.config.dto.AttributeConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.EntityConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.ValidationConfiguration;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import io.github.linagora.linid.im.corelib.plugin.validation.ValidationPlugin;
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
@DisplayName("Test class: ValidationEngineImpl")
class ValidationEngineImplTest {

  @Mock
  private PluginRegistry<ValidationPlugin, String> validationRegistry;

  @Mock
  private PluginConfigurationService configurationService;

  @InjectMocks
  private ValidationEngineImpl validationEngine;

  @Test
  @DisplayName("test mergeConfigurationWithGlobal: should return provided configuration without global")
  void testMergeWithoutGlobal() {
    var configuration = new ValidationConfiguration();
    configuration.setName("custom");
    configuration.setType("myType");

    Mockito.when(configurationService.getValidationConfiguration("custom")).thenReturn(Optional.empty());

    var result = validationEngine.mergeConfigurationWithGlobal(configuration);

    assertEquals(configuration, result);
  }

  @Test
  @DisplayName("test mergeConfigurationWithGlobal: should return configuration with global data")
  void testMergeWithGlobal() {
    var global = new ValidationConfiguration();
    global.setName("myValidation");
    global.setType("global-type");
    global.addOption("a", "1");
    global.setPhases(List.of("global1", "global2"));

    var local = new ValidationConfiguration();
    local.setName("myValidation");
    local.addOption("b", "2");

    Mockito.when(configurationService.getValidationConfiguration("myValidation")).thenReturn(Optional.of(global));

    var merged = validationEngine.mergeConfigurationWithGlobal(local);

    assertEquals("global-type", merged.getType());
    assertEquals(List.of("global1", "global2"), merged.getPhases());
    assertEquals("1", merged.getOptions().get("a"));
    assertEquals("2", merged.getOptions().get("b"));
  }

  @Test
  @DisplayName("test mergeConfigurationWithGlobal: should keep local phases if defined")
  void testMergeWithGlobalAndLocalPhases() {
    var global = new ValidationConfiguration();
    global.setName("myValidation");
    global.setType("global-type");
    global.addOption("a", "1");
    global.setPhases(List.of("global1", "global2"));

    var local = new ValidationConfiguration();
    local.setName("myValidation");
    local.addOption("b", "2");
    local.setPhases(List.of("localPhase"));

    Mockito.when(configurationService.getValidationConfiguration("myValidation")).thenReturn(Optional.of(global));

    var merged = validationEngine.mergeConfigurationWithGlobal(local);

    assertEquals("global-type", merged.getType());
    assertEquals(List.of("localPhase"), merged.getPhases());
    assertEquals("1", merged.getOptions().get("a"));
    assertEquals("2", merged.getOptions().get("b"));
  }

  @Test
  @DisplayName("test getValidationPlugin: should throw exception with invalid plugin")
  void testGetPluginWithInvalidPlugin() {
    ValidationConfiguration config = new ValidationConfiguration();
    config.setName("nonexistent");
    config.setType("missing-type");

    Mockito.when(validationRegistry.getPlugins()).thenReturn(List.of());

    ApiException ex = assertThrows(ApiException.class, () -> validationEngine.getValidationPlugin(config));
    assertEquals("error.plugin.unknown", ex.getError().key());
    assertEquals(Map.of("type", "missing-type"), ex.getError().context());
    assertEquals(400, ex.getStatusCode());
  }

  @Test
  @DisplayName("test getValidationPlugin: should return plugin with valid plugin")
  void testGetPluginWithValidPlugin() {
    ValidationConfiguration config = new ValidationConfiguration();
    config.setName("myValidation");
    config.setType("type1");

    var plugin = new DummyPlugin();
    Mockito.when(validationRegistry.getPlugins()).thenReturn(List.of(plugin));

    var result = validationEngine.getValidationPlugin(config);
    assertNotNull(result);
    assertEquals(plugin, result);
  }

  @Test
  @DisplayName("test validate: should not throw exception without attributes error")
  void testValidateShouldNotThrow() {
    var plugin = Mockito.spy(new DummyPlugin());

    var config = new ValidationConfiguration();
    config.setName("val1");
    config.setType("type1");
    config.setPhases(List.of("create"));

    var attrConfig = new AttributeConfiguration();
    attrConfig.setName("attr1");
    attrConfig.setValidations(List.of(config));

    var entityConfig = new EntityConfiguration();
    entityConfig.setName("MyEntity");
    entityConfig.setAttributes(List.of(attrConfig));

    var entity = Mockito.mock(DynamicEntity.class);
    Mockito.when(entity.getConfiguration()).thenReturn(entityConfig);
    Mockito.when(entity.getAttributes()).thenReturn(Map.of("attr1", "ok"));

    Mockito.when(configurationService.getValidationConfiguration("val1")).thenReturn(Optional.empty());
    Mockito.when(validationRegistry.getPlugins()).thenReturn(List.of(plugin));

    assertDoesNotThrow(() -> validationEngine.validate(entity, "create"));
  }

  @Test
  @DisplayName("test validate: should throw exception with attributes error")
  void testValidateShouldThrow() {
    var plugin = Mockito.spy(new DummyPlugin() {
      @Override
      public Optional<I18nMessage> validate(ValidationConfiguration configuration, Object value) {
        return Optional.of(I18nMessage.of("error.test", Map.of("reason", "invalid")));
      }
    });

    var config = new ValidationConfiguration();
    config.setName("val1");
    config.setType("type1");
    config.setPhases(List.of("create"));

    var attrConfig = new AttributeConfiguration();
    attrConfig.setName("attr1");
    attrConfig.setValidations(List.of(config));

    var entityConfig = new EntityConfiguration();
    entityConfig.setName("MyEntity");
    entityConfig.setAttributes(List.of(attrConfig));

    var entity = Mockito.mock(DynamicEntity.class);
    Mockito.when(entity.getConfiguration()).thenReturn(entityConfig);
    Mockito.when(entity.getAttributes()).thenReturn(Map.of("attr1", "bad value"));

    Mockito.when(configurationService.getValidationConfiguration("val1")).thenReturn(Optional.empty());
    Mockito.when(validationRegistry.getPlugins()).thenReturn(List.of(plugin));

    var ex = assertThrows(ApiException.class, () -> validationEngine.validate(entity, "create"));
    assertEquals("error.entity.attributes", ex.getError().key());
    assertEquals("MyEntity", ex.getError().context().get("entity"));

    var errors = (List<?>) ex.getDetails().get("errors");
    assertFalse(errors.isEmpty());
  }


  public static class DummyPlugin implements ValidationPlugin {
    @Override
    public boolean supports(@NonNull String name) {
      return "type1".equals(name) || "myValidation".equals(name);
    }

    @Override
    public Optional<I18nMessage> validate(ValidationConfiguration configuration, Object value) {
      return Optional.empty();
    }
  }
}
