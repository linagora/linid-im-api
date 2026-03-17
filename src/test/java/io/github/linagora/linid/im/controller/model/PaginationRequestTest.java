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

package io.github.linagora.linid.im.controller.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PaginationRequestTest {

  @Test
  void testToPageableWithoutSort() {
    PaginationRequest request = new PaginationRequest(1, 10, null, null);
    Pageable pageable = request.toPageable();
    assertEquals(PageRequest.of(1, 10), pageable);
  }

  @Test
  void testToPageableWithAscendingSort() {
    PaginationRequest request = new PaginationRequest(0, 5, "name", "asc");
    Pageable pageable = request.toPageable();
    assertEquals(PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "name")), pageable);
  }


  @Test
  void testToPageableWithDescendingSort() {
    PaginationRequest request = new PaginationRequest(2, 15, "createdAt", "desc");
    Pageable pageable = request.toPageable();
    assertEquals(PageRequest.of(2, 15, Sort.by(Sort.Direction.DESC, "createdAt")), pageable);
  }

  @Test
  void testToPageableWithDescendingSortCaseInsensitive() {
    PaginationRequest request = new PaginationRequest(2, 15, "updatedAt", "DESC");
    Pageable pageable = request.toPageable();
    assertEquals(PageRequest.of(2, 15, Sort.by(Sort.Direction.DESC, "updatedAt")), pageable);
  }
}
