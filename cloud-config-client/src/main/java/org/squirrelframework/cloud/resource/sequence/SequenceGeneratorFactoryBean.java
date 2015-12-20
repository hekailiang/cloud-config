package org.squirrelframework.cloud.resource.sequence;

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.squirrelframework.cloud.resource.AbstractRoutingResourceFactoryBean;
import org.squirrelframework.cloud.routing.NestedRoutingKeyResolver;
import org.squirrelframework.cloud.utils.BeanIdGenerator;

/**
 * Created by kailianghe on 15/12/15.
 */
public class SequenceGeneratorFactoryBean extends AbstractRoutingResourceFactoryBean<SequenceGenerator> {

    private static final Logger logger = LoggerFactory.getLogger(SequenceGeneratorFactoryBean.class);

    private SequenceFormatter sequenceFormatter;

    private String sequenceFormatExpression;

    @Override
    public void afterPropertiesSet() throws Exception {
        if(sequenceFormatter == null && StringUtils.hasText(sequenceFormatExpression)) {
            sequenceFormatter = SpringELSequenceFormatterFactory.createFormatter(sequenceFormatExpression);
        }
        super.afterPropertiesSet();
    }

    @Override
    protected String getResourceBeanIdFromPath(String resPath) {
        return BeanIdGenerator.getSequenceGeneratorBeanId(resPath);
    }

    @Override
    public Class<?> getObjectType() {
        return SequenceGenerator.class;
    }

    @Override
    protected SequenceGenerator createInstance() throws Exception {
        if(resolver!=null) {
            // build routing supported sequence generator
            createChildResourceBeanDefinition();
            return new RoutingSequenceGenerator();
        } else {
            // build simple sequence generator
            String seqBeanId = getResourceBeanIdFromPath(path);
            buildGeneratorBeanDefinition(path, seqBeanId, false);
            return applicationContext.getBean(seqBeanId, SequenceGenerator.class);
        }
    }

    private void buildGeneratorBeanDefinition(String resPath, String seqBeanId, boolean lazy) {
        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder.rootBeanDefinition(JdbcSequenceDao.class);
        String dataSourceId = BeanIdGenerator.getDataSourceBeanId(resPath);
        daoBuilder.addPropertyReference("dataSource", dataSourceId);
        daoBuilder.setLazyInit(lazy);
        String daoBeanId = BeanIdGenerator.getSequenceDaoBeanId(resPath);
        getBeanFactory().registerBeanDefinition(daoBeanId, daoBuilder.getBeanDefinition());

        BeanDefinitionBuilder seqBuilder = BeanDefinitionBuilder.rootBeanDefinition(JdbcSequenceGenerator.class);
        seqBuilder.addPropertyValue("sequenceFormatter", sequenceFormatter);
        seqBuilder.addPropertyReference("sequenceDao", daoBeanId);
        seqBuilder.setLazyInit(lazy);
        getBeanFactory().registerBeanDefinition(seqBeanId, seqBuilder.getBeanDefinition());
    }

    @Override
    protected void buildResourceBeanDefinition(String resPath, String seqBeanId) throws Exception {
        if(getBeanFactory().containsBeanDefinition(seqBeanId)) {
            return;
        }

        if( isNestedRoutingNeeded(resPath) ) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(SequenceGeneratorFactoryBean.class);
            builder.addPropertyValue("path", resPath);
            builder.addPropertyValue("client", this.client);
            builder.addPropertyValue("resolver", ((NestedRoutingKeyResolver)this.resolver).next());
            builder.addPropertyValue("sequenceFormatter", sequenceFormatter);
            builder.setLazyInit(true);
            getBeanFactory().registerBeanDefinition(seqBeanId, builder.getBeanDefinition());
        } else {
            buildGeneratorBeanDefinition(resPath, seqBeanId, true);
        }
    }

    @Override
    public void setResourceFactoryBeanClass(Class<?> resourceFactoryBeanClass) {
        throw new UnsupportedOperationException("no simple resource factory bean is defined for sequence");
    }

    @Override
    protected void removeResourceBeanDefinition(String resPath, String beanId) throws Exception {
        throw new UnsupportedOperationException("cannot reload sequence");
    }

    @Override
    public boolean isAutoReload() {
        return false;
    }

    public void setSequenceFormatter(SequenceFormatter sequenceFormatter) {
        this.sequenceFormatter = sequenceFormatter;
    }

    public void setSequenceFormatExpression(String sequenceFormatExpression) {
        this.sequenceFormatExpression = sequenceFormatExpression;
    }

    public class RoutingSequenceGenerator implements SequenceGenerator {
        @Override
        public String next(String seqName) throws Exception {
            String routingKey = resolver.get().orNull();
            if( routingKey!=null ) {
                logger.debug("Routing sequence generator lookup key is '{}'", routingKey);
            } else {
                logger.warn("Routing sequence generator lookup key cannot be found in current context!");
                routingKey = "__absent_tenant__";
            }
            try {
                return localResourceStore.getUnchecked(routingKey).next(seqName);
            } catch (UncheckedExecutionException e) {
                Throwable cause = e.getCause();
                throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + routingKey + "]", cause);
            }
        }
    }
}
