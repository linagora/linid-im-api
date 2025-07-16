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

package io.github.linagora.linid.im.plugin.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.linagora.linid.im.corelib.plugin.config.dto.AttributeConfiguration;
import io.github.linagora.linid.im.corelib.plugin.config.dto.EntityConfiguration;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test class: DynamicEntityMapperImpl")
class DynamicEntityMapperImplTest {

  @Mock
  private ConversionService conversionService;

  @InjectMocks
  private DynamicEntityMapperImpl mapper;

  @Test
  @DisplayName("test resolveClass: should return valid type")
  void testResolveClass() {
    assertEquals(Boolean.class, mapper.resolveClass("Boolean"));
    assertEquals(Integer.class, mapper.resolveClass("integer"));
    assertEquals(Float.class, mapper.resolveClass("float"));
    assertEquals(Double.class, mapper.resolveClass("double"));
    assertEquals(Long.class, mapper.resolveClass("long"));
    assertEquals(java.sql.Timestamp.class, mapper.resolveClass("timestamp"));
    assertEquals(java.util.Date.class, mapper.resolveClass("date"));
    assertEquals(String.class, mapper.resolveClass("unknown"));
  }

  @Test
  @DisplayName("test convertValue: should return null on null value")
  void testConvertValueNull() {
    assertNull(mapper.convertValue(null, null, false));
  }

  @Test
  @DisplayName("test convertValue: should return null on empty value with nullIfEmpty option enabled")
  void testConvertValueEmpty() {
    assertNull(mapper.convertValue(null, "", true));
  }

  @Test
  @DisplayName("test convertValue: should return valid value")
  void testConvertValue() {
    mapper.convertValue("String", "", false);
    Mockito.verify(conversionService, Mockito.times(1))
        .convert(Mockito.eq(""), Mockito.eq(String.class));

    mapper.convertValue("String", "a", true);
    Mockito.verify(conversionService, Mockito.times(1))
        .convert(Mockito.eq("a"), Mockito.eq(String.class));
  }

  @Test
  @DisplayName("test apply: should map attributes")
  void testApply() {
    Mockito.when(conversionService.convert(Mockito.any(), Mockito.eq(String.class))).thenReturn("test");
    Mockito.when(conversionService.convert(Mockito.any(), Mockito.eq(Integer.class))).thenReturn(1);

    var entity = new DynamicEntity();
    var configuration = new EntityConfiguration();
    var attributes = new ArrayList<AttributeConfiguration>();
    var attribute1 = new AttributeConfiguration();
    var attribute2 = new AttributeConfiguration();
    var attribute3 = new AttributeConfiguration();

    attribute1.setName("string");
    attribute1.setType("string");
    attribute2.setName("integer");
    attribute2.setType("integer");
    attribute3.setName("double");
    attribute3.setType("double");
    attributes.add(attribute1);
    attributes.add(attribute2);
    attributes.add(attribute3);
    configuration.setAttributes(attributes);
    entity.setConfiguration(configuration);
    entity.setAttributes(Map.of("string", "test", "integer", "1"));

    var result = mapper.apply(entity);

    var expected = new HashMap<String, Object>();
    expected.put("string", "test");
    expected.put("integer", 1);
    expected.put("double", null);
    assertEquals(expected, result);
  }

}
