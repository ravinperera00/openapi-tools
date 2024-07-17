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
package io.ballerina.openapi.service.mapper.example.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.values.ConstantValue;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.example.ExampleMapper;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.example.CommonUtils.extractOpenApiExampleValue;
import static io.ballerina.openapi.service.mapper.example.CommonUtils.getJsonString;

/**
 * This {@link TypeExampleMapper} class represents the example mapper for type.
 *
 * @since 2.1.0
 */
public class TypeExampleMapper implements ExampleMapper {

    TypeDefinitionSymbol typeDefinitionSymbol;
    Schema typeSchema;
    SemanticModel semanticModel;
    List<OpenAPIMapperDiagnostic> diagnostics;

    public TypeExampleMapper(TypeDefinitionSymbol typeDefinitionSymbol, Schema typeSchema,
                             SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        this.typeDefinitionSymbol = typeDefinitionSymbol;
        this.typeSchema = typeSchema;
        this.semanticModel = semanticModel;
        this.diagnostics = diagnostics;
    }

    @Override
    public void setExample() {
        Optional<Object> exampleValue = extractExample();
        if (exampleValue.isEmpty()) {
            return;
        }

        typeSchema.setExample(exampleValue.get());
    }

    public Optional<Object> extractExample() {
        try {
            List<AnnotationAttachmentSymbol> annotations = typeDefinitionSymbol.annotAttachments();
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
