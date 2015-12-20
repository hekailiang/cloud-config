package org.squirrelframework.cloud.resource.codec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.squirrelframework.cloud.BaseTestClass;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;

/**
 * Created by kailianghe on 15/12/20.
 */
public class AbstractCipherCodecTest extends BaseTestClass {

    protected static final String KEY1 = "x4zOovfg7+Y=";

    protected static final String KEY2 = "squirrel1234";

    protected static final String STORE_PASSWORD = "password_store";

    protected static final String KEY_PASSWORD = "password_key";

    protected File createKeyStoreFileForTest(String keystr, String alias, String keyPassword, String storePassword) throws Exception {
        String prefix = RandomStringUtils.randomAlphabetic(10);
        File keyStoreFile = File.createTempFile(prefix, "keystore");
        SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
        Key key = factory.generateSecret(new DESKeySpec(Base64.decodeBase64(keystr.getBytes())));
        KeyStore store = KeyStore.getInstance("jceks");
        store.load(null, null);
        store.setKeyEntry(alias, key, keyPassword.toCharArray(), null);

        try (FileOutputStream out = new FileOutputStream(keyStoreFile)) {
            store.store(out, storePassword.toCharArray());
        }
        return keyStoreFile;
    }
}
