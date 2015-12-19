package org.squirrelframework.cloud.resource.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.squirrelframework.cloud.resource.codec.CipherCodec;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.io.IOException;

/**
 * Created by kailianghe on 15/12/19.
 */
public class CipherEncodeSerializer extends JsonSerializer<String> implements InitializingBean, ApplicationContextAware {

    private CipherCodec cipherCodec;

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        if( CloudConfigCommon.isEncryptionEnabled() ) {
            cipherCodec = applicationContext.getBean("zk-default-cipher-codec", CipherCodec.class);
        }
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if( CloudConfigCommon.isEncryptionEnabled() ) {
            try {
                gen.writeString(cipherCodec.encode(value));
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e.getCause());
            }
        } else {
            gen.writeString(value);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
