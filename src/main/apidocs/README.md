<!-- Table of Contents ---------------------------------------------------->
Contents
========
1. com.wadpam.oauth2.web.OAuth2Controller
  * [registerFederated()](#registerfederated)
  * [unregisterFederated()](#unregisterfederated)

<!-- Resource: /person ---------------------------------------------------->
/{domain}
============

**Description**: Provides methods to register and unregister a federated OAuth2 access token.

**Concrete class**: com.wadpam.oauth2.web.OAuth2Controller

<!-- Method: findByName() ---------------------------------------------------->		

registerFederated()
----------------

**Description**: Registers an access token from a separate (federated) OAuth2 Provider.

**Implementing Class**: com.wadpam.oauth2.web.OAuth2Controller

**REST path**: *     /{domain}/federated/v11

**Request parameters**:

| Where | Name | Type | Description |
|-------|------|------|-------------|
| path | domain | [String](#string) | for multi-tenancy |
| query | access_token | [String](#string) | the access token to register |
| query | providerId | [String](#string) | id of the OAuth2 provider |
| query | providerUserId | [String](#string) | user's id at the OAuth2 provider |
| query | secret | [String](#string) | only used for twitter |
| query | expires_in | [Integer](#integer) | seconds this access token is valid (from now) |
| query | appArg0 | [String](#string) | provider-specific. For Salesforce, this is instance_url |


**Response Codes**:

| HTTP Response Code | Message | Description |
|--------------------|---------|-------------|
| 200 | OK | The token was registered for an existing user |
| 201 | Created | A User was created, and the token was registered |

**Response Type**: [com.wadpam.oauth2.json.JConnection](#comwadpamoauth2jsonjconnection)

**Response Example**:
{<div>&nbsp;&nbsp;&nbsp;<b>"createdBy"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"createdDate"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"displayName"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"expireTime"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"id"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"imageUrl"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"profileUrl"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"providerId"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"providerUserId"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"refreshToken"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"secret"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"state"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"updatedBy"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"updatedDate"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"userId"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"userRoles"</b>&nbsp;:&nbsp;String,</div>}
				
<!-- Method: findByName() ---------------------------------------------------->		

unregisterFederated()
----------------

**Description**: Removes the cookie for this host and path.

**Implementing Class**: com.wadpam.oauth2.web.OAuth2Controller

**REST path**: DELETE     /{domain}/federated/v11/{providerId}

**Request parameters**:

| Where | Name | Type | Description |
|-------|------|------|-------------|
| path | domain | [String](#string) | used to construct the cookie path |
| path | providerId | [String](#string) | not used. |


**Response Codes**:

| HTTP Response Code | Message | Description |
|--------------------|---------|-------------|
| 200 | OK | Cookie will be deleted. |

**Response Type**: [java.lang.Void](#javalangvoid)

**Response Example**:
{<div>&nbsp;&nbsp;&nbsp;<b>"createdBy"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"createdDate"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"displayName"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"expireTime"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"id"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"imageUrl"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"profileUrl"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"providerId"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"providerUserId"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"refreshToken"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"secret"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"state"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"updatedBy"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"updatedDate"</b>&nbsp;:&nbsp;Long,</div><div>&nbsp;&nbsp;&nbsp;<b>"userId"</b>&nbsp;:&nbsp;String,</div><div>&nbsp;&nbsp;&nbsp;<b>"userRoles"</b>&nbsp;:&nbsp;String,</div>}
				

JSON Objects
============

<!-- JSON object: com.wadpam.oauth2.json.JConnection ---------------------------------------------------->		
com.wadpam.oauth2.json.JConnection
------------

| Name | Type | Description |
|------|------|-------------|
| displayName | [java.lang.String](#javalangstring) | The name, as entered in the social site
 |
| expireTime | [java.lang.Long](#javalanglong) | Timestamp when the access token will expire
 |
| imageUrl | [java.lang.String](#javalangstring) | Link to user's image at social site
 |
| profileUrl | [java.lang.String](#javalangstring) | Link to user's profile at social site
 |
| providerId | [java.lang.String](#javalangstring) | provider id, facebook, twitter, salesforce
 |
| providerUserId | [java.lang.String](#javalangstring) | The user's id at social site
 |
| refreshToken | [java.lang.String](#javalangstring) | Long-lived token used to refresh access_token
 |
| secret | [java.lang.String](#javalangstring) | Used by twitter
 |
| userId | [java.lang.String](#javalangstring) | User's id in this system
 |
| userRoles | [java.lang.String](#javalangstring) | User's roles, comma-separated, populated by registerFederated()
 |
<!-- JSON object: com.wadpam.open.json.JCursorPage ---------------------------------------------------->		
com.wadpam.open.json.JCursorPage
------------

| Name | Type | Description |
|------|------|-------------|
| cursorKey | [java.lang.String](#javalangstring) | 
 |
| items | [java.util.Collection](#javautilcollection) | 
 |
| pageSize | [int](#int) | 
 |
| totalSize | [java.lang.Integer](#javalanginteger) | 
 |
<!-- JSON object: java.lang.Integer ---------------------------------------------------->		
java.lang.Integer
------------

| Name | Type | Description |
|------|------|-------------|
| chars | [void](#void) |  |
| integer | [java.lang.Integer](#javalanginteger) |  |
| integer | [java.lang.Integer](#javalanginteger) |  |
| integer | [java.lang.Integer](#javalanginteger) |  |
<!-- JSON object: java.lang.Long ---------------------------------------------------->		
java.lang.Long
------------

| Name | Type | Description |
|------|------|-------------|
| chars | [void](#void) |  |
| long | [java.lang.Long](#javalanglong) |  |
| long | [java.lang.Long](#javalanglong) |  |
| long | [java.lang.Long](#javalanglong) |  |
<!-- JSON object: java.lang.Object ---------------------------------------------------->		
java.lang.Object
------------

| Name | Type | Description |
|------|------|-------------|
| class | [java.lang.Class](#javalangclass) |  |
<!-- JSON object: java.lang.String ---------------------------------------------------->		
java.lang.String
------------

| Name | Type | Description |
|------|------|-------------|
| empty | [boolean](#boolean) |  |
| chars | [void](#void) |  |
| chars | [void](#void) |  |
| bytes | [void](#void) |  |
| bytes | [byte](#byte) |  |
| bytes | [byte](#byte) |  |
| bytes | [byte](#byte) |  |
<!-- JSON object: java.net.URI ---------------------------------------------------->		
java.net.URI
------------

| Name | Type | Description |
|------|------|-------------|
| scheme | [java.lang.String](#javalangstring) | 
 |
| absolute | [boolean](#boolean) |  |
| opaque | [boolean](#boolean) |  |
| rawSchemeSpecificPart | [java.lang.String](#javalangstring) |  |
| schemeSpecificPart | [java.lang.String](#javalangstring) | 
 |
| rawAuthority | [java.lang.String](#javalangstring) |  |
| authority | [java.lang.String](#javalangstring) | 
 |
| rawUserInfo | [java.lang.String](#javalangstring) |  |
| userInfo | [java.lang.String](#javalangstring) | 
 |
| host | [java.lang.String](#javalangstring) | 
 |
| port | [int](#int) | 
 |
| rawPath | [java.lang.String](#javalangstring) |  |
| path | [java.lang.String](#javalangstring) | 
 |
| rawQuery | [java.lang.String](#javalangstring) |  |
| query | [java.lang.String](#javalangstring) | 
 |
| rawFragment | [java.lang.String](#javalangstring) |  |
| fragment | [java.lang.String](#javalangstring) | 
 |
<!-- JSON object: java.util.Collection ---------------------------------------------------->		
java.util.Collection
------------

| Name | Type | Description |
|------|------|-------------|
| empty | [boolean](#boolean) |  |
<!-- JSON object: org.springframework.http.ResponseEntity ---------------------------------------------------->		
org.springframework.http.ResponseEntity
------------

| Name | Type | Description |
|------|------|-------------|
| statusCode | [org.springframework.http.HttpStatus](#orgspringframeworkhttphttpstatus) | 
 |

