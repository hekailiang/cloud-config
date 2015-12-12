package org.squirrelframework.cloud.resource.tenant;

import org.squirrelframework.cloud.annotation.UIProperty;
import org.squirrelframework.cloud.resource.CloudResourceConfig;

/**
 * Created by kailianghe on 9/10/15.
 */
public class TenantConfig extends CloudResourceConfig {

    private Long id;

    private String name;

    private String code;

    private String country;

    private String company;

    private String domain;

    private String localDomain;

    private String searchEngineCollection;
    
    private boolean enabled;

    @UIProperty(label = "ID", order = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @UIProperty(label = "Name", order = 2)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @UIProperty(label = "Code", order = 3)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @UIProperty(label = "Country", order = 7)
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @UIProperty(label = "Company", order = 8)
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @UIProperty(label = "Domain", order = 4)
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @UIProperty(label = "Local Domain", order = 5)
    public String getLocalDomain() {
        return localDomain;
    }

    public void setLocalDomain(String localDomain) {
        this.localDomain = localDomain;
    }

    @UIProperty(label = "Search Engine Collection", order = 6)
    public String getSearchEngineCollection() {
        return searchEngineCollection;
    }

    public void setSearchEngineCollection(String searchEngineCollection) {
        this.searchEngineCollection = searchEngineCollection;
    }

    @UIProperty(label = "Enable This Tenant", order = 9)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
