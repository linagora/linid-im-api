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

import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntity;
import io.github.linagora.linid.im.corelib.plugin.entity.DynamicEntityMapper;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link DynamicEntityMapper} that converts a {@link DynamicEntity} into a {@link Map} representation
 * based on the entity's attribute configuration.
 *
 * <p>Each attribute is converted according to its configured type and the {@code nullIfEmpty} flag.
 * This ensures consistent mapping and type safety when serializing entities for API responses.
 */
@Component
public class DynamicEntityMapperImpl implements DynamicEntityMapper {

  /**
   * Spring ConversionService used to convert attribute values from String to their target types based on
   * configuration.
   */
  private final ConversionService conversionService;

  /**
   * Constructs a DynamicEntityMapperImpl with the given ConversionService.
   *
   * @param conversionService the Spring ConversionService for type conversion
   */
  @Autowired
  public DynamicEntityMapperImpl(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Override
  public Map<String, Object> apply(DynamicEntity dynamicEntity) {
    var mappedAttributes = new HashMap<String, Object>();

    dynamicEntity.getConfiguration()
        .getAttributes()
        .forEach(configuration -> {
          String key = configuration.getName();
          String type = configuration.getType();
          Object value = dynamicEntity.getAttributes().getOrDefault(key, null);

          mappedAttributes.put(key, convertValue(type, value, configuration.isNullIfEmpty()));
        });

    return mappedAttributes;
  }

  /**
   * Converts the given {@code value} to the specified {@code type}.
   *
   * <p>Supports primitive types such as boolean, integer, float, double, long, as well as
   * date/time types like {@link Timestamp} and {@link Date}. If {@code nullIfEmpty} is {@code true}, empty strings will
   * be converted to {@code null}.
   *
   * @param type the target type as a string (e.g. "boolean", "integer", "date")
   * @param value the original value to convert
   * @param nullIfEmpty whether to convert empty strings to {@code null}
   * @return the converted value, or the original value if type is not recognized
   */
  public Object convertValue(String type, Object value, boolean nullIfEmpty) {
    if (value == null) {
      return null;
    }
    String stringValue = value.toString();

    if (nullIfEmpty && StringUtils.isEmpty(stringValue)) {
      return null;
    }

    Class<?> targetClass = resolveClass(type);

    return conversionService.convert(value, targetClass);
  }

  /**
   * Resolves the target Java class corresponding to the given type name.
   *
   * <p>Supported types include primitive wrappers and common date/time classes. If the type is not recognized,
   * {@link String} class is returned by default.
   *
   * @param type the attribute type as a string (case-insensitive)
   * @return the corresponding Java Class object for the type
   */
  public Class<?> resolveClass(String type) {
    return switch (type.toLowerCase()) {
      case "boolean" -> Boolean.class;
      case "integer" -> Integer.class;
      case "float" -> Float.class;
      case "double" -> Double.class;
      case "long" -> Long.class;
      case "timestamp" -> java.sql.Timestamp.class;
      case "date" -> java.util.Date.class;
      default -> String.class;
    };
  }
}
