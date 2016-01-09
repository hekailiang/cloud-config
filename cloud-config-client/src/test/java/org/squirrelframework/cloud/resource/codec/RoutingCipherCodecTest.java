package org.squirrelframework.cloud.resource.codec;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.CustomRoutingKeyResolver;
import org.squirrelframework.cloud.resource.security.CipherCodec;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by kailianghe on 15/12/20.
 */
public class RoutingCipherCodecTest extends AbstractCipherCodecTest {

    private File keyStoreFile1;

    private File keyStoreFile2;

    private ApplicationContext applicationContext;

    @Override
    protected void prepare() throws Exception {
        keyStoreFile1 = createKeyStoreFileForTest(KEY1, "test1", KEY_PASSWORD, STORE_PASSWORD);
        keyStoreFile2 = createKeyStoreFileForTest(KEY2, "test2", KEY_PASSWORD, STORE_PASSWORD);
        String ccConfig1 = "{\n" +
                "    \"__type__\" : \"org.squirrelframework.cloud.resource.security.keystore.KeyStoreConfig\", \n"+
                "    \"keyStoreLocation\" : \""+keyStoreFile1.getAbsolutePath()+"\",\n" +
                "    \"keyStorePassword\" : \""+STORE_PASSWORD+"\",\n" +
                "    \"keyAlias\" : \"test1\", \n"+
                "    \"keyPassword\" : \""+KEY_PASSWORD+"\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/codec/mycipher/tenant1", ccConfig1.getBytes());

        String ccConfig2 = "{\n" +
                "    \"__type__\" : \"org.squirrelframework.cloud.resource.security.keystore.KeyStoreConfig\", \n"+
                "    \"keyStoreLocation\" : \""+keyStoreFile2.getAbsolutePath()+"\",\n" +
                "    \"keyStorePassword\" : \""+STORE_PASSWORD+"\",\n" +
                "    \"keyAlias\" : \"test2\", \n"+
                "    \"keyPassword\" : \""+KEY_PASSWORD+"\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/codec/mycipher/tenant2", ccConfig2.getBytes());

        applicationContext = new ClassPathXmlApplicationContext("classpath:routing-ciphercodec-context.xml");
    }

    @Override
    protected void finish() throws Exception {
        System.getProperties().remove(CloudConfigCommon.ENABLE_ENCRYPTION);
        if(keyStoreFile1.exists()) {
            keyStoreFile1.delete();
        }
        if(keyStoreFile2.exists()) {
            keyStoreFile2.delete();
        }
        super.finish();
    }

    @Test
    public void testRoutingCipherCodec() throws Exception {
        String random = RandomStringUtils.randomAlphabetic(10);
        System.out.println("random: "+random);

        CipherCodec cc = applicationContext.getBean("zk-default-cipher-codec", CipherCodec.class);
        CustomRoutingKeyResolver resolver = applicationContext.getBean("tenantResolver", CustomRoutingKeyResolver.class);

        resolver.key = "tenant1";
        String encrypted1 = cc.encode(random, "UTF-8");
        System.out.println("encrypted: "+encrypted1);
        String decrypted1 = cc.decode(encrypted1, "UTF-8");
        System.out.println("decrypted: "+decrypted1);
        assertThat(random, is(decrypted1));

        resolver.key = "tenant2";
        String encrypted2 = cc.encode(random, "UTF-8");
        System.out.println("encrypted: "+encrypted2);
        String decrypted2 = cc.decode(encrypted2, "UTF-8");
        System.out.println("decrypted: "+decrypted2);
        assertThat(random, is(decrypted2));

        assertThat(encrypted1, not(encrypted2));
    }
}
