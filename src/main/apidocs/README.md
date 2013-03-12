<!-- Table of Contents ---------------------------------------------------->
Contents
========
ToDo

<!-- Resource: /person ---------------------------------------------------->
/{domain}
============

**Description**: 

**Concrete class**: com.wadpam.oauth2.web.OAuth2Controller

<!-- Method: findByName() ---------------------------------------------------->		

registerFederated()
----------------

**Description**: Registers an access token from a separate (federated) OAuth2 Provider.

**Implementing Class**: com.wadpam.oauth2.web.OAuth2Controller

**REST path**: *     /{domain}/federated/v11

``` Request parameters ```

| Where | Name | Type | Description |
|-------|------|------|-------------|
| path | domain | String | for multi-tenancy |
| query | access_token | String | the access token to register |
| query | providerId | String | id of the OAuth2 provider |
| query | providerUserId | String | user's id at the OAuth2 provider |
| query | secret | String | only used for twitter |
| query | expires_in | Integer | seconds this access token is valid (from now) |
| query | appArg0 | String | provider-specific. For Salesforce, this is instance_url |


``` Response Codes ```

| HTTP Response Code | Message | Description |
|--------------------|---------|-------------|
   | 200 | OK | The token was registered for an existing user |
   | 201 | Created | A User was created, and the token was registered |

**Response Type**: <a href="api.html#com.wadpam.oauth2.json.JConnection" class="link">com.wadpam.oauth2.json.JConnection</a>

**Response Example**:

{<div>&nbsp;&nbsp;&nbsp;<b>"createdBy"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"createdDate"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"displayName"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"expireTime"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"id"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"imageUrl"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"profileUrl"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"providerId"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"providerUserId"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"refreshToken"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"secret"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"state"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"updatedBy"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"updatedDate"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"userId"</b>&nbsp;:&nbsp;String,</div>}
				

JSON Objects
============

<!-- JSON object: com.wadpam.oauth2.json.JConnection ---------------------------------------------------->		
com.wadpam.oauth2.json.JConnection
------------

| Name | Type | Description |
|------|------|-------------|
| displayName | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| expireTime | <a href="api.html#java.lang.Long" class="link">java.lang.Long</a> | 
 |
| imageUrl | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| profileUrl | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| providerId | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| providerUserId | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| refreshToken | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| secret | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| userId | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
<!-- JSON object: com.wadpam.open.json.JCursorPage ---------------------------------------------------->		
com.wadpam.open.json.JCursorPage
------------

| Name | Type | Description |
|------|------|-------------|
| cursorKey | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| items | <a href="api.html#java.util.Collection" class="link">java.util.Collection</a> | 
 |
| pageSize | int | 
 |
<!-- JSON object: java.lang.Long ---------------------------------------------------->		
java.lang.Long
------------

| Name | Type | Description |
|------|------|-------------|
| chars | void |  |
| long | <a href="api.html#java.lang.Long" class="link">java.lang.Long</a> |  |
| long | <a href="api.html#java.lang.Long" class="link">java.lang.Long</a> |  |
| long | <a href="api.html#java.lang.Long" class="link">java.lang.Long</a> |  |
<!-- JSON object: java.lang.Object ---------------------------------------------------->		
java.lang.Object
------------

| Name | Type | Description |
|------|------|-------------|
| class | java.lang.Class |  |
<!-- JSON object: java.lang.String ---------------------------------------------------->		
java.lang.String
------------

| Name | Type | Description |
|------|------|-------------|
| empty | boolean |  |
| chars | void |  |
| chars | void |  |
| bytes | void |  |
| bytes | byte |  |
| bytes | byte |  |
| bytes | byte |  |
<!-- JSON object: java.net.URI ---------------------------------------------------->		
java.net.URI
------------

| Name | Type | Description |
|------|------|-------------|
| scheme | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| absolute | boolean |  |
| opaque | boolean |  |
| rawSchemeSpecificPart | <a href="api.html#java.lang.String" class="link">java.lang.String</a> |  |
| schemeSpecificPart | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| rawAuthority | <a href="api.html#java.lang.String" class="link">java.lang.String</a> |  |
| authority | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| rawUserInfo | <a href="api.html#java.lang.String" class="link">java.lang.String</a> |  |
| userInfo | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| host | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| port | int | 
 |
| rawPath | <a href="api.html#java.lang.String" class="link">java.lang.String</a> |  |
| path | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| rawQuery | <a href="api.html#java.lang.String" class="link">java.lang.String</a> |  |
| query | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
| rawFragment | <a href="api.html#java.lang.String" class="link">java.lang.String</a> |  |
| fragment | <a href="api.html#java.lang.String" class="link">java.lang.String</a> | 
 |
<!-- JSON object: java.util.Collection ---------------------------------------------------->		
java.util.Collection
------------

| Name | Type | Description |
|------|------|-------------|
| empty | boolean |  |
<!-- JSON object: org.springframework.http.ResponseEntity ---------------------------------------------------->		
org.springframework.http.ResponseEntity
------------

| Name | Type | Description |
|------|------|-------------|
| statusCode | org.springframework.http.HttpStatus | 
 |

