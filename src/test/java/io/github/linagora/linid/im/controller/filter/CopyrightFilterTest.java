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

package io.github.linagora.linid.im.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("Test class: CopyrightFilter")
class CopyrightFilterTest {
  private CopyrightFilter filter;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain chain;

  @BeforeEach
  void setUp() {
    filter = new CopyrightFilter();
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    chain = Mockito.mock(FilterChain.class);

    // Valeurs par défaut utilisées dans tous les tests
    ReflectionTestUtils.setField(filter, "copyrightDefault", "© Linagora");
    ReflectionTestUtils.setField(filter, "copyrightCustom", "© Custom Corp");
  }

  @Test
  void shouldSetCustomHeaderWhenModeIsCustom() throws ServletException, IOException {
    ReflectionTestUtils.setField(filter, "copyrightMode", "custom");

    filter.doFilterInternal(request, response, chain);

    Mockito.verify(response).setHeader("X-Copyright", "© Custom Corp");
    Mockito.verify(chain).doFilter(request, response);
  }

  @Test
  void shouldSetDefaultHeaderWhenModeIsDefaultOrUnknown() throws ServletException, IOException {
    ReflectionTestUtils.setField(filter, "copyrightMode", "default");

    filter.doFilterInternal(request, response, chain);

    Mockito.verify(response).setHeader("X-Copyright", "© Linagora");
    Mockito.verify(chain).doFilter(request, response);
  }

  @Test
  void shouldNotSetHeaderWhenModeIsNone() throws ServletException, IOException {
    ReflectionTestUtils.setField(filter, "copyrightMode", "none");

    filter.doFilterInternal(request, response, chain);

    Mockito.verify(response, Mockito.never()).setHeader(Mockito.eq("X-Copyright"), Mockito.anyString());
    Mockito.verify(chain).doFilter(request, response);
  }

  @Test
  void shouldNotSetHeaderWhenModeIsNONECaseInsensitive() throws ServletException, IOException {
    ReflectionTestUtils.setField(filter, "copyrightMode", "NoNe");

    filter.doFilterInternal(request, response, chain);

    Mockito.verify(response, Mockito.never()).setHeader(Mockito.eq("X-Copyright"), Mockito.anyString());
    Mockito.verify(chain).doFilter(request, response);
  }
}
