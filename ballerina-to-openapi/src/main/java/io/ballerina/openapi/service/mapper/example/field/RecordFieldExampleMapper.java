/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.service.mapper.example.field;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.example.ExampleMapper;
import io.swagger.v3.oas.models.media.ObjectSchema;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.example.CommonUtils.extractOpenApiExampleValue;

/**
 * This {@link RecordFieldExampleMapper} class represents the example mapper for record field.
 *
 * @since 2.1.0
 */
public class RecordFieldExampleMapper implements ExampleMapper {

    RecordTypeSymbol recordTypeSymbol;
    ObjectSchema schema;
    SemanticModel semanticModel;
    List<OpenAPIMapperDiagnostic> diagnostics;

    public RecordFieldExampleMapper(RecordTypeSymbol recordTypeSymbol, ObjectSchema schema,
                                    SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        this.recordTypeSymbol = recordTypeSymbol;
        this.schema = schema;
        this.semanticModel = semanticModel;
        this.diagnostics = diagnostics;
    }

    @Override
    public void setExample() {
        Map<String, RecordFieldSymbol> recordFields = recordTypeSymbol.fieldDescriptors();
        if (Objects.isNull(recordFields) || recordFields.isEmpty()) {
            return;
        }

        recordFields.forEach(this::setPropertyExample);
    }

    private void setPropertyExample(String fieldName, RecordFieldSymbol fieldSymbol) {
        Optional<Object> example = extractExample(fieldSymbol);
        if (example.isEmpty()) {
            return;
        }
        schema.getProperties().get(fieldName).setExample(example.get());
    }

    private Optional<Object> extractExample(RecordFieldSymbol fieldSymbol) {
        try {
            List<AnnotationAttachmentSymbol> annotations = fieldSymbol.annotAttachments();
            if (Objects.isNull(annotations)) {
                return Optional.empty();
            }
            return extractOpenApiExampleValue(annotations, semanticModel);
        } catch (JsonProcessingException exp) {
            // Add a diagnostic
            return Optional.empty();
        }
    }
}
