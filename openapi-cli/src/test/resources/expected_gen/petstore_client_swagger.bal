// AUTO-GENERATED FILE. DO NOT MODIFY.
// This file is auto-generated by the Ballerina OpenAPI tool.

import ballerina/http;
import ballerina/mime;

# This is a sample server Petstore server.  You can find out more about     Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).      For this sample, you can use the api key `special-key` to test the authorization     filters.
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig? apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config, string serviceUrl = "https://petstore.swagger.io/v2") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, timeout: config.timeout, forwarded: config.forwarded, poolConfig: config.poolConfig, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, validation: config.validation};
        do {
            if config.http1Settings is ClientHttp1Settings {
                ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);
                httpClientConfig.http1Settings = {...settings};
            }
            if config.http2Settings is http:ClientHttp2Settings {
                httpClientConfig.http2Settings = check config.http2Settings.ensureType(http:ClientHttp2Settings);
            }
            if config.cache is http:CacheConfig {
                httpClientConfig.cache = check config.cache.ensureType(http:CacheConfig);
            }
            if config.responseLimits is http:ResponseLimitConfigs {
                httpClientConfig.responseLimits = check config.responseLimits.ensureType(http:ResponseLimitConfigs);
            }
            if config.secureSocket is http:ClientSecureSocket {
                httpClientConfig.secureSocket = check config.secureSocket.ensureType(http:ClientSecureSocket);
            }
            if config.proxy is http:ProxyConfig {
                httpClientConfig.proxy = check config.proxy.ensureType(http:ProxyConfig);
            }
        }
        if config.auth is ApiKeysConfig {
            self.apiKeyConfig = (<ApiKeysConfig>config.auth).cloneReadOnly();
        } else {
            config.auth = <http:BearerTokenConfig>config.auth;
            self.apiKeyConfig = ();
        }
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }

    # Add a new pet to the store
    #
    # + headers - Headers to be sent with the request
    # + payload - Pet object that needs to be added to the store
    # + return - Invalid input
    remote isolated function addPet(Pet payload, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/pet`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Create user
    #
    # + headers - Headers to be sent with the request
    # + request - Created user object
    # + return - successful operation
    remote isolated function createUser(http:Request request, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/user`;
        // TODO: Update the request as needed;
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Creates list of users with given input array
    #
    # + headers - Headers to be sent with the request
    # + request - List of user object
    # + return - successful operation
    remote isolated function createUsersWithArrayInput(http:Request request, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/user/createWithArray`;
        // TODO: Update the request as needed;
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Creates list of users with given input array
    #
    # + headers - Headers to be sent with the request
    # + request - List of user object
    # + return - successful operation
    remote isolated function createUsersWithListInput(http:Request request, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/user/createWithList`;
        // TODO: Update the request as needed;
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Delete purchase order by ID
    #
    # + orderId - ID of the order that needs to be deleted
    # + headers - Headers to be sent with the request
    # + return - Invalid ID supplied
    remote isolated function deleteOrder(int orderId, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/store/order/${getEncodedUri(orderId)}`;
        return self.clientEp->delete(resourcePath, headers = headers);
    }

    # Deletes a pet
    #
    # + petId - Pet id to delete
    # + headers - Headers to be sent with the request
    # + return - Invalid ID supplied
    remote isolated function deletePet(int petId, DeletePetHeaders headers = {}) returns http:Response|error {
        string resourcePath = string `/pet/${getEncodedUri(petId)}`;
        map<string|string[]> httpHeaders = getMapForHeaders(headers);
        return self.clientEp->delete(resourcePath, headers = httpHeaders);
    }

    # Delete user
    #
    # + username - The name that needs to be deleted
    # + headers - Headers to be sent with the request
    # + return - Invalid username supplied
    remote isolated function deleteUser(string username, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/user/${getEncodedUri(username)}`;
        return self.clientEp->delete(resourcePath, headers = headers);
    }

    # Finds Pets by status
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - successful operation
    remote isolated function findPetsByStatus(map<string|string[]> headers = {}, *FindPetsByStatusQueries queries) returns Pet[]|error {
        string resourcePath = string `/pet/findByStatus`;
        map<Encoding> queryParamEncoding = {"status": {style: FORM, explode: true}};
        resourcePath = resourcePath + check getPathForQueryParam(queries, queryParamEncoding);
        return self.clientEp->get(resourcePath, headers);
    }

    # Finds Pets by tags
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - successful operation
    #
    # # Deprecated
    @deprecated
    remote isolated function findPetsByTags(map<string|string[]> headers = {}, *FindPetsByTagsQueries queries) returns Pet[]|error {
        string resourcePath = string `/pet/findByTags`;
        map<Encoding> queryParamEncoding = {"tags": {style: FORM, explode: true}};
        resourcePath = resourcePath + check getPathForQueryParam(queries, queryParamEncoding);
        return self.clientEp->get(resourcePath, headers);
    }

    # Returns pet inventories by status
    #
    # + headers - Headers to be sent with the request
    # + return - successful operation
    remote isolated function getInventory(map<string|string[]> headers = {}) returns record {|int:Signed32...;|}|error {
        string resourcePath = string `/store/inventory`;
        map<anydata> headerValues = {...headers};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api_key"] = self.apiKeyConfig?.api_key;
        }
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        return self.clientEp->get(resourcePath, httpHeaders);
    }

    # Find purchase order by ID
    #
    # + orderId - ID of pet that needs to be fetched
    # + headers - Headers to be sent with the request
    # + return - successful operation
    remote isolated function getOrderById(int orderId, map<string|string[]> headers = {}) returns Order|error {
        string resourcePath = string `/store/order/${getEncodedUri(orderId)}`;
        return self.clientEp->get(resourcePath, headers);
    }

    # Find pet by ID
    #
    # + petId - ID of pet to return
    # + headers - Headers to be sent with the request
    # + return - successful operation
    remote isolated function getPetById(int petId, map<string|string[]> headers = {}) returns Pet|error {
        string resourcePath = string `/pet/${getEncodedUri(petId)}`;
        map<anydata> headerValues = {...headers};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api_key"] = self.apiKeyConfig?.api_key;
        }
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        return self.clientEp->get(resourcePath, httpHeaders);
    }

    # Get user by user name
    #
    # + username - The name that needs to be fetched. Use user1 for testing.
    # + headers - Headers to be sent with the request
    # + return - successful operation
    remote isolated function getUserByName(string username, map<string|string[]> headers = {}) returns User|error {
        string resourcePath = string `/user/${getEncodedUri(username)}`;
        return self.clientEp->get(resourcePath, headers);
    }

    # Logs user into the system
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - successful operation
    remote isolated function loginUser(map<string|string[]> headers = {}, *LoginUserQueries queries) returns string|error {
        string resourcePath = string `/user/login`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }

    # Logs out current logged in user session
    #
    # + headers - Headers to be sent with the request
    # + return - successful operation
    remote isolated function logoutUser(map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/user/logout`;
        return self.clientEp->get(resourcePath, headers);
    }

    # Place an order for a pet
    #
    # + headers - Headers to be sent with the request
    # + request - order placed for purchasing the pet
    # + return - successful operation
    remote isolated function placeOrder(http:Request request, map<string|string[]> headers = {}) returns Order|error {
        string resourcePath = string `/store/order`;
        // TODO: Update the request as needed;
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Update an existing pet
    #
    # + headers - Headers to be sent with the request
    # + payload - Pet object that needs to be added to the store
    # + return - Invalid ID supplied
    remote isolated function updatePet(Pet payload, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/pet`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request, headers);
    }

    # Updates a pet in the store with form data
    #
    # + petId - ID of pet that needs to be updated
    # + headers - Headers to be sent with the request
    # + return - Invalid input
    remote isolated function updatePetWithForm(int petId, pet_petId_body payload, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/pet/${getEncodedUri(petId)}`;
        http:Request request = new;
        string encodedRequestBody = createFormURLEncodedRequestBody(payload);
        request.setPayload(encodedRequestBody, "application/x-www-form-urlencoded");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Updated user
    #
    # + username - name that need to be updated
    # + headers - Headers to be sent with the request
    # + request - Updated user object
    # + return - Invalid user supplied
    remote isolated function updateUser(string username, http:Request request, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/user/${getEncodedUri(username)}`;
        // TODO: Update the request as needed;
        return self.clientEp->put(resourcePath, request, headers);
    }

    # uploads an image
    #
    # + petId - ID of pet to update
    # + headers - Headers to be sent with the request
    # + return - successful operation
    remote isolated function uploadFile(int petId, petId_uploadImage_body payload, map<string|string[]> headers = {}) returns ApiResponse|error {
        string resourcePath = string `/pet/${getEncodedUri(petId)}/uploadImage`;
        http:Request request = new;
        mime:Entity[] bodyParts = check createBodyParts(payload);
        request.setBodyParts(bodyParts);
        return self.clientEp->post(resourcePath, request, headers);
    }
}
