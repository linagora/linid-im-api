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

import io.github.linagora.linid.im.corelib.exception.ApiException;
import io.github.linagora.linid.im.corelib.i18n.I18nMessage;
import io.github.linagora.linid.im.corelib.plugin.authorization.AuthorizationFactory;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.config.dto.ProviderConfiguration;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntityService;
import io.github.linagora.linid.im.corelib.plugin.provider.ProviderFactory;
import io.github.linagora.linid.im.corelib.plugin.provider.ProviderPlugin;
import io.github.linagora.linid.im.corelib.plugin.task.TaskEngine;
import io.github.linagora.linid.im.corelib.plugin.task.TaskExecutionContext;
import io.github.linagora.linid.im.corelib.plugin.validation.ValidationEngine;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * Service implementation for managing dynamic entities using a plugin-based architecture.
 *
 * <p>
 * This class handles the core lifecycle operations (create, update, patch, delete, and retrieval) of dynamic entities defined at
 * runtime, applying configured validation and task plugins at each phase.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DynamicEntityServiceImpl implements DynamicEntityService {

  /**
   * Factory to obtain {@link ProviderPlugin} instances by provider name. Used to delegate entity operations to the appropriate
   * provider implementation.
   */
  private final ProviderFactory providerFactory;
  private final AuthorizationFactory authorizationFactory;

  /**
   * Service to load plugin and entity configurations. Provides access to entity definitions and provider-specific
   * configurations.
   */
  private final PluginConfigurationService configurationService;

  /**
   * Engine responsible for validating dynamic entities according to configured rules. Validation occurs in different phases of
   * the entity lifecycle.
   */
  private final ValidationEngine validationEngine;

  /**
   * Engine to execute lifecycle tasks before and after validation and CRUD operations. Supports running custom logic at various
   * points in the entity handling process.
   */
  private final TaskEngine taskEngine;

  private static final String ENTITY_KEYWORD = "entity";

  /**
   * Updates the given {@link DynamicEntity} with its associated configuration based on its name.
   *
   * <p>If no configuration is found for the given name, an {@link ApiException} is thrown.
   *
   * @param entity the {@link DynamicEntity} to update
   * @param entityName the name of the entity whose configuration should be loaded
   * @throws ApiException if the configuration for the given entity name is not found
   */
  public void updateEntityConfiguration(DynamicEntity entity, String entityName) {
    var configuration = configurationService.getEntityConfiguration(entityName)
        .orElseThrow(() -> new ApiException(404, I18nMessage.of("error.entity.unknown", Map.of(ENTITY_KEYWORD,
            entityName))));

    entity.setConfiguration(configuration);
  }

  @Override
  public DynamicEntity handleCreate(HttpServletRequest request, String entityName, Map<String, Object> body) {
    TaskExecutionContext context = new TaskExecutionContext();
    var entity = new DynamicEntity();
    entity.setAttributes(body);

    var authorizationPlugin = authorizationFactory.getAuthorizationPlugin();

    taskEngine.execute(entity, context, "beforeTokenValidationCreate");
    authorizationPlugin.validateToken(request, context);
    taskEngine.execute(entity, context, "afterTokenValidationCreate");

    taskEngine.execute(entity, context, "beforePermissionValidationCreate");
    authorizationPlugin.isAuthorized(request, entity, "CREATE", context);
    taskEngine.execute(entity, context, "afterPermissionValidationCreate");

    updateEntityConfiguration(entity, entityName);
    var provider = getProvider(entity);
    var configuration = getProviderConfiguration(entity);

    taskEngine.execute(entity, context, "beforeValidationCreate");
    validationEngine.validate(entity, "beforeCreate");
    taskEngine.execute(entity, context, "afterValidationCreate");

    taskEngine.execute(entity, context, "beforeCreate");
    entity = provider.create(context, configuration, entity);
    taskEngine.execute(entity, context, "afterCreate");

    return entity;
  }

  @Override
  public DynamicEntity handleUpdate(HttpServletRequest request, String entityName, String id, Map<String, Object> body) {
    TaskExecutionContext context = new TaskExecutionContext();
    var entity = new DynamicEntity();
    entity.setAttributes(body);

    var authorizationPlugin = authorizationFactory.getAuthorizationPlugin();

    taskEngine.execute(entity, context, "beforeTokenValidationUpdate");
    authorizationPlugin.validateToken(request, context);
    taskEngine.execute(entity, context, "afterTokenValidationUpdate");

    taskEngine.execute(entity, context, "beforePermissionValidationUpdate");
    authorizationPlugin.isAuthorized(request, entity, id, "UPDATE", context);
    taskEngine.execute(entity, context, "afterPermissionValidationUpdate");

    updateEntityConfiguration(entity, entityName);
    var provider = getProvider(entity);
    var configuration = getProviderConfiguration(entity);

    taskEngine.execute(entity, context, "beforeValidationUpdate");
    validationEngine.validate(entity, "beforeUpdate");
    taskEngine.execute(entity, context, "afterValidationUpdate");

    taskEngine.execute(entity, context, "beforeUpdate");
    entity = provider.update(context, configuration, id, entity);
    taskEngine.execute(entity, context, "afterUpdate");

    return entity;
  }

  @Override
  public DynamicEntity handlePatch(HttpServletRequest request, String entityName, String id, Map<String, Object> body) {
    TaskExecutionContext context = new TaskExecutionContext();
    var entity = new DynamicEntity();
    entity.setAttributes(body);

    var authorizationPlugin = authorizationFactory.getAuthorizationPlugin();

    taskEngine.execute(entity, context, "beforeTokenValidationPatch");
    authorizationPlugin.validateToken(request, context);
    taskEngine.execute(entity, context, "afterTokenValidationPatch");

    taskEngine.execute(entity, context, "beforePermissionValidationPatch");
    authorizationPlugin.isAuthorized(request, entity, id, "UPDATE", context);
    taskEngine.execute(entity, context, "afterPermissionValidationPatch");

    updateEntityConfiguration(entity, entityName);
    var provider = getProvider(entity);
    var configuration = getProviderConfiguration(entity);

    taskEngine.execute(entity, context, "beforeValidationPatch");
    validationEngine.validate(entity, "beforePatch");
    taskEngine.execute(entity, context, "afterValidationPatch");

    taskEngine.execute(entity, context, "beforePatch");
    entity = provider.patch(context, configuration, id, entity);
    taskEngine.execute(entity, context, "afterPatch");

    return entity;
  }

  @Override
  public boolean handleDelete(HttpServletRequest request, String entityName, String id) {
    TaskExecutionContext context = new TaskExecutionContext();
    var entity = new DynamicEntity();

    var authorizationPlugin = authorizationFactory.getAuthorizationPlugin();

    taskEngine.execute(entity, context, "beforeTokenValidationDelete");
    authorizationPlugin.validateToken(request, context);
    taskEngine.execute(entity, context, "afterTokenValidationDelete");

    taskEngine.execute(entity, context, "beforePermissionValidationDelete");
    authorizationPlugin.isAuthorized(request, entity, id, "DELETE", context);
    taskEngine.execute(entity, context, "afterPermissionValidationDelete");

    updateEntityConfiguration(entity, entityName);
    var provider = getProvider(entity);
    var configuration = getProviderConfiguration(entity);

    taskEngine.execute(entity, context, "beforeValidationDelete");
    validationEngine.validate(entity, "beforeDelete");
    taskEngine.execute(entity, context, "afterValidationDelete");

    taskEngine.execute(entity, context, "beforeDelete");
    boolean state = provider.delete(context, configuration, id, entity);
    taskEngine.execute(entity, context, "afterDelete");

    return state;
  }

  @Override
  public DynamicEntity handleFindById(HttpServletRequest request, String entityName, String id) {
    TaskExecutionContext context = new TaskExecutionContext();
    context.put("id", id);

    var entity = new DynamicEntity();

    var authorizationPlugin = authorizationFactory.getAuthorizationPlugin();

    taskEngine.execute(entity, context, "beforeTokenValidationFindById");
    authorizationPlugin.validateToken(request, context);
    taskEngine.execute(entity, context, "afterTokenValidationFindById");

    taskEngine.execute(entity, context, "beforePermissionValidationFindById");
    authorizationPlugin.isAuthorized(request, entity, id, "READ", context);
    taskEngine.execute(entity, context, "afterPermissionValidationFindById");

    updateEntityConfiguration(entity, entityName);
    var provider = getProvider(entity);
    var configuration = getProviderConfiguration(entity);

    taskEngine.execute(entity, context, "beforeValidationFindById");
    validationEngine.validate(entity, "beforeFindById");
    taskEngine.execute(entity, context, "afterValidationFindById");

    taskEngine.execute(entity, context, "beforeFindById");
    entity = provider.findById(context, configuration, id, entity);
    taskEngine.execute(entity, context, "afterFindById");

    return entity;
  }

  @Override
  public Page<DynamicEntity> handleFindAll(HttpServletRequest request, String entityName, MultiValueMap<String, String> filters,
                                           Pageable pageable) {
    TaskExecutionContext context = new TaskExecutionContext();
    context.put("filters", filters);
    context.put("pageable", pageable);

    var entity = new DynamicEntity();

    var authorizationPlugin = authorizationFactory.getAuthorizationPlugin();

    taskEngine.execute(entity, context, "beforeTokenValidationFindAll");
    authorizationPlugin.validateToken(request, context);
    taskEngine.execute(entity, context, "afterTokenValidationFindAll");

    taskEngine.execute(entity, context, "beforePermissionValidationFindAll");
    authorizationPlugin.isAuthorized(request, entity, filters, "READ", context);
    taskEngine.execute(entity, context, "afterPermissionValidationFindAll");

    updateEntityConfiguration(entity, entityName);
    var provider = getProvider(entity);
    var configuration = getProviderConfiguration(entity);

    taskEngine.execute(entity, context, "beforeValidationFindAll");
    validationEngine.validate(entity, "beforeFindAll");
    taskEngine.execute(entity, context, "afterValidationFindAll");

    taskEngine.execute(entity, context, "beforeFindAll");
    var entities = provider.findAll(context, configuration, filters, pageable, entity);
    taskEngine.execute(entity, context, "afterFindAll");

    return entities;
  }

  /**
   * Retrieves the corresponding provider plugin for the dynamic entity.
   *
   * @param entity the dynamic entity
   * @return the {@link ProviderPlugin} responsible for the entity
   * @throws ApiException if the provider plugin is unknown
   */
  public ProviderPlugin getProvider(DynamicEntity entity) {
    return providerFactory.getProviderByName(entity.getConfiguration().getProvider())
        .orElseThrow(() -> new ApiException(500, I18nMessage.of("error.provider.unknown",
            Map.of(
                ENTITY_KEYWORD, entity.getConfiguration().getName(),
                "provider", entity.getConfiguration().getProvider())
        )));
  }

  /**
   * Retrieves the provider-specific configuration for the given entity.
   *
   * @param entity the dynamic entity
   * @return the {@link ProviderConfiguration}
   * @throws ApiException if the provider configuration is not found
   */
  public ProviderConfiguration   getProviderConfiguration(DynamicEntity entity) {
    return configurationService.getProviderConfiguration(entity.getConfiguration().getProvider())
        .orElseThrow(() -> new ApiException(500, I18nMessage.of("error.provider.unknown",
            Map.of(
                ENTITY_KEYWORD, entity.getConfiguration().getName(),
                "provider", entity.getConfiguration().getProvider())
        )));
  }

  @Override
  public void validateAttribute(String entityName, String attributeName, Object value) {
    var entity = new DynamicEntity();
    updateEntityConfiguration(entity, entityName);

    validationEngine.validateAttribute(entity, attributeName, value);
  }
}
