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

import io.github.linagora.linid.im.corelib.exception.ApiException;
import io.github.linagora.linid.im.corelib.i18n.I18nMessage;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.config.dto.ValidationConfiguration;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import io.github.linagora.linid.im.corelib.plugin.validation.ValidationEngine;
import io.github.linagora.linid.im.corelib.plugin.validation.ValidationPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link ValidationEngine} interface.
 *
 * <p>
 * This component is responsible for validating dynamic entities by applying all relevant {@link ValidationPlugin} instances
 * according to the validation configurations defined for each attribute and validation phase.
 *
 * <p>
 * Validation plugins are dynamically resolved using a {@link PluginRegistry}. Global plugin configurations are merged with
 * attribute-level configurations before validation.
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ValidationEngineImpl implements ValidationEngine {
  /**
   * Registry containing all available {@link ValidationPlugin} instances, used to resolve the appropriate plugin for each
   * validation configuration by type.
   */
  private final PluginRegistry<ValidationPlugin, String> validationRegistry;

  /**
   * Service used to retrieve global plugin configurations by name, allowing for merging of local and shared validation settings.
   */
  private final PluginConfigurationService configurationService;


  @Override
  public void validate(DynamicEntity dynamicEntity, String phase) {
    List<I18nMessage> errors = new ArrayList<>();

    dynamicEntity.getConfiguration()
        .getAttributes()
        .forEach(attributeConfiguration -> attributeConfiguration.getValidations()
            .stream()
            .filter(configuration -> configuration.getPhases().contains(phase))
            .map(this::mergeConfigurationWithGlobal)
            .forEach(configuration -> getValidationPlugin(configuration)
                .validate(configuration, dynamicEntity.getAttributes()
                    .getOrDefault(attributeConfiguration.getName(), null))
                .ifPresent(error -> {
                  error.context().put("entity", dynamicEntity.getConfiguration().getName());
                  error.context().put("attribute", attributeConfiguration.getName());

                  errors.add(error);
                })));

    if (!errors.isEmpty()) {
      throw new ApiException(
          400,
          I18nMessage.of("error.entity.attributes", Map.of("entity", dynamicEntity.getConfiguration().getName())),
          Map.of("errors", errors)
      );
    }
  }

  @Override
  public void validateAttribute(DynamicEntity dynamicEntity, String attributeName, Object value) {
    var attributeConfiguration = dynamicEntity.getConfiguration()
        .getAttributes()
        .stream()
        .filter(attribute -> attributeName.equals(attribute.getName()))
        .findFirst()
        .orElseThrow(() -> new ApiException(
            404,
            I18nMessage.of(
                "error.attribute.unknown",
                Map.of(
                    "entity", dynamicEntity.getConfiguration().getName(),
                    "attribute", attributeName))));

    List<I18nMessage> errors = new ArrayList<>();

    attributeConfiguration.getValidations()
        .stream()
        .map(this::mergeConfigurationWithGlobal)
        .forEach(configuration -> getValidationPlugin(configuration)
            .validate(configuration, value)
            .ifPresent(error -> {
              error.context().put("entity", dynamicEntity.getConfiguration().getName());
              error.context().put("attribute", attributeName);

              errors.add(error);
            }));

    if (!errors.isEmpty()) {
      throw new ApiException(
          400,
          I18nMessage.of(
              "error.entity.attributes",
              Map.of("entity", dynamicEntity.getConfiguration().getName())),
          Map.of("errors", errors));
    }
  }

  /**
   * Resolves the correct {@link ValidationPlugin} for a given validation configuration.
   *
   * @param configuration the validation configuration
   * @return the matching plugin
   * @throws ApiException if no plugin is found that supports the given type
   */
  public ValidationPlugin getValidationPlugin(ValidationConfiguration configuration) {
    return validationRegistry.getPlugins()
        .stream()
        .filter(validationPlugin -> validationPlugin.supports(configuration.getType()))
        .findFirst()
        .orElseThrow(() -> new ApiException(400, I18nMessage.of(
            "error.plugin.unknown",
            Map.of("type", configuration.getType())
        )));
  }

  /**
   * Merges a given attribute-level {@link ValidationConfiguration} with the global one (if defined) retrieved from
   * {@link PluginConfigurationService}.
   *
   * <p>
   * The global configuration serves as a default, which can be overridden or extended by the attribute-specific options and
   * phases.
   *
   * @param configuration the attribute-level validation configuration
   * @return the merged configuration
   */
  public ValidationConfiguration mergeConfigurationWithGlobal(ValidationConfiguration configuration) {
    var opt = configurationService.getValidationConfiguration(configuration.getName());

    if (opt.isEmpty()) {
      return configuration;
    }

    var globalConfiguration = opt.get();
    var result = new ValidationConfiguration();

    result.setName(globalConfiguration.getName());
    result.setType(globalConfiguration.getType());
    if (configuration.getPhases().isEmpty()) {
      result.setPhases(globalConfiguration.getPhases());
    } else {
      result.setPhases(configuration.getPhases());
    }

    globalConfiguration.getOptions().forEach(result::addOption);
    configuration.getOptions().forEach(result::addOption);

    return result;
  }
}
