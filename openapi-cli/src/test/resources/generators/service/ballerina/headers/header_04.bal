import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Info for a specific pet
    #
    # + return - An paged array of pets
    resource function get pets(@http:Header string[] x\-request\-id) returns http:Ok {
    }
}
