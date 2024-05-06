// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
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

# Service validation code.
# + contract - OpenAPI contract link
# + tags - OpenAPI tags
# + operations - OpenAPI operations
# + excludeTags - Disable the OpenAPI validator for these tags
# + excludeOperations - Disable the OpenAPI validator for these operations
# + failOnErrors - Enable the OpenAPI validator
# + embed - Enable auto-inject of OpenAPI documentation to current service
# + title - Title for generated OpenAPI contract
# + version - Version for generated OpenAPI contract
public type ServiceInformation record {|
    string contract = "";
    string[]? tags = [];
    string[]? operations = [];
    string[]? excludeTags = [];
    string[]? excludeOperations = [];
    boolean failOnErrors = true;
    boolean embed = false;
    string title?;
    string version?;
|};

// # Client configurations code.
// # + tags - OpenAPI tags that filter the openapi operations which need to be generated as client method
// # + operations - OpenAPI operationIds that filter the openapi operations which need to be generated as client method
// # + nullable - This enables to generate all data types in the record with Ballerina nil support
// # + isResource - Select client methods as resources for generation
// # + license - License path for adding license headers
// public type ClientConfiguration record {|
//     string[]? tags = [];
//     string[]? operations = [];
//     boolean nullable = false;
//     boolean isResource = true;
//     string license = "// AUTO-GENERATED FILE. DO NOT MODIFY.\n\n " +
//                      "// This file is auto-generated by the Ballerina OpenAPI tool.\n";
// |};

# Annotation for additional OpenAPI information of a Ballerina service.
public annotation ServiceInformation ServiceInfo on service;
// # Annotation for additional OpenAPI configurations of a Ballerina client.
// public const annotation ClientConfiguration ClientConfig on source client;
