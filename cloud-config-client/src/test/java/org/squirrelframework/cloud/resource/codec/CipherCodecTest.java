package org.squirrelframework.cloud.resource.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.resource.database.BoneCPDataSourceConfig;
import org.squirrelframework.cloud.resource.json.SpringHandlerInstantiator;
import org.squirrelframework.cloud.resource.security.CipherCodec;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.io.File;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/19.
 */
public class CipherCodecTest extends AbstractCipherCodecTest {

    File keyStoreFile;

    ApplicationContext applicationContext;

    protected void prepare() throws Exception {
        keyStoreFile = createKeyStoreFileForTest(KEY1, "test", KEY_PASSWORD, STORE_PASSWORD);
        String ccConfig = "{\n" +
                "    \"keyStoreLocation\" : \""+keyStoreFile.getAbsolutePath()+"\",\n" +
                "    \"keyStorePassword\" : \""+STORE_PASSWORD+"\",\n" +
                "    \"keyAlias\" : \"test\", \n"+
                "    \"keyPassword\" : \""+KEY_PASSWORD+"\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/codec/mycipher", ccConfig.getBytes());

        String dsConfig = "{\n" +
                "    \"driverClassName\" : \"org.h2.Driver\",\n" +
                "    \"userName\" : \"hhe\",\n" +
                "    \"password\" : \"PmLd4EiUfP8=\", \n"+
                "    \"jdbcUrl\" : \"jdbc:h2:mem:cctv;MODE=MySQL;DB_CLOSE_DELAY=-1\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/database/mydb", dsConfig.getBytes());

        System.getProperties().put(CloudConfigCommon.ENABLE_ENCRYPTION, "true");
        applicationContext = new ClassPathXmlApplicationContext("classpath:ciphercodec-context.xml");
    }

    protected void finish() throws Exception {
        System.getProperties().remove(CloudConfigCommon.ENABLE_ENCRYPTION);
        if(keyStoreFile.exists()) {
            keyStoreFile.delete();
        }
        super.finish();
    }

    @Test
    public void testCipherCodec() throws Exception {
        CipherCodec cc = applicationContext.getBean("zk-default-cipher-codec", CipherCodec.class);

        String random = RandomStringUtils.randomAlphabetic(10);
        System.out.println("random: "+random);
        String encrypted = cc.encode(random, "UTF-8");
        System.out.println("encrypted: "+encrypted);
        String decrypted = cc.decode(encrypted, "UTF-8");
        System.out.println("decrypted: "+decrypted);
        assertThat(random, is(decrypted));
    }

    @Test
    public void testJsonSerialize() throws Exception {
        CipherCodec cc = applicationContext.getBean("zk-default-cipher-codec", CipherCodec.class);
        String random = RandomStringUtils.randomAlphabetic(10);
        String encrypted = cc.encode(random, "UTF-8");
        System.out.println("encrypted: "+encrypted);

        BoneCPDataSourceConfig config = new BoneCPDataSourceConfig();
        config.setJdbcUrl("jdbc:h2:mem:b;MODE=MySQL;DB_CLOSE_DELAY=-1");
        config.setUserName("hhe");
        config.setPassword(random);

        ObjectMapper om = new ObjectMapper();
        om.setHandlerInstantiator(new SpringHandlerInstantiator(applicationContext.getAutowireCapableBeanFactory()));

        String result = om.writeValueAsString(config);
        System.out.println(result);
        assertThat(result.indexOf(encrypted)>0, is(true));
        assertThat(result.indexOf(random), is(-1));
    }

    @Test
    public void testJsonDeSerialize() throws Exception {
        BoneCPDataSourceConfig dsConfig = applicationContext.getBean("dsconfig", BoneCPDataSourceConfig.class);
        assertThat(dsConfig.getPassword(), is("1234"));
    }
}
