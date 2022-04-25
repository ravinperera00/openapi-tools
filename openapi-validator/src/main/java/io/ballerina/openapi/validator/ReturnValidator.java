/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPTIONAL_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUALIFIED_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SIMPLE_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.UNION_TYPE_DESC;
import static io.ballerina.openapi.validator.Constants.HTTP;
import static io.ballerina.openapi.validator.Constants.HTTP_CODES;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.getMediaType;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;

/**
 * This class for validate the return and response types.
 */
public class ReturnValidator {
    private SyntaxNodeAnalysisContext context;
    private OpenAPI openAPI;
    private String method;
    private String path;

    private List<String> balStatusCodes = new ArrayList<>();

    public ReturnValidator(SyntaxNodeAnalysisContext context, OpenAPI openAPI, String method, String path) {
        this.context = context;
        this.openAPI = openAPI;
        this.method = method;
        this.path = path;
    }

    public void validateReturnBallerinaToOas(TypeDescriptorNode returnNode, ApiResponses responses) {
        SyntaxKind kind = returnNode.kind();
        if (kind == QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) returnNode;
            validateQualifiedType(qNode, responses);
        } else if (kind == UNION_TYPE_DESC) {
            // A|B|C|D
            UnionTypeDescriptorNode uNode = (UnionTypeDescriptorNode) returnNode;
            validateUnionType(uNode, responses);
        } else if (kind == SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode simpleRefNode = (SimpleNameReferenceNode) returnNode;
            validateSimpleNameReference(simpleRefNode, responses);
        } else if (kind == ERROR_TYPE_DESC) {
            if (!responses.containsKey("500")) {
                // error message
                updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, returnNode.location(), "500",
                        method, path);
            }
        } else if (kind == OPTIONAL_TYPE_DESC) {
             OptionalTypeDescriptorNode optionalNode = (OptionalTypeDescriptorNode) returnNode;
            validateReturnBallerinaToOas((TypeDescriptorNode) optionalNode.typeDescriptor(), responses);
        }
        //TODO: array type validation
    }

    private void validateSimpleNameReference(SimpleNameReferenceNode simpleRefNode, ApiResponses responses) {
        Optional<Symbol> symbol = context.semanticModel().symbol(simpleRefNode);
        //Access the status code and media type via record
        if (symbol.isEmpty()) {
            return;
        }
        if (symbol.get() instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol refType = (TypeReferenceTypeSymbol) symbol.get();
            TypeSymbol type = refType.typeDescriptor();
            if (type instanceof RecordTypeSymbol) {
                RecordTypeSymbol typeSymbol = (RecordTypeSymbol) type;
                List<TypeSymbol> typeInclusions = typeSymbol.typeInclusions();
                boolean isHttp = false;
                if (!typeInclusions.isEmpty()) {
                    for (TypeSymbol typeInSymbol : typeInclusions) {
                        if (HTTP.equals(typeInSymbol.getModule().orElseThrow().getName().orElseThrow())) {
                            isHttp = true;
                            Optional<String> code =
                                    generateApiResponseCode(typeInSymbol.getName().orElseThrow().trim());
                            if (code.isEmpty()) {
                                return;
                            }
                            balStatusCodes.add(code.get());
                            if (!responses.containsKey(code.get())) {
                                updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE,
                                        simpleRefNode.location(), code.get(), method, path);
                            } else {
                                // handle media types
                                ApiResponse apiResponse = responses.get(code.get());
                                Map<String, RecordFieldSymbol> fields = typeSymbol.fieldDescriptors();
                                TypeSymbol bodyFieldType = fields.get("body").typeDescriptor();
                                TypeDescKind bodyKind = bodyFieldType.typeKind();
                                String mediaType = getMediaType(bodyKind);
                                Content content = apiResponse.getContent();
                                if (content != null) {
                                    MediaType oasMtype = content.get(mediaType);
                                    if (!content.containsKey(mediaType)) {
                                        // not media type
                                        updateContext(context, CompilationError.UNDOCUMENTED_RETURN_MEDIA_TYPE,
                                                simpleRefNode.location(), mediaType, method, path);
                                        return;
                                    }
                                    if (oasMtype.getSchema() != null && oasMtype.getSchema().get$ref() != null &&
                                            bodyKind == TypeDescKind.TYPE_REFERENCE) {

                                        //TODO inline record both ballerina and schema
                                        //TODO array type
                                        Optional<String> schemaName =
                                                extractReferenceType(oasMtype.getSchema().get$ref());
                                        if (schemaName.isEmpty()) {
                                            return;
                                        }
                                        TypeValidatorUtils.validateRecordType(
                                                openAPI.getComponents().getSchemas().get(schemaName.get()),
                                                bodyFieldType,
                                                ((TypeReferenceTypeSymbol) bodyFieldType).definition().getName().get(),
                                                context, openAPI, schemaName.get());
                                    }
                                }
                            }
                        }
                    }
                }
                if (!isHttp) {
                    // validate normal Record status code 200 and application json
                    if (!responses.containsKey("200")) {
                        updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, simpleRefNode.location(),
                                "200", method, path);
                    } else {
                        // record validation - done
                        // Array validation
                        ApiResponse apiResponse = responses.get("200");
                        Content content = apiResponse.getContent();
                        MediaType oasMtype = content.get("application/json");

                        if (oasMtype.getSchema() != null && oasMtype.getSchema().get$ref() != null) {
                            Optional<String> schemaName = extractReferenceType(oasMtype.getSchema().get$ref());
                            if (schemaName.isEmpty()) {
                                return;
                            }
                            TypeValidatorUtils.validateRecordType(
                                    openAPI.getComponents().getSchemas().get(schemaName.get()),
                                    typeSymbol, refType.definition().getName().orElse(null),
                                    context, openAPI, schemaName.orElse(null));
                        }
                    }
                }
            }
        }
    }

    private void validateUnionType(UnionTypeDescriptorNode uNode, ApiResponses responses) {
        List<Node> unionNodes = new ArrayList<>();
        while (uNode != null) {
            unionNodes.add(uNode.rightTypeDesc());
            if (uNode.leftTypeDesc().kind() == UNION_TYPE_DESC) {
                uNode = (UnionTypeDescriptorNode) uNode.leftTypeDesc();
            } else {
                unionNodes.add(uNode.leftTypeDesc());
                uNode = null;
            }
        }
        for (Node reNode : unionNodes) {
            if (reNode.kind() == QUALIFIED_NAME_REFERENCE) {
                QualifiedNameReferenceNode traversRNode = (QualifiedNameReferenceNode) reNode;
                validateQualifiedType(traversRNode, responses);
            } else if (reNode.kind() == SIMPLE_NAME_REFERENCE) {
                SimpleNameReferenceNode simpleRefNode = (SimpleNameReferenceNode) reNode;
                validateSimpleNameReference(simpleRefNode, responses);
            } else if (reNode.kind() == ERROR_TYPE_DESC) {
                if (!responses.containsKey("500")) {
                    updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, uNode.location(), "500",
                            method, path);
                }
            }
        }
    }

    private void validateQualifiedType(QualifiedNameReferenceNode qNode, ApiResponses responses) {
        qNode.identifier();
        // get the status code
        Optional<String> statusCode = generateApiResponseCode(qNode.identifier().toString().trim());
        if (statusCode.isPresent()) {
            String balCode = statusCode.get();
            balStatusCodes.add(balCode);
            if (!responses.containsKey(balCode)) {
                // Undocumented status code
                updateContext(context, CompilationError.UNDOCUMENTED_RETURN_CODE, qNode.location(), balCode,
                        method, path);
            }
        }
    }

    /**
     * Get related http status code.
     */
    private Optional<String> generateApiResponseCode(String identifier) {

        if (HTTP_CODES.containsKey(identifier)) {
            return Optional.of(HTTP_CODES.get(identifier));
        } else {
            return Optional.empty();
        }
    }
}
