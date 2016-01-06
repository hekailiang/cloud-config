package org.squirrelframework.cloud.resource.codec;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.squirrelframework.cloud.BaseTestClass;
import org.squirrelframework.cloud.resource.security.CipherCodec;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 16/1/6.
 */
public class RSACodecTest extends BaseTestClass {

    private static final String test_private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIBKm1PQh7Trf3fWSUhW" +
            "YOxSv5YHcE68/uE1U3X9K23p1AbFfb+mSG6005ksft/fbIUom6yi0/vFBSWI9gJq9x8PKWNt6dgiQZbOa1JlUEJFH5MzELvu70k8LnlvlhA" +
            "0uxaCJvlV89IpXjNBiNuJ0fpzOjAfHbAyme7XVFQu2FARAgMBAAECgYAtLJg3UjWHAmnXI9CYNTpZ3OVzidkFEM3bNXDXGjfBs1WisMYPxx" +
            "i4Sto7lIx0fnLzDJKMnqVwgTbHucuOnGW5vTuHYTuUIK2eBpTX0+Dd/lvqeSVOGOOp8FDGaGPZUG90K6cMVKxSXdWOkzvboeaEXfZ6o4HNM" +
            "mXAierVo0TzQQJBAMCAM6BZ2kqNkcVgxHt5kOvlmFBSLyJD5CPr6cO9c7ldWOY0Ru3KI+UG9gxeVWWXMYrRTEn8upLr8+LooAdAnykCQQCq" +
            "nDkUYe2fEEwHu9IYjk94SelvbpY6BM8MDKl2G3wkTCasL6p0we+PdE9hfXhVQqWgN1zh+yJ17M1l9NrnAw6pAkEAksCpmvMlEXT/zkNJRwC" +
            "UVOJBzen5eNvdDu2I1uqVUJkzbrwn3pwd5Tn7Vc6Mt9gdssCAsl7zNZKf6TqC1USUkQJAFJYtPQxkA+GPiw9a1qO1aypgBJYvgmi6IxYUDL" +
            "BcVnTp5bUKuHP+WC0MtxLQxlj4a1DC4HHRQrGER104XM+9oQJBAJmrdVckrTj4IQW792TAPsgmUeDmqIOfrou/mypBEmn8bhwEINLGvQq+L" +
            "wQqzd/nkFEX6dJSL461TJZY/8MyOOE=";
    private static final String test_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCASptT0Ie063931klIVmDsUr+WB3BO" +
            "vP7hNVN1/Stt6dQGxX2/pkhutNOZLH7f32yFKJusotP7xQUliPYCavcfDyljbenYIkGWzmtSZVBCRR+TMxC77u9JPC55b5YQNLsWgib5VfP" +
            "SKV4zQYjbidH6czowHx2wMpnu11RULthQEQIDAQAB";

    ApplicationContext applicationContext;

    @Override
    protected void prepare() throws Exception {
        String ccConfig1;
        ccConfig1 = "{\n" +
                "    \"__type__\" : \"org.squirrelframework.cloud.resource.security.RSAPrivateKeyConfig\", \n"+
                "    \"privateKey\" : \""+test_private_key+"\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/codec/rsa/private", ccConfig1.getBytes());

        String ccConfig2 = "{\n" +
                "    \"__type__\" : \"org.squirrelframework.cloud.resource.security.RSAPublicKeyConfig\", \n"+
                "    \"publicKey\" : \""+test_public_key+"\"\n"+
                "}";
        zkConfigClient.create().creatingParentsIfNeeded().forPath("/codec/rsa/public", ccConfig2.getBytes());
        applicationContext = new ClassPathXmlApplicationContext("classpath:rsa-codec-context.xml");
    }

    @Test
    public void testRoundTrip() throws Exception {
        CipherCodec cc = applicationContext.getBean("zk-default-cipher-codec", CipherCodec.class);
        String encoded = cc.encode("hello world", "UTF-8");
        String decoded = cc.decode(encoded, "UTF-8");
        assertThat(decoded, is("hello world"));
    }

}
