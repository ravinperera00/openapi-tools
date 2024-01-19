/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.cmd;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.service.mapper.ServiceToOpenAPIMapper;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.OASResult;
import io.ballerina.openapi.service.mapper.utils.CodegenUtils;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.utils.CodegenUtils.resolveContractFileName;

/**
 * OpenApi related utility classes.
 *
 * @since 1.3.0
 */

public class OASContractGenerator {
    private SyntaxTree syntaxTree;
    private SemanticModel semanticModel;
    private Project project;
    private List<OpenAPIMapperDiagnostic> errors = new ArrayList<>();
    private PrintStream outStream = System.out;

    /**
     * Initialize constructor.
     */
    public OASContractGenerator() {

    }

    public List<OpenAPIMapperDiagnostic> getErrors() {
        return errors;
    }

    /**
     * This util for generating OAS files.
     *
     * @param servicePath The path to a single ballerina file.
     * @param outPath     The output directory to which the OpenAPI specifications should be generated to.
     * @param serviceName Filter the services to generate OpenAPI specification for service with this name.
     */
    public void generateOAS3DefinitionsAllService(Path servicePath, Path outPath, String serviceName,
                                                  Boolean needJson) {
        // Load project instance for single ballerina file
        project = ProjectLoader.loadProject(servicePath);
        DiagnosticResult diagnosticsFromCodeGenAndModify = project.currentPackage().runCodeGenAndModifyPlugins();
        boolean hasErrorsFromCodeGenAndModify = diagnosticsFromCodeGenAndModify.diagnostics().stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        DocumentId docId;
        Document doc;
        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
            docId = project.documentId(servicePath);
            ModuleId moduleId = docId.moduleId();
            doc = project.currentPackage().module(moduleId).document(docId);
        } else {
            // Take module instance for traversing the syntax tree
            Module currentModule = project.currentPackage().getDefaultModule();
            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();

            docId = documentIterator.next();
            doc = currentModule.document(docId);
        }
        Optional<Path> path = project.documentPath(docId);
        Path inputPath = path.orElse(null);

        syntaxTree = doc.syntaxTree();
        PackageCompilation compilation = project.currentPackage().getCompilation();
        boolean hasCompilationErrors = compilation.diagnosticResult()
                .diagnostics().stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        if (hasCompilationErrors || hasErrorsFromCodeGenAndModify) {
            // if there are any compilation errors, do not proceed
            return;
        }
        semanticModel = compilation.getSemanticModel(docId.moduleId());
        List<OASResult> openAPIDefinitions = ServiceToOpenAPIMapper.generateOAS3Definition(project, syntaxTree,
                semanticModel, serviceName, needJson, inputPath);

        if (!openAPIDefinitions.isEmpty()) {
            List<String> fileNames = new ArrayList<>();
            for (OASResult definition : openAPIDefinitions) {
                try {
                    this.errors.addAll(definition.getDiagnostics());
                    if (definition.getOpenAPI().isPresent()) {
                        Optional<String> content;
                        if (needJson) {
                            content = definition.getJson();
                        } else {
                            content = definition.getYaml();
                        }
                        String fileName = resolveContractFileName(outPath, definition.getServiceName(), needJson);
                        CodegenUtils.writeFile(outPath.resolve(fileName), content.get());
                        fileNames.add(fileName);
                    }
                } catch (IOException e) {
                    DiagnosticMessages message = DiagnosticMessages.OAS_CONVERTOR_108;
                    ExceptionDiagnostic error = new ExceptionDiagnostic(message.getCode(),
                            message.getDescription() + e.getLocalizedMessage(),
                            null);
                    this.errors.add(error);
                }
            }
            if (fileNames.isEmpty()) {
                return;
            }
            outStream.println("OpenAPI definition(s) generated successfully and copied to :");
            Iterator<String> iterator = fileNames.iterator();
            while (iterator.hasNext()) {
                outStream.println("-- " + iterator.next());
            }
        } else {
            DiagnosticMessages message = DiagnosticMessages.OAS_CONVERTOR_115;
            ExceptionDiagnostic error = new ExceptionDiagnostic(message.getCode(),
                    message.getDescription(), null);
            this.errors.add(error);
        }
    }
}
