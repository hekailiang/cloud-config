package org.squirrelframework.cloud.resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.squirrelframework.cloud.utils.BeanIdGenerator;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import javax.sql.DataSource;
import javax.validation.Validator;

/**
 * Created by kailianghe on 15/12/19.
 */
public abstract class AbstractResourceFactoryBean<R, T extends CloudResourceConfig> extends AbstractFactoryBean<R>
        implements CloudResourceFactoryBean<R, T>, ApplicationContextAware, BeanFactoryAware {

    protected T config;

    protected String configPath;

    protected ApplicationContext applicationContext;

    protected Validator validator;

    private DefaultListableBeanFactory beanFactory;

    private boolean autoReload = false;

    protected DefaultListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(configPath!=null && config==null) {
            DefaultListableBeanFactory beanFactory = getBeanFactory();
            // build config bean
            String configBeanId = BeanIdGenerator.getResourceConfigBeanId(configPath, getConfigType());
            if(!applicationContext.containsBeanDefinition(configBeanId)) {
                BeanDefinitionBuilder cnfBuilder = BeanDefinitionBuilder.rootBeanDefinition(SimpleResourceConfigFactoryBean.class);
                cnfBuilder.addPropertyReference("client", CloudConfigCommon.ZK_CLIENT_BEAN_NAME);
                cnfBuilder.addPropertyValue("path", configPath);
                cnfBuilder.addPropertyValue("resourceType", getConfigType().getName());
                cnfBuilder.addPropertyValue("validator", validator);
                cnfBuilder.addPropertyValue("autoReload", autoReload);
                cnfBuilder.setLazyInit(true);
                beanFactory.registerBeanDefinition(configBeanId, cnfBuilder.getBeanDefinition());
            }
            config = (T) applicationContext.getBean(configBeanId);
        }
        super.afterPropertiesSet();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setConfig(T config) {
        this.config = config;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    protected abstract Class<? extends CloudResourceConfig> getConfigType();

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    protected DelegatingDataSource createProxyDataSource(DataSource targetDataSource) {
        return new DelegatingDataSource(targetDataSource) {
            @Override
            public String toString() {
                if(getTargetDataSource()!=null) {
                    return getTargetDataSource().toString();
                } else {
                    return "_NULL_DATA_SOURCE_";
                }
            }
        };
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public void setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
    }
}
