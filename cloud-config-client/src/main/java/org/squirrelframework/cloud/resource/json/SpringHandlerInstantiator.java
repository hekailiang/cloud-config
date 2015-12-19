package org.squirrelframework.cloud.resource.json;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Eventually get Jackson handler ({@link JsonSerializer}, {@link JsonDeserializer},
 * {@link KeyDeserializer}, {@link TypeResolverBuilder}, {@link TypeIdResolver}) beans by
 * type from Spring {@link ApplicationContext}. If no bean is found, the default behavior
 * happen (calling no-argument constructor via reflection).
 *
 * @since 4.1.3
 * @author Sebastien Deleuze
 * @see HandlerInstantiator
 */
public class SpringHandlerInstantiator extends HandlerInstantiator {

    private final AutowireCapableBeanFactory beanFactory;


    /**
     * Create a new SpringHandlerInstantiator for the given BeanFactory.
     * @param beanFactory the target BeanFactory
     */
    public SpringHandlerInstantiator(AutowireCapableBeanFactory beanFactory) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        this.beanFactory = beanFactory;
    }

    @Override
    public JsonSerializer<?> serializerInstance(SerializationConfig config,
                                                Annotated annotated, Class<?> keyDeserClass) {
        return (JsonSerializer<?>) this.beanFactory.createBean(keyDeserClass);
    }

    @Override
    public JsonDeserializer<?> deserializerInstance(DeserializationConfig config,
                                                    Annotated annotated, Class<?> deserClass) {
        return (JsonDeserializer<?>) this.beanFactory.createBean(deserClass);
    }

    @Override
    public KeyDeserializer keyDeserializerInstance(DeserializationConfig config,
                                                   Annotated annotated, Class<?> serClass) {
        return (KeyDeserializer) this.beanFactory.createBean(serClass);
    }

    @Override
    public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config,
                                                              Annotated annotated, Class<?> resolverClass) {
        return (TypeResolverBuilder<?>) this.beanFactory.createBean(resolverClass);
    }

    @Override
    public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config,
                                                 Annotated annotated, Class<?> resolverClass) {
        return (TypeIdResolver) this.beanFactory.createBean(resolverClass);
    }
}