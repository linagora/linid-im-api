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

package org.linagora.linid.dmapi.plugin.authorization;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linagora.linid.dmapicore.plugin.authorization.AuthorizationPlugin;
import org.linagora.linid.dmapicore.plugin.authorization.DenyAllAuthorizationPlugin;
import org.linagora.linid.dmapicore.plugin.config.PluginConfigurationService;
import org.linagora.linid.dmapicore.plugin.config.dto.AuthorizationConfiguration;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test class: AuthorizationFactoryImpl")
class AuthorizationFactoryImplTest {
  @Mock
  private PluginRegistry<AuthorizationPlugin, String> pluginRegistry;
  @Mock
  private PluginConfigurationService pluginConfigurationService;

  @InjectMocks
  private AuthorizationFactoryImpl authorizationFactory;

  @Test
  @DisplayName("test getAuthorizationPlugin: should return deny-all without configuration")
  void testGetAuthorizationPluginWithoutConfiguration() {
    Mockito.when(pluginConfigurationService.getAuthorizationConfiguration()).thenReturn(Optional.empty());

    var plugin = authorizationFactory.getAuthorizationPlugin();
    assertInstanceOf(DenyAllAuthorizationPlugin.class, plugin);
  }

  @Test
  @DisplayName("test getAuthorizationPlugin: should return deny-all without plugin")
  void testGetAuthorizationPluginWithoutPlugin() {
    Mockito.when(pluginConfigurationService.getAuthorizationConfiguration())
        .thenReturn(Optional.of(new AuthorizationConfiguration()));
    Mockito.when(pluginRegistry.getPluginOrDefaultFor(Mockito.any(), Mockito.any(AuthorizationPlugin.class)))
        .thenReturn(new DenyAllAuthorizationPlugin());

    var plugin = authorizationFactory.getAuthorizationPlugin();
    assertInstanceOf(DenyAllAuthorizationPlugin.class, plugin);
  }

}
