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

package io.github.linagora.linid.im.controller;

import io.github.linagora.linid.im.corelib.exception.ApiException;
import io.github.linagora.linid.im.corelib.i18n.I18nMessage;
import io.github.linagora.linid.im.corelib.plugin.authorization.AuthorizationFactory;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.task.TaskExecutionContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes metadata related to the plugin configuration system.
 *
 * <p>This controller allows clients to retrieve metadata about configured routes and entities
 * defined in the plugin configuration file (YAML or JSON). The metadata includes descriptions of routes, entities, and individual
 * entity definitions.
 */
@RestController
@RequestMapping("/metadata")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MetadataController {
  private final AuthorizationFactory authorizationFactory;

  /**
   * Service responsible for accessing the plugin configuration metadata.
   */
  private final PluginConfigurationService pluginConfigurationService;

  /**
   * Returns metadata describing all available routes.
   *
   * <p>This endpoint is typically used by clients (such as front-end UIs or tools) to retrieve
   * information about the available route types and their configuration.
   *
   * @return an HTTP 200 response with a list of route metadata objects
   */
  @GetMapping("/routes")
  public ResponseEntity<?> getRouteDescriptions(HttpServletRequest request) {
    authorizationFactory.getAuthorizationPlugin().validateToken(request, new TaskExecutionContext());
    return ResponseEntity.ok(pluginConfigurationService.getRouteDescriptions());
  }

  /**
   * Returns metadata describing all configured entities.
   *
   * <p>The returned metadata includes the names, attributes, validations, and associated
   * configuration of all entities defined in the plugin configuration.
   *
   * @return an HTTP 200 response with a list of entity metadata objects
   */
  @GetMapping("/entities")
  public ResponseEntity<?> getEntityDescriptions(HttpServletRequest request) {
    authorizationFactory.getAuthorizationPlugin().validateToken(request, new TaskExecutionContext());
    return ResponseEntity.ok(pluginConfigurationService.getEntityDescriptions());
  }

  /**
   * Returns metadata for a specific entity by name.
   *
   * <p>This allows a client to introspect the structure and configuration of a specific entity,
   * including its attributes, validation rules, and lifecycle behavior.
   *
   * @param entity the name of the entity to retrieve
   * @return an HTTP 200 response with the metadata of the requested entity
   */
  @GetMapping("/entities/{entity}")
  public ResponseEntity<?> getEntityDescription(@PathVariable String entity, HttpServletRequest request) {
    authorizationFactory.getAuthorizationPlugin().validateToken(request, new TaskExecutionContext());
    return ResponseEntity.ok(
        pluginConfigurationService.getEntityDescription(entity)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND.value(),
                I18nMessage.of("error.entity.unknown", Map.of("entity", entity))
            ))
    );
  }
}
