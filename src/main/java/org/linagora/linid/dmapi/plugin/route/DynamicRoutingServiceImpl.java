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

package org.linagora.linid.dmapi.plugin.route;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.linagora.linid.dmapicore.exception.ApiException;
import org.linagora.linid.dmapicore.i18n.I18nMessage;
import org.linagora.linid.dmapicore.plugin.config.PluginConfigurationService;
import org.linagora.linid.dmapicore.plugin.route.DynamicRoutingService;
import org.linagora.linid.dmapicore.plugin.route.RoutePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;

/**
 * Service implementation responsible for dynamically routing HTTP requests to the appropriate {@link RoutePlugin}.
 *
 * <p>
 * This service inspects the current HTTP request, determines the matching route plugin based on the URI and method, retrieves its
 * associated configuration (if any), sets it on the plugin, and then delegates execution to the plugin.
 *
 * <p>
 * If no matching route plugin is found, a {@link ApiException} with a 404 status is thrown.
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DynamicRoutingServiceImpl implements DynamicRoutingService {
  /**
   * Registry of available {@link RoutePlugin} instances. Used to resolve the correct plugin based on the incoming request.
   */
  private final PluginRegistry<RoutePlugin, String> routeRegistry;
  /**
   * Service used to retrieve the configurations associated with each route plugin.
   */
  private final PluginConfigurationService configurationService;

  @Override
  public ResponseEntity<?> route(HttpServletRequest request) {
    var routePlugin = routeRegistry.getPlugins()
        .stream()
        .filter(route -> route.match(request.getRequestURI(), request.getMethod()))
        .findFirst()
        .orElseThrow(() -> new ApiException(404, I18nMessage.of(
            "error.router.unknown.route",
            Map.of("route", request.getRequestURI())
        )));

    var configuration = configurationService.getRoutesConfiguration()
        .stream()
        .filter(routeConfiguration -> routePlugin.supports(routeConfiguration.getName()))
        .findFirst()
        .orElse(null);

    routePlugin.setConfiguration(configuration);

    return routePlugin.execute(request);
  }
}
