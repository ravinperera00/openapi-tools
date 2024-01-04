// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.openapi.service.mapper.type;

import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class MapTypeMapper extends AbstractTypeMapper {

    public MapTypeMapper(TypeReferenceTypeSymbol typeSymbol, AdditionalData additionalData) {
        super(typeSymbol, additionalData);
    }

    @Override
    public Schema getReferenceSchema(OpenAPI openAPI) {
        MapTypeSymbol referredType = (MapTypeSymbol) typeSymbol.typeDescriptor();
        return getSchema(referredType, openAPI, additionalData).description(description);
    }

    public static Schema getSchema(MapTypeSymbol typeSymbol, OpenAPI openAPI, AdditionalData additionalData) {
        TypeSymbol memberType = typeSymbol.typeParam();
        if (additionalData.semanticModel().types().JSON.subtypeOf(memberType)) {
            return new ObjectSchema();
        }
        return new ObjectSchema().additionalProperties(TypeMapper.getTypeSchema(memberType,
                openAPI, additionalData));
    }
}
