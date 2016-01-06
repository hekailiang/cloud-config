package org.squirrelframework.cloud.resource.security;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.cloud.resource.AbstractResourceFactoryBean;
import org.squirrelframework.cloud.resource.CloudResourceConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by kailianghe on 15/12/19.
 */
public abstract class AbstractCipherCoderFactoryBean<T> extends AbstractResourceFactoryBean<T, CipherCodecConfig> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCipherCoderFactoryBean.class);

    @Override
    protected Class<? extends CloudResourceConfig> getConfigType() {
        return CipherCodecConfig.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(isAutoReload()) {
            logger.warn("CipherCodec cannot be auto reloaded.");
        }
        super.afterPropertiesSet();
    }

    @Override
    protected T createInstance() throws Exception {
        Key key = null;
        if(config instanceof KeyStoreConfig) {
            KeyStoreConfig ksConfig = (KeyStoreConfig)config;
            KeyStore store = KeyStore.getInstance(ksConfig.getType());
            File keyStoreFile = new File(ksConfig.getKeyStoreLocation());
            if(!keyStoreFile.exists()) {
                throw new IllegalArgumentException("cannot find any .keystore file at \""+ksConfig.getKeyStoreLocation()+"\".");
            }
            try {
                InputStream input = new FileInputStream(keyStoreFile);
                store.load(input, (ksConfig.getKeyStorePassword() != null) ? ksConfig.getKeyStorePassword().toCharArray() : null);
            } catch (IOException e) {
                throw new IllegalStateException("load .keystore file failed", e.getCause());
            }
            key = store.getKey(ksConfig.getKeyAlias(), (ksConfig.getKeyPassword() != null) ? ksConfig.getKeyPassword().toCharArray() : null);
        } else if (config instanceof RSAPublicKeyConfig) {
            RSAPublicKeyConfig rsaPublicKeyConfig = (RSAPublicKeyConfig) config;
            byte[] keyBytes = Base64.decodeBase64( rsaPublicKeyConfig.getPublicKey() );
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            key = keyFactory.generatePublic(x509EncodedKeySpec);
        } else if(config instanceof RSAPrivateKeyConfig) {
            RSAPrivateKeyConfig rsaPrivateKeyConfig = (RSAPrivateKeyConfig) config;
            byte[] keyBytes = Base64.decodeBase64( rsaPrivateKeyConfig.getPrivateKey() );
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            key = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        }
        return createCoder(key);
    }

    abstract protected T createCoder(Key key);
}
