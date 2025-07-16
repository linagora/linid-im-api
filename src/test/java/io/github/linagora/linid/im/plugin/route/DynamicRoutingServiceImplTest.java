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

package io.github.linagora.linid.im.plugin.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.linagora.linid.im.corelib.exception.ApiException;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.config.dto.EntityConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.RouteConfiguration;
import io.github.linagora.linid.im.corelib.plugin.route.AbstractRoutePlugin;
import io.github.linagora.linid.im.corelib.plugin.route.RouteDescription;
import io.github.linagora.linid.im.corelib.plugin.route.RoutePlugin;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test class: DynamicRoutingServiceImpl")
public class DynamicRoutingServiceImplTest {
  @Mock
  private PluginRegistry<RoutePlugin, String> routeRegistry;
  @Mock
  private PluginConfigurationService configurationService;

  @InjectMocks
  private DynamicRoutingServiceImpl service;

  @Test
  @DisplayName("test route: should throw error on unknown route")
  public void testRouteUnknown() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    Mockito.when(request.getRequestURI()).thenReturn("/unknown");
    Mockito.when(request.getMethod()).thenReturn("GET");

    Mockito.when(routeRegistry.getPlugins()).thenReturn(List.of(new SimpleRoutePlugin()));

    ApiException exception = null;

    try {
      service.route(request);
    } catch (ApiException e) {
      exception = e;
    }

    assertNotNull(exception);
    assertEquals("error.router.unknown.route", exception.getError().key());
    assertEquals(Map.of("route", "/unknown"), exception.getError().context());
    assertEquals(404, exception.getStatusCode());
  }

  @Test
  @DisplayName("test route: should execute route with configuration")
  public void testRouteWithConfiguration() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    RouteConfiguration configuration = new RouteConfiguration();
    configuration.setName("test");

    Mockito.when(request.getRequestURI()).thenReturn("/valid");
    Mockito.when(request.getMethod()).thenReturn("GET");

    Mockito.when(routeRegistry.getPlugins()).thenReturn(List.of(new SimpleRoutePlugin()));
    Mockito.when(configurationService.getRoutesConfiguration()).thenReturn(List.of(configuration));

    var response = service.route(request);

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertEquals("test", response.getBody());
  }

  @Test
  @DisplayName("test route: should execute route without configuration")
  public void testRouteWithoutConfiguration() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    RouteConfiguration configuration = new RouteConfiguration();
    configuration.setName("none");

    Mockito.when(request.getRequestURI()).thenReturn("/valid");
    Mockito.when(request.getMethod()).thenReturn("GET");

    Mockito.when(routeRegistry.getPlugins()).thenReturn(List.of(new SimpleRoutePlugin()));
    Mockito.when(configurationService.getRoutesConfiguration()).thenReturn(List.of(configuration));

    var response = service.route(request);

    assertNotNull(response);
    assertEquals(200, response.getStatusCode().value());
    assertEquals("empty", response.getBody());
  }

  public class SimpleRoutePlugin extends AbstractRoutePlugin {
    @Override
    public List<RouteDescription> getRoutes(List<EntityConfiguration> entities) {
      return List.of();
    }

    @Override
    public boolean match(String url, String method) {
      return "/valid".equals(url);
    }

    @Override
    public ResponseEntity<?> execute(HttpServletRequest request) {
      String name = "empty";
      if (getConfiguration() != null) {
        name = getConfiguration().getName();
      }
      return ResponseEntity.ok(name);
    }

    @Override
    public boolean supports(@NonNull String type) {
      return "test".equals(type);
    }
  }
}
