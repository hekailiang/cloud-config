package org.squirrelframework.cloud.resource.codec;

import org.squirrelframework.cloud.resource.AbstractResourceFactoryBean;
import org.squirrelframework.cloud.resource.CloudResourceConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;

/**
 * Created by kailianghe on 15/12/19.
 */
public class CipherCodecFactoryBean extends AbstractResourceFactoryBean<CipherCodec, CipherCodecConfig> {

    @Override
    protected Class<? extends CloudResourceConfig> getConfigType() {
        return CipherCodecConfig.class;
    }

    @Override
    public Class<?> getObjectType() {
        return CipherCodec.class;
    }

    @Override
    protected CipherCodec createInstance() throws Exception {
        KeyStore store = KeyStore.getInstance(config.getType());
        File keyStoreFile = new File(config.getKeyStoreLocation());
        if(!keyStoreFile.exists()) {
            throw new IllegalArgumentException("cannot find any .keystore file at \""+config.getKeyStoreLocation()+"\".");
        }
        try {
            InputStream input = new FileInputStream(keyStoreFile);
            store.load(input, (config.getKeystorePassword() != null) ? config.getKeystorePassword().toCharArray() : null);
        } catch (IOException e) {
            throw new IllegalStateException("load .keystore file failed", e.getCause());
        }
        Key key = store.getKey(config.getKeyAlias(), (config.getKeyPassword() != null) ? config.getKeyPassword().toCharArray() : null);
        return new CipherCodec(key);
    }
}
