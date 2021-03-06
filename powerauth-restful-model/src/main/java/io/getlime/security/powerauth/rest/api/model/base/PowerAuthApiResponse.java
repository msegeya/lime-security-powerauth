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
package io.getlime.security.powerauth.rest.api.model.base;

/**
 * Generic response object for all PowerAuth RESTful API responses.
 *
 * @author Petr Dvorak
 *
 * @param <T> Type of the response object
 */
public class PowerAuthApiResponse<T> {

    /**
     * Response status string
     */
    public class Status {

        /**
         * In case response was OK
         */
        public static final String OK = "OK";

        /**
         * In case an error response is sent
         */
        public static final String ERROR = "ERROR";

    }

    /**
     * Response encryption type
     */
    public class Encryption {

        /**
         * None
         */
        public static final String NONE = "none";

        /**
         * Personalized
         */
        public static final String PERSONALIZED = "personalized";

        /**
         * Non-personalized
         */
        public static final String NON_PERSONALIZED = "nonpersonalized";

    }

    private String status;
    private String encryption;
    private T responseObject;

    /**
     * Default constructor
     */
    public PowerAuthApiResponse() {
    }

    /**
     * Constructor with response status and response object
     * @param status Response status, use static constant from {@link Status} class.
     * @param responseObject Response object.
     */
    public PowerAuthApiResponse(String status, T responseObject) {
        this.status = status;
        this.encryption = Encryption.NONE;
        this.responseObject = responseObject;
    }

    /**
     * Constructor with response status, encryption type and response object
     * @param status Response status, use static constant from {@link Status} class.
     * @param encryption Response encryption type, use static constant from {@link Encryption} class.
     * @param responseObject Response object.
     */
    public PowerAuthApiResponse(String status, String encryption, T responseObject) {
        this.status = status;
        this.encryption = encryption;
        this.responseObject = responseObject;
    }

    /**
     * Get response status.
     * @return Response status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set response status
     * @param status Response status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get encryption type.
     * @return Encryption type.
     */
    public String getEncryption() {
        return encryption;
    }

    /**
     * Set encryption type.
     * @param encryption Encryption type.
     */
    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    /**
     * Get response object
     * @return Response object
     */
    public T getResponseObject() {
        return responseObject;
    }

    /**
     * Set response object
     * @param responseObject Response object
     */
    public void setResponseObject(T responseObject) {
        this.responseObject = responseObject;
    }

}
