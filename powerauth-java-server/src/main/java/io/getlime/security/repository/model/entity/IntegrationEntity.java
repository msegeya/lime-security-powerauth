package io.getlime.security.repository.model.entity;

import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Class representing an integration - essentially an application that is allowed to communicate
 * with this PowerAuth 2.0 Server instance.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Entity(name = "pa_integration")
public class IntegrationEntity implements Serializable {

    private static final long serialVersionUID = 3372029113954119581L;

    @Id
    @Column(name = "id", updatable = false, length = 37)
    private String id;

    @Column(name = "name", nullable = false, updatable = false)
    private String name;

    @Column(name = "client_token", nullable = false, updatable = false, length = 37)
    private String clientToken;

    @Column(name = "client_secret", nullable = false, updatable = false, length = 37)
    private String clientSecret;

    /**
     * Get the ID of an integration.
     * @return ID of an integration.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the ID of an integration.
     * @param id ID of an integration.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the name of an integration.
     * @return Name of an integration.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of an integration.
     * @param name Name of an integration.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the client token value. Basically, this value serves as integration's "username".
     * @return Client token.
     */
    public String getClientToken() {
        return clientToken;
    }

    /**
     * Set the client token value.
     * @param clientToken Client token.
     */
    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    /**
     * Get the client secret value. Basically, this value serves as integration's "password".
     * @return Client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Set the client secret value.
     * @param clientSecret Client secret.
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

}