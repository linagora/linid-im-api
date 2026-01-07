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

package io.github.linagora.linid.im.plugin.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.linagora.linid.im.corelib.plugin.config.dto.ProviderConfiguration;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import io.github.linagora.linid.im.corelib.plugin.provider.ProviderPlugin;
import io.github.linagora.linid.im.corelib.plugin.task.TaskExecutionContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.MultiValueMap;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test class: ProviderFactoryImpl")
class ProviderFactoryImplTest {

  @Mock
  private PluginRegistry<ProviderPlugin, String> providerRegistry;

  @InjectMocks
  private ProviderFactoryImpl providerFactory;

  @Test
  @DisplayName("test ProviderFactoryImpl: should return wanted provider")
  void testProviderFactoryImpl() {
    var provider = new SimpleProviderPlugin();
    Mockito.when(providerRegistry.getPlugins()).thenReturn(List.of(provider));

    var result = providerFactory.getProviderByName("test");
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(provider, result.get());
  }

  @Test
  @DisplayName("test ProviderFactoryImpl: should return empty optional")
  void testProviderFactoryImplEmpty() {
    var provider = new SimpleProviderPlugin();
    Mockito.when(providerRegistry.getPlugins()).thenReturn(List.of(provider));

    var result = providerFactory.getProviderByName("bas");
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  public class SimpleProviderPlugin implements ProviderPlugin {

    @Override
    public boolean supports(@NonNull String type) {
      return type.equals("test");
    }

    @Override
    public DynamicEntity create(TaskExecutionContext context, ProviderConfiguration configuration, DynamicEntity dynamicEntity) {
      return null;
    }

    @Override
    public DynamicEntity update(TaskExecutionContext context, ProviderConfiguration configuration, String id,
                                DynamicEntity dynamicEntity) {
      return null;
    }

    @Override
    public DynamicEntity patch(TaskExecutionContext context, ProviderConfiguration configuration, String id,
                               DynamicEntity dynamicEntity) {
      return null;
    }

    @Override
    public boolean delete(TaskExecutionContext context, ProviderConfiguration configuration, String id,
                          DynamicEntity dynamicEntity) {
      return false;
    }

    @Override
    public DynamicEntity findById(TaskExecutionContext context, ProviderConfiguration configuration, String id,
                                  DynamicEntity dynamicEntity) {
      return null;
    }

    @Override
    public Page<DynamicEntity> findAll(TaskExecutionContext context, ProviderConfiguration configuration,
                                       MultiValueMap<String, String> filters, Pageable pageable, DynamicEntity dynamicEntity) {
      return null;
    }
  }
}
