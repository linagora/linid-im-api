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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntityMapper;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntityService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test class: GenericController")
class GenericControllerTest {

  @Mock
  private DynamicEntityService service;

  @Mock
  private DynamicEntityMapper mapper;

  @InjectMocks
  private GenericController controller;

  @Test
  @DisplayName("test getStatus: should return valid status")
  void testGetStatus() {
    assertEquals(200, controller.getStatus(Page.empty()));
    assertEquals(200, controller.getStatus(new PageImpl<>(List.of(), PageRequest.of(0, 1), 1)));
    assertEquals(206, controller.getStatus(new PageImpl<>(List.of(), PageRequest.of(0, 1), 2)));
  }

  @Test
  @DisplayName("test createEntity: should return CREATED status and call service")
  void testCreateEntity() {
    var request = Mockito.mock(HttpServletRequest.class);
    Map<String, Object> requestBody = Map.of("field", "value");
    DynamicEntity expected = new DynamicEntity();
    Mockito.when(service.handleCreate(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(expected);
    Mockito.when(mapper.apply(Mockito.any())).thenReturn(expected.getAttributes());

    ResponseEntity<?> response = controller.createEntity("testEntity", requestBody, request);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(expected.getAttributes(), response.getBody());
  }

  @Test
  @DisplayName("test getEntities: should return OK status and call service")
  void testGetEntities() {
    var request = Mockito.mock(HttpServletRequest.class);
    Page<DynamicEntity> expected = Page.empty();
    Mockito.when(service.handleFindAll(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(expected);

    ResponseEntity<?> response = controller.getEntities(
        "testEntity",
        MultiValueMap.fromMultiValue(Map.of()),
        Page.empty().getPageable(),
        request
    );

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expected, response.getBody());
  }

  @Test
  @DisplayName("test getEntityById: should return OK status and call service")
  void testGetEntityById() {
    var request = Mockito.mock(HttpServletRequest.class);
    var expected = new DynamicEntity();
    Mockito.when(service.handleFindById(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(expected);
    Mockito.when(mapper.apply(Mockito.any())).thenReturn(expected.getAttributes());

    ResponseEntity<?> response = controller.getEntityById(
        "testEntity",
        "id",
        request
    );

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expected.getAttributes(), response.getBody());
  }

  @Test
  @DisplayName("test putEntity: should return OK status and call service")
  void testPutEntity() {
    var request = Mockito.mock(HttpServletRequest.class);
    Map<String, Object> requestBody = Map.of("field", "value");
    var expected = new DynamicEntity();
    Mockito.when(service.handleUpdate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(expected);
    Mockito.when(mapper.apply(Mockito.any())).thenReturn(expected.getAttributes());

    ResponseEntity<?> response = controller.putEntity("testEntity", "id", requestBody, request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expected.getAttributes(), response.getBody());
  }

  @Test
  @DisplayName("test patchEntity: should return OK status and call service")
  void testPatchEntity() {
    var request = Mockito.mock(HttpServletRequest.class);
    Map<String, Object> requestBody = Map.of("field", "value");
    var expected = new DynamicEntity();
    Mockito.when(service.handlePatch(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(expected);
    Mockito.when(mapper.apply(Mockito.any())).thenReturn(expected.getAttributes());

    ResponseEntity<?> response = controller.patchEntity("testEntity", "id", requestBody, request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expected.getAttributes(), response.getBody());
  }

  @Test
  @DisplayName("test deleteEntity: should return NO_CONTENT status and call service")
  void testDeleteEntity() {
    var request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(service.handleDelete(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

    ResponseEntity<?> response = controller.deleteEntity("testEntity", "id", request);

    assertNotNull(response);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  @DisplayName("test validateAttribute: should return 204 when valid")
  void testValidateAttributeWhenValid() {
    var request = Mockito.mock(HttpServletRequest.class);
    doNothing().when(service).validateAttribute(eq("users"), eq("email"), eq("a@b.com"));

    ResponseEntity<Void> response = controller.validateAttribute(
        "users",
        "email",
        "a@b.com",
        request
    );

    assertNotNull(response);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    Mockito.verify(service).validateAttribute(eq("users"), eq("email"), eq("a@b.com"));
  }
}
