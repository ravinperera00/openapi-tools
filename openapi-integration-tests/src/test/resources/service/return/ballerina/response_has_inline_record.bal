// AUTO-GENERATED FILE.
// This file is auto-generated by the Ballerina OpenAPI tool.

import ballerina/http;

public type InlineResponse400BadRequest record {|
    *http:BadRequest;
    inline_response_400 body;
|};

public type inline_response_400 record {
    # The error ID.
    int id?;
    # The error name.
    string errorType?;
};
