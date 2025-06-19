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

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.linagora.linid.dmapicore.i18n.I18nService;
import org.linagora.linid.dmapicore.plugin.authorization.AuthorizationFactory;
import org.linagora.linid.dmapicore.plugin.task.TaskExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for accessing internationalization (i18n) data.
 *
 * <p>
 * Provides endpoints to retrieve available languages and their associated translations.
 */
@RestController
@RequestMapping("/i18n")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class I18nController {
  private final AuthorizationFactory authorizationFactory;

  /**
   * Service providing access to internationalized messages.
   */
  private final I18nService i18nService;

  /**
   * Retrieves the list of available language codes (e.g., "en", "fr").
   *
   * @return a {@link ResponseEntity} containing the list of languages.
   */
  @GetMapping("/languages")
  public ResponseEntity<List<String>> getLanguages(HttpServletRequest request) {
    authorizationFactory.getAuthorizationPlugin().validateToken(request, new TaskExecutionContext());
    return ResponseEntity.ok(i18nService.getLanguages());
  }

  /**
   * Retrieves the translation key-value pairs for a given language.
   *
   * @param language the language code (e.g., "en", "fr")
   * @return a {@link ResponseEntity} containing the translation map for the specified language.
   */
  @GetMapping("/{lang}.json")
  public ResponseEntity<Map<String, String>> getTranslationFile(@PathVariable("lang") String language,
                                                                HttpServletRequest request) {
    authorizationFactory.getAuthorizationPlugin().validateToken(request, new TaskExecutionContext());
    return ResponseEntity.ok(i18nService.getTranslations(language));
  }
}
