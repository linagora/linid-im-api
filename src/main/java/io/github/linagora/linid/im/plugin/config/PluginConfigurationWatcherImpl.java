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

package io.github.linagora.linid.im.plugin.config;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link PluginConfigurationWatcher} that monitors a configuration file for changes using the Java NIO
 * WatchService API.
 *
 * <p>
 * It starts a daemon thread that watches the directory containing the configuration file, triggering the provided callback when
 * the file is created or modified.
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PluginConfigurationWatcherImpl implements PluginConfigurationWatcher {

  /**
   * Watches the specified configuration file path for modifications or creation events. When the target file changes, the
   * provided {@code onChange} runnable is executed.
   *
   * @param configPath the path to the configuration file to watch
   * @param onChange the callback to execute when the file changes
   */
  @Override
  public void watch(Path configPath, Runnable onChange) {
    Thread thread = new Thread(() -> {
      try {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path dir = configPath.getParent();
        String fileName = configPath.getFileName().toString();

        dir.register(watchService, ENTRY_MODIFY, ENTRY_CREATE);

        while (true) {
          WatchKey key = watchService.take();
          for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent<Path> ev = (WatchEvent<Path>) event;
            if (ev.context().toString().equals(fileName)) {
              onChange.run();
            }
          }
          key.reset();
        }
      } catch (InterruptedException | IOException e) {
        log.error("Watcher thread failed for config path: {}", configPath, e);
        Thread.currentThread().interrupt();
      }
    });

    thread.setDaemon(true);
    thread.start();
  }
}