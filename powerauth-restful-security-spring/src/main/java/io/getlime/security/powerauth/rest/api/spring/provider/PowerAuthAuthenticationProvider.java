/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.rest.api.spring.provider;

import com.google.common.io.BaseEncoding;
import io.getlime.powerauth.soap.VerifySignatureRequest;
import io.getlime.powerauth.soap.VerifySignatureResponse;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.http.PowerAuthHttpBody;
import io.getlime.security.powerauth.http.PowerAuthHttpHeader;
import io.getlime.security.powerauth.rest.api.base.application.PowerAuthApplicationConfiguration;
import io.getlime.security.powerauth.rest.api.base.authentication.PowerAuthApiAuthentication;
import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;
import io.getlime.security.powerauth.rest.api.base.provider.PowerAuthAuthenticationProviderBase;
import io.getlime.security.powerauth.rest.api.spring.authentication.PowerAuthApiAuthenticationImpl;
import io.getlime.security.powerauth.rest.api.spring.authentication.PowerAuthAuthenticationImpl;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Implementation of PowerAuth authentication provider.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 *
 */
@Component
public class PowerAuthAuthenticationProvider extends PowerAuthAuthenticationProviderBase implements AuthenticationProvider {

    private PowerAuthServiceClient powerAuthClient;

    private PowerAuthApplicationConfiguration applicationConfiguration;

    @Autowired
    public void setPowerAuthClient(PowerAuthServiceClient powerAuthClient) {
        this.powerAuthClient = powerAuthClient;
    }

    @Autowired
    public void setApplicationConfiguration(PowerAuthApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        PowerAuthAuthenticationImpl powerAuthAuthentication = (PowerAuthAuthenticationImpl) authentication;

        VerifySignatureRequest soapRequest = new VerifySignatureRequest();
        soapRequest.setActivationId(powerAuthAuthentication.getActivationId());
        soapRequest.setApplicationKey(powerAuthAuthentication.getApplicationKey());
        soapRequest.setSignature(powerAuthAuthentication.getSignature());
        soapRequest.setSignatureType(powerAuthAuthentication.getSignatureType());
        soapRequest.setData(PowerAuthHttpBody.getSignatureBaseString(
                powerAuthAuthentication.getHttpMethod(),
                powerAuthAuthentication.getRequestUri(),
                powerAuthAuthentication.getNonce(),
                powerAuthAuthentication.getData()
        ));

        VerifySignatureResponse soapResponse = powerAuthClient.verifySignature(soapRequest);

        if (soapResponse.isSignatureValid()) {
            PowerAuthApiAuthenticationImpl apiAuthentication = new PowerAuthApiAuthenticationImpl();
            apiAuthentication.setActivationId(soapResponse.getActivationId());
            apiAuthentication.setUserId(soapResponse.getUserId());
            apiAuthentication.setAuthenticated(true);
            return apiAuthentication;
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == PowerAuthAuthenticationImpl.class;
    }

    /**
     * Validate the signature from the PowerAuth 2.0 HTTP header against the provided HTTP method, request body and URI identifier.
     * Make sure to accept only allowed signatures.
     * @param httpMethod HTTP method (GET, POST, ...)
     * @param httpBody Body of the HTTP request.
     * @param requestUriIdentifier Request URI identifier.
     * @param httpAuthorizationHeader PowerAuth 2.0 HTTP authorization header.
     * @param allowedSignatureTypes Allowed types of the signature.
     * @return Instance of a PowerAuthApiAuthenticationImpl on successful authorization.
     * @throws PowerAuthAuthenticationException In case authorization fails, exception is raised.
     */
    public PowerAuthApiAuthentication validateRequestSignature(
            String httpMethod,
            byte[] httpBody,
            String requestUriIdentifier,
            String httpAuthorizationHeader,
            List<PowerAuthSignatureTypes> allowedSignatureTypes
    ) throws PowerAuthAuthenticationException {

        // Check for HTTP PowerAuth signature header
        if (httpAuthorizationHeader == null || httpAuthorizationHeader.equals("undefined")) {
            throw new PowerAuthAuthenticationException("POWER_AUTH_SIGNATURE_INVALID_EMPTY");
        }

        // Parse HTTP header
        Map<String, String> httpHeaderInfo = PowerAuthHttpHeader.parsePowerAuthSignatureHTTPHeader(httpAuthorizationHeader);

        // Check if the parsing was successful
        if (httpHeaderInfo == null) {
            throw new PowerAuthAuthenticationException("POWER_AUTH_SIGNATURE_INVALID_EMPTY");
        }

        // Fetch HTTP header attributes
        String activationId = httpHeaderInfo.get(PowerAuthHttpHeader.ACTIVATION_ID);
        if (activationId == null) {
            throw new PowerAuthAuthenticationException("POWER_AUTH_ACTIVATION_ID_EMPTY");
        }
        String nonce = httpHeaderInfo.get(PowerAuthHttpHeader.NONCE);
        if (nonce == null) {
            throw new PowerAuthAuthenticationException("POWER_AUTH_NONCE_EMPTY");
        }
        String signatureType = httpHeaderInfo.get(PowerAuthHttpHeader.SIGNATURE_TYPE);
        if (signatureType == null) {
            throw new PowerAuthAuthenticationException("POWER_AUTH_SIGNATURE_TYPE_EMPTY");
        }
        String signature = httpHeaderInfo.get(PowerAuthHttpHeader.SIGNATURE);
        if (signature == null) {
            throw new PowerAuthAuthenticationException("POWER_AUTH_SIGNATURE_EMPTY");
        }
        String applicationId = httpHeaderInfo.get(PowerAuthHttpHeader.APPLICATION_ID);
        if (applicationId == null) {
            throw new PowerAuthAuthenticationException("POWER_AUTH_APPLICATION_EMPTY");
        }

        // Check if the application is allowed, "true" is the default behavior
        if (applicationConfiguration != null) {
            boolean isApplicationAllowed = applicationConfiguration.isAllowedApplicationKey(applicationId);
            if (!isApplicationAllowed) {
                throw new PowerAuthAuthenticationException("POWER_AUTH_SIGNATURE_INVALID_APPLICATION_ID");
            }
        }

        // Check if the signature type is allowed
        PowerAuthSignatureTypes expectedSignatureType = PowerAuthSignatureTypes.getEnumFromString(signatureType);
        if (!allowedSignatureTypes.contains(expectedSignatureType)) {
            throw new PowerAuthAuthenticationException("POWER_AUTH_SIGNATURE_TYPE_INVALID");
        }

        // Configure PowerAuth authentication object
        PowerAuthAuthenticationImpl powerAuthAuthentication = new PowerAuthAuthenticationImpl();
        powerAuthAuthentication.setActivationId(activationId);
        powerAuthAuthentication.setApplicationKey(applicationId);
        powerAuthAuthentication.setNonce(BaseEncoding.base64().decode(nonce));
        powerAuthAuthentication.setSignatureType(signatureType);
        powerAuthAuthentication.setSignature(signature);
        powerAuthAuthentication.setHttpMethod(httpMethod);
        powerAuthAuthentication.setRequestUri(requestUriIdentifier);
        powerAuthAuthentication.setData(httpBody);

        // Call the authentication
        PowerAuthApiAuthentication auth = (PowerAuthApiAuthentication) this.authenticate(powerAuthAuthentication);

        // In case authentication is null, throw PowerAuth exception
        if (auth == null) {
            throw new PowerAuthAuthenticationException("POWER_AUTH_SIGNATURE_INVALID_VALUE");
        }

        return auth;
    }

}
