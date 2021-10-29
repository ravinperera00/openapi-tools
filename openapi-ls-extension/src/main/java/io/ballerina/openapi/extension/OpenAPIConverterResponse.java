package io.ballerina.openapi.extension;

/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

import io.ballerina.openapi.converter.service.OASResult;

/**
 * The extended service for the OpenAPIConverter endpoint.
 *
 * @since 2.0.0
 */
public class OpenAPIConverterResponse {
    private OASResult yamlContent;

    public OASResult getYamlContent() {
        return yamlContent;
    }

    public void setYamlContent(OASResult yamlContent) {
        this.yamlContent = yamlContent;
    }

    public OpenAPIConverterResponse() {
    }
}
