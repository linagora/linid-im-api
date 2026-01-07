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

package io.github.linagora.linid.im.controller;

import io.github.linagora.linid.im.corelib.exception.ApiException;
import io.github.linagora.linid.im.corelib.i18n.I18nMessage;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntityMapper;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntityService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST controller providing generic CRUD operations for dynamic entities.
 *
 * <p>
 * The controller handles HTTP requests for creating, retrieving, updating, patching, and deleting entities dynamically
 * based on the entity name provided in the URL path.
 *
 * <p>
 * It delegates business logic to the {@link DynamicEntityService}.
 */
@RestController
@RequestMapping("/api/{entity}")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GenericController {

  private final DynamicEntityService service;

  private final DynamicEntityMapper mapper;

  /**
   * Determines the HTTP status code for a paged response. Returns 206 (Partial Content) if multiple pages exist,
   * otherwise 200 (OK).
   *
   * @param resources the page of resources to check
   * @param <T> the type of resource contained in the page
   * @return the HTTP status code to use for the response
   */
  public <T> int getStatus(final Page<T> resources) {
    if (resources.getTotalPages() > 1) {
      return HttpStatus.PARTIAL_CONTENT.value();
    }

    return HttpStatus.OK.value();
  }

  /**
   * Creates a new entity instance.
   *
   * @param entity the name of the entity type to create
   * @param body a map of attribute names and values for the new entity
   * @return a ResponseEntity containing the created entity and HTTP 201 status
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createEntity(@PathVariable String entity,
                                        @RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
    var dynamicEntity = service.handleCreate(request, entity, body);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(mapper.apply(dynamicEntity));
  }

  /**
   * Retrieves a paginated list of entities filtered by optional criteria.
   *
   * @param entity the name of the entity type to retrieve
   * @param filters a map of filters to apply to the query
   * @param pageable pagination information including page number and size
   * @return a ResponseEntity containing the page of entities and HTTP status 200 or 206
   */
  @GetMapping()
  public ResponseEntity<Page<Map<String, Object>>> getEntities(@PathVariable String entity,
                                       @RequestParam MultiValueMap<String, String> filters,
                                       Pageable pageable,
                                       HttpServletRequest request) {
    Page<Map<String, Object>> resources = service
        .handleFindAll(request, entity, filters, pageable)
        .map(mapper);

    return ResponseEntity.status(this.getStatus(resources))
        .body(resources);
  }

  /**
   * Retrieves a single entity by its ID.
   *
   * @param entity the name of the entity type to retrieve
   * @param id the ID of the entity to retrieve
   * @return a ResponseEntity containing the entity and HTTP status 200
   */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getEntityById(@PathVariable String entity, @PathVariable String id,
                                         HttpServletRequest request) {
    var dynamicEntity = service.handleFindById(request, entity, id);
    return ResponseEntity.ok(mapper.apply(dynamicEntity));
  }

  /**
   * Updates an existing entity by replacing it completely.
   *
   * @param entity the name of the entity type to update
   * @param id the ID of the entity to update
   * @param body a map of attribute names and values for the updated entity
   * @return a ResponseEntity containing the updated entity and HTTP status 200
   */
  @PutMapping("/{id}")
  public ResponseEntity<Map<String, Object>> putEntity(@PathVariable String entity, @PathVariable String id,
                                     @RequestBody Map<String, Object> body,
                                     HttpServletRequest request) {
    var dynamicEntity = service.handleUpdate(request, entity, id, body);
    return ResponseEntity.ok(mapper.apply(dynamicEntity));
  }

  /**
   * Partially updates an existing entity.
   *
   * @param entity the name of the entity type to patch
   * @param id the ID of the entity to patch
   * @param body a map of attribute names and values to patch in the entity
   * @return a ResponseEntity containing the patched entity and HTTP status 200
   */
  @PatchMapping("/{id}")
  public ResponseEntity<Map<String, Object>> patchEntity(@PathVariable String entity,
                                       @PathVariable String id,
                                       @RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
    var dynamicEntity = service.handlePatch(request, entity, id, body);
    return ResponseEntity.ok(mapper.apply(dynamicEntity));
  }

  /**
   * Deletes an entity by its ID.
   *
   * @param entity the name of the entity type to delete
   * @param id the ID of the entity to delete
   * @return a ResponseEntity with HTTP status 204 (No Content)
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEntity(@PathVariable String entity, @PathVariable String id,
                                        HttpServletRequest request) {
    if (!service.handleDelete(request, entity, id)) {
      throw new ApiException(404, I18nMessage.of(
          "error.router.unknown.route",
          Map.of("route", request.getRequestURI())
      ));
    }

    return ResponseEntity.noContent().build();
  }

  /**
   * Validates a single attribute value for a dynamic entity.
   *
   * <p>
   * This endpoint performs validation only (no side-effects). When validation succeeds, HTTP 204 (No Content) is returned.
   *
   * @param entityRoute entity route (as used in configuration)
   * @param attributeName attribute name to validate
   * @param value request body value to validate; may be omitted, in which case the value is treated as {@code null} for
   *     validation purposes
   * @param request HTTP request
   * @return 204 No Content on success; 404 if entity or attribute not found; 400 if validation fails
   */
  @PostMapping("/validate/{attributeName}")
  public ResponseEntity<Void> validateAttribute(
      @PathVariable("entity") String entityRoute,
      @PathVariable("attributeName") String attributeName,
      @RequestBody(required = false) Object value,
      HttpServletRequest request) {
    service.validateAttribute(entityRoute, attributeName, value);
    return ResponseEntity.noContent().build();
  }

}
