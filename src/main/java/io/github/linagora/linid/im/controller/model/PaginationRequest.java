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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

/**
 * Represents a pagination request, typically built from query parameters.
 *
 * <p>This class holds page number, page size, optional sorting field, and sort direction.
 * It provides a utility method to convert itself into a Spring {@link Pageable}.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaginationRequest {

  /**
   * Zero-based page index. Must be greater than or equal to 0. Default value is 0.
   */
  @Min(value = 0, message = "Page index must be 0 or greater")
  private int page = 0;

  /**
   * Size of the page (number of items per page). Must be at least 1. Default value is 20.
   */
  @Min(value = 1, message = "Page size must be at least 1")
  private int size = 20;

  /**
   * Name of the property used for sorting. Optional: if null or empty, no sorting will be applied.
   */
  private String sort;

  /**
   * Sort direction for the {@link #sort} property. Allowed values are 'asc' or 'desc' (case-insensitive). Optional:
   * defaults to ascending if not specified.
   */
  @Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE,
      message = "Direction must be either 'asc' or 'desc'")
  private String direction;

  /**
   * Converts this pagination request into a Spring {@link Pageable} object.
   *
   * <p>If {@link #sort} is not set, returns a pageable with no sorting. If {@link #direction} is 'desc'
   * (case-insensitive), the sorting will be descending. Otherwise, sorting is ascending by default.
   *
   * @return a {@link Pageable} representing this pagination request
   */
  public Pageable toPageable() {
    if (!StringUtils.hasText(sort)) {
      return PageRequest.of(page, size);
    }

    Sort.Direction dir = Sort.Direction.ASC;

    if ("desc".equalsIgnoreCase(direction)) {
      dir = Sort.Direction.DESC;
    }

    return PageRequest.of(page, size, Sort.by(dir, sort));
  }
}
