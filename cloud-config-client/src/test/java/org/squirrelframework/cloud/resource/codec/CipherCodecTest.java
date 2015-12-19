package org.squirrelframework.cloud.resource.codec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/19.
 */
public class CipherCodecTest extends BaseTestClass {

    private static final String KEY = "x4zOovfg7+Y=";
    private static final String ALGORITHM = "DES";
    private static final String STORE_PASSWORD = "password_store";
    private static final String KEY_PASSWORD = "password_key";

    File keyStoreFile;

    ApplicationContext applicationContext;

    protected void prepare() throws Exception {
        keyStoreFile = File.createTempFile("CipherCodecTest", "keystore");
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        Key key = factory.generateSecret(new DESKeySpec(Base64.decodeBase64(KEY.getBytes())));
        KeyStore store = KeyStore.getInstance("jceks");
        store.load(null, null);
        store.setKeyEntry("test", key, KEY_PASSWORD.toCharArray(), null);

        try (FileOutputStream out = new FileOutputStream(keyStoreFile)) {
            store.store(out, STORE_PASSWORD.toCharArray());
        }

        String config = "{\n" +
                "    \"keyStoreLocation\" : \""+keyStoreFile.getAbsolutePath()+"\",\n" +
                "    \"keystorePassword\" : \""+STORE_PASSWORD+"\",\n" +
                "    \"keyAlias\" : \"test\", \n"+
                "    \"keyPassword\" : \""+KEY_PASSWORD+"\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/codec/mycipher", config.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:ciphercodec-context.xml");
    }

    @Test
    public void testCipherCodec() throws Exception {
        CipherCodec cc = applicationContext.getBean("cipher", CipherCodec.class);

        String random = RandomStringUtils.randomAlphabetic(10);
        System.out.println("random: "+random);
        String encrypted = cc.encode(random);
        System.out.println("encrypted: "+encrypted);
        String decrypted = cc.decode(encrypted);
        System.out.println("decrypted: "+decrypted);
        assertThat(random, is(decrypted));
    }

    protected void finish() throws Exception {
        if(keyStoreFile.exists()) {
            keyStoreFile.delete();
        }
        super.finish();
    }
}
