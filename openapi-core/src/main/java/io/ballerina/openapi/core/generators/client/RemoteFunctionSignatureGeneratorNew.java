/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.parameter.HeadersParameterGenerator;
import io.ballerina.openapi.core.generators.client.parameter.PathParameterGenerator;
import io.ballerina.openapi.core.generators.client.parameter.QueriesParameterGenerator;
import io.ballerina.openapi.core.generators.client.parameter.RequestBodyGeneratorNew;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages.OAS_CLIENT_100;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;

public class RemoteFunctionSignatureGeneratorNew implements FunctionSignatureGenerator {
    OpenAPI openAPI;
    Operation operation;
    List<ClientDiagnostic> diagnostics  = new ArrayList<>();
    boolean treatDefaultableAsRequired = false;
    FunctionReturnTypeGeneratorImp functionReturnTypeGenerator;
    private final String httpMethod;
    private final String path;

    private boolean hasDefaultHeader = false;
    private boolean hasHeadersParam = false;
    private boolean hasQueriesParam = false;

    public RemoteFunctionSignatureGeneratorNew(Operation operation, OpenAPI openAPI, String httpMethod,
                                               String path) {
        this.operation = operation;
        this.openAPI = openAPI;
        this.httpMethod = httpMethod;
        this.path = path;
        this.functionReturnTypeGenerator = new FunctionReturnTypeGeneratorImp(operation, openAPI, httpMethod);
    }

    @Override
    public Optional<FunctionSignatureNode> generateFunctionSignature() {
        List<Parameter> parameters = operation.getParameters();
        ParametersInfo parametersInfo = getParametersInfo(parameters);

        if (parametersInfo == null) {
            return Optional.empty();
        }

        List<Node> defaultable = parametersInfo.defaultable();
        List<Node> parameterList = parametersInfo.parameterList();

        // filter defaultable parameters
        if (!defaultable.isEmpty()) {
            parameterList.addAll(defaultable);
        }
        // Remove the last comma
        if (!parameterList.isEmpty()) {
            parameterList.remove(parameterList.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameterNodes = createSeparatedNodeList(parameterList);

        // 3. return statements
        FunctionReturnTypeGeneratorImp returnTypeGenerator = getFunctionReturnTypeGenerator();
        Optional<ReturnTypeDescriptorNode> returnType = returnTypeGenerator.getReturnType();
        diagnostics.addAll(returnTypeGenerator.getDiagnostics());
        if (returnType.isEmpty()) {
            return Optional.empty();
        }

        return returnType.map(returnTypeDescriptorNode -> NodeFactory.createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterNodes,
                createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode));
    }

    protected ParametersInfo getParametersInfo(List<Parameter> parameters) {
        List<Node> parameterList = new ArrayList<>();
        List<Node> defaultable = new ArrayList<>();
        Token comma = createToken(COMMA_TOKEN);

        List<Parameter> headerParameters = new ArrayList<>();
        List<Parameter> queryParameters = new ArrayList<>();

        // 1. path parameters
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (parameter.getIn().equals("path")) {
                    PathParameterGenerator paramGenerator = new PathParameterGenerator(parameter, openAPI);
                    Optional<ParameterNode> param = paramGenerator.generateParameterNode(
                            treatDefaultableAsRequired);
                    if (param.isEmpty()) {
                        diagnostics.addAll(paramGenerator.getDiagnostics());
                        return null;
                    }
                    // Path parameters are always required.
                    parameterList.add(param.get());
                    parameterList.add(comma);
                }
            }
        }

        // 1. requestBody
        if (operation.getRequestBody() != null) {
            RequestBodyGeneratorNew requestBodyGenerator = new RequestBodyGeneratorNew(operation.getRequestBody(),
                    openAPI);
            Optional<ParameterNode> requestBody = requestBodyGenerator.generateParameterNode(
                    treatDefaultableAsRequired);
            if (requestBody.isEmpty()) {
                diagnostics.addAll(requestBodyGenerator.getDiagnostics());
                return null;
            }
            headerParameters = requestBodyGenerator.getHeaderSchemas();
            parameterList.add(requestBody.get());
            parameterList.add(comma);
        }

        // 2. parameters -  query, headers
        if (parameters != null) {
            populateQueryAndHeaderParameters(parameters, queryParameters, headerParameters);

            HeadersParameterGenerator headersParameterGenerator = new HeadersParameterGenerator(headerParameters,
                    openAPI, operation, httpMethod, path);
            Optional<ParameterNode> headers;
            if (headerParameters.isEmpty()) {
                hasDefaultHeader = true;
                headers = headersParameterGenerator.getDefaultParameterNode(treatDefaultableAsRequired);
            } else {
                headers = headersParameterGenerator.generateParameterNode();
            }

            if (headers.isPresent()) {
                hasHeadersParam = true;
                if (headers.get() instanceof RequiredParameterNode headerNode) {
                    parameterList.add(headerNode);
                    parameterList.add(comma);
                } else {
                    defaultable.add(headers.get());
                    defaultable.add(comma);
                }
            } else if (!headerParameters.isEmpty()) {
                diagnostics.addAll(headersParameterGenerator.getDiagnostics());
                return null;
            }

            QueriesParameterGenerator queriesParameterGenerator = new QueriesParameterGenerator(queryParameters,
                    openAPI, operation, httpMethod, path);
            Optional<ParameterNode> queries = queriesParameterGenerator.generateParameterNode();
            if (queries.isPresent()) {
                hasQueriesParam = true;
                parameterList.add(queries.get());
                parameterList.add(comma);
            } else if (!queryParameters.isEmpty()) {
                diagnostics.addAll(queriesParameterGenerator.getDiagnostics());
                return null;
            }
        }
        return new ParametersInfo(parameterList, defaultable);
    }

    private void populateQueryAndHeaderParameters(List<Parameter> parameters, List<Parameter> queryParameters,
                                                  List<Parameter> headerParameters) {
    for (Parameter parameter : parameters) {
        if (parameter.get$ref() != null) {
            String paramType = null;
            try {
                paramType = extractReferenceType(parameter.get$ref());
            } catch (BallerinaOpenApiException e) {
                ClientDiagnosticImp clientDiagnostic = new ClientDiagnosticImp(OAS_CLIENT_100,
                        parameter.get$ref());
                diagnostics.add(clientDiagnostic);
            }
            parameter = openAPI.getComponents().getParameters().get(paramType);
        }

        String in = parameter.getIn();

        switch (in) {
            case "query":
                queryParameters.add(parameter);
                break;
            case "header":
                headerParameters.add(parameter);
                break;
            default:
                break;
        }
    }
}

    protected FunctionReturnTypeGeneratorImp getFunctionReturnTypeGenerator() {
        return functionReturnTypeGenerator;
    }

    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public boolean hasDefaultHeaders() {
        return hasDefaultHeader;
    }

    public boolean hasHeaders() {
        return hasHeadersParam;
    }

    public boolean hasQueries() {
        return hasQueriesParam;
    }
}
