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

package io.github.linagora.linid.im.plugin.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.linagora.linid.im.corelib.plugin.authentication.AuthenticationPlugin;
import io.github.linagora.linid.im.corelib.plugin.authentication.DenyAllAuthenticationPlugin;
import io.github.linagora.linid.im.corelib.plugin.config.PluginConfigurationService;
import io.github.linagora.linid.im.corelib.plugin.config.dto.AuthenticationConfiguration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.plugin.core.PluginRegistry;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test class: AuthenticationFactoryImpl")
class AuthenticationFactoryImplTest {
  @Mock
  private PluginRegistry<AuthenticationPlugin, String> pluginRegistry;
  @Mock
  private PluginConfigurationService pluginConfigurationService;

  @InjectMocks
  private AuthenticationFactoryImpl authenticationFactory;

  @Test
  @DisplayName("test getAuthenticationPlugin: should return deny-all without configuration")
  void testGetAuthenticationPluginWithoutConfiguration() {
    Mockito.when(pluginConfigurationService.getAuthenticationConfiguration()).thenReturn(Optional.empty());

    var plugin = authenticationFactory.getAuthenticationPlugin();
    assertInstanceOf(DenyAllAuthenticationPlugin.class, plugin);
  }

  @Test
  @DisplayName("test getAuthenticationPlugin: should return deny-all without plugin")
  void testGetAuthenticationPluginWithoutPlugin() {
    Mockito.when(pluginConfigurationService.getAuthenticationConfiguration())
        .thenReturn(Optional.of(new AuthenticationConfiguration()));
    Mockito.when(pluginRegistry.getPluginOrDefaultFor(Mockito.any(), Mockito.any(AuthenticationPlugin.class)))
        .thenReturn(new DenyAllAuthenticationPlugin());

    var plugin = authenticationFactory.getAuthenticationPlugin();
    assertInstanceOf(DenyAllAuthenticationPlugin.class, plugin);
  }

  @Test
  @DisplayName("test getAuthenticationConfiguration: should return null without configuration")
  void testGetAuthenticationConfigurationWithoutConfiguration() {
    Mockito.when(pluginConfigurationService.getAuthenticationConfiguration()).thenReturn(Optional.empty());

    var config = authenticationFactory.getAuthenticationConfiguration();
    assertNull(config);
  }

  @Test
  @DisplayName("test getAuthenticationConfiguration: should return configuration when present")
  void testGetAuthenticationConfigurationWithConfiguration() {
    var expected = new AuthenticationConfiguration();
    Mockito.when(pluginConfigurationService.getAuthenticationConfiguration()).thenReturn(Optional.of(expected));

    var config = authenticationFactory.getAuthenticationConfiguration();
    assertEquals(expected, config);
  }
}
