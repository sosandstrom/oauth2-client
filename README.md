oauth2-client
=============

Library to be used by a service acting as an OAuth2 client.

Three main components
---------------------
1. OAuth2Service, which contains the business logic
2. OAuth2Interceptor, which authenticates requests based on registered tokens
3. OAuth2Controller, which provides methods to register and unregister a federated token

API docs
--------
For API documentation, please see
[src/main/apidocs](tree/master/src/main/apidocs).

Auto-register your federated token
---------------------------------
The OAuth2Interceptor has a feature to auto-register a federated token. If the
token is not found locally, and the following parameters are present on the request

* providerId
* providerUserId

then the interceptor will invoke the `registerFederated()` method on the OAuth2Service,
and retry the authentication. For this support, the OAuth2Service must be wired to the
OAuth2Interceptor, and the user must have an existing connection with the federated provider.
