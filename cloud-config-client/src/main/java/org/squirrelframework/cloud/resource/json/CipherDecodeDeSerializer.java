package org.squirrelframework.cloud.resource.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.squirrelframework.cloud.resource.codec.Decoder;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.io.IOException;

/**
 * Created by kailianghe on 15/12/19.
 */
public class CipherDecodeDeSerializer extends JsonDeserializer<String> implements InitializingBean, ApplicationContextAware {

    private Decoder cipherDecoder;

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        if( CloudConfigCommon.isEncryptionEnabled() ) {
            cipherDecoder = applicationContext.getBean(CloudConfigCommon.ZK_DEFAULT_CIPHER_DECODER_BEAN_ID, Decoder.class);
        }
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String result = p.getText();
        if( CloudConfigCommon.isEncryptionEnabled() ) {
            try {
                result = cipherDecoder.decode(result, "UTF-8");
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e.getCause());
            }
        }
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
