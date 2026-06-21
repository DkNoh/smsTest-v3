package com.example.sms.config;

import com.example.sms.service.system.scaffold.ScaffoldDialect;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sms.scaffold")
public class ScaffoldProperties {

    private String dbPlatform = "oracle";

    public String getDbPlatform() {
        return dbPlatform;
    }

    public void setDbPlatform(String dbPlatform) {
        this.dbPlatform = dbPlatform;
    }

    public ScaffoldDialect dialect() {
        return ScaffoldDialect.from(dbPlatform);
    }
}
