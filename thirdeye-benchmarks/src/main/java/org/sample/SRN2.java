/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.sample;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SRN2(List<Resource> _resourceHierarchy) {

    record Resource (String _type, String _name) {}
    public static final String SRN2_PREFIX = "srn2";
    public static final String SRN2_DELIMITER = ":";
    public static final String SRN2_RESOURCE_TYPE_NAME_DELIMITER = "#";
    private static final String ZONE = "zone";
    private static final String DEFAULT_NAMESPACE = "default";

    public static SRN2 fromString(String srn2String) {
        String[] parts = srn2String.split(SRN2_DELIMITER);
        if (!parts[0].equals(SRN2_PREFIX)) {
            throw new IllegalArgumentException("Not a startree identifier");
        }
        List<Resource> resourceHierarchy = new ArrayList<>();

        for (int i = 1; i < parts.length; i++) {
            String[] resourceTypeAndName = parts[i].split(SRN2_RESOURCE_TYPE_NAME_DELIMITER);
            if (resourceTypeAndName.length != 2) {
                throw new IllegalArgumentException("Resource type and name must be separated by " + SRN2_RESOURCE_TYPE_NAME_DELIMITER);
            }
            String resourceType = resourceTypeAndName[0];
            String resourceName = resourceTypeAndName[1];

            resourceHierarchy.add(new Resource(resourceType, resourceName));
        }
        return new SRN2(resourceHierarchy);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SRN2_PREFIX);
        for (Resource resource : _resourceHierarchy) {
            sb.append(SRN2_DELIMITER);
            sb.append(resource._type);
            sb.append(SRN2_RESOURCE_TYPE_NAME_DELIMITER);
            sb.append(resource._name);
        }
        return sb.toString();
    }
}
