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

package org.linagora.linid.dmapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linagora.linid.dmapicore.exception.ApiException;
import org.linagora.linid.dmapicore.plugin.authorization.AuthorizationFactory;
import org.linagora.linid.dmapicore.plugin.authorization.AuthorizationPlugin;
import org.linagora.linid.dmapicore.plugin.config.PluginConfigurationService;
import org.linagora.linid.dmapicore.plugin.entity.EntityDescription;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test class: MetadataController")
class MetadataControllerTest {

  @Mock
  private AuthorizationFactory authorizationFactory;

  @Mock
  private PluginConfigurationService pluginConfigurationService;

  @InjectMocks
  private MetadataController metadataController;

  private AuthorizationPlugin authorizationPlugin;

  @BeforeEach
  void setUp() {
    authorizationPlugin = Mockito.mock(AuthorizationPlugin.class);
    Mockito.when(authorizationFactory.getAuthorizationPlugin()).thenReturn(authorizationPlugin);
  }

  @Test
  void shouldReturnRouteDescriptions() {
    var request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(pluginConfigurationService.getRouteDescriptions()).thenReturn(List.of());

    var response = metadataController.getRouteDescriptions(request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
    assertEquals(List.of(), response.getBody());
  }

  @Test
  void shouldReturnEntityDescriptions() {
    var request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(pluginConfigurationService.getEntityDescriptions()).thenReturn(List.of());

    var response = metadataController.getEntityDescriptions(request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
    assertEquals(List.of(), response.getBody());
  }

  @Test
  void shouldReturnEntityDescriptionByName() throws Exception {
    var request = Mockito.mock(HttpServletRequest.class);
    var entity = new EntityDescription("test", List.of());
    Mockito.when(pluginConfigurationService.getEntityDescription(Mockito.any())).thenReturn(Optional.of(entity));

    var response = metadataController.getEntityDescription("test", request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
    assertEquals(entity, response.getBody());
  }

  @Test
  void shouldReturn404WhenEntityNotFound() throws Exception {
    var request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(pluginConfigurationService.getEntityDescription(Mockito.any())).thenReturn(Optional.empty());

    ApiException exception = null;

    try {
      metadataController.getEntityDescription("test", request);
    } catch (ApiException e) {
      exception = e;
    }

    assertNotNull(exception);
    assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode());
  }
}
