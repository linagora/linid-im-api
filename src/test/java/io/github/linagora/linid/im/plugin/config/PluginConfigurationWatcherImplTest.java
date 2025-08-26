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

package io.github.linagora.linid.im.plugin.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.linagora.linid.im.corelib.i18n.I18nService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Test class: PluginConfigurationWatcherImpl")
class PluginConfigurationWatcherImplTest {

  @Test
  @DisplayName("Should trigger onChange when the config file is modified")
  void shouldTriggerOnChangeOnFileModification() throws IOException, InterruptedException {
    I18nService i18nService = Mockito.mock(I18nService.class);
    Mockito.when(i18nService.translate(Mockito.any())).thenReturn("Localized error message");

    PluginConfigurationWatcherImpl watcher = new PluginConfigurationWatcherImpl();

    Path tempDir = Files.createTempDirectory("watcher-test");
    Path configFile = tempDir.resolve("test-config.yaml");
    Files.writeString(configFile, "initial");

    CountDownLatch latch = new CountDownLatch(1);
    Runnable onChange = latch::countDown;

    watcher.watch(configFile, onChange);

    Thread.sleep(200);
    Files.writeString(configFile, "updated");

    boolean triggered = latch.await(2, TimeUnit.SECONDS);
    assertTrue(triggered);
  }
}
