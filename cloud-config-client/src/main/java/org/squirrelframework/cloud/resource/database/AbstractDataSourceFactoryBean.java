package org.squirrelframework.cloud.resource.database;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.squirrelframework.cloud.resource.CloudResourceConfig;
import org.squirrelframework.cloud.resource.CloudResourceFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.squirrelframework.cloud.resource.SimpleResourceConfigFactoryBean;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import javax.sql.DataSource;
import javax.validation.Validator;

/**
 * Created by kailianghe on 9/11/15.
 */
public abstract class AbstractDataSourceFactoryBean<T extends CloudResourceConfig> extends AbstractFactoryBean<DataSource>
        implements CloudResourceFactoryBean<DataSource, T>, ApplicationContextAware, BeanFactoryAware {

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

    public static String getResourceConfigBeanIdFromPath(String configPath) {
        return "_"+configPath.replace('/', '_')+"CNF";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(configPath!=null && config==null) {
            DefaultListableBeanFactory beanFactory = getBeanFactory();
            // build config bean
            String configBeanId = getResourceConfigBeanIdFromPath(configPath);
            if(!applicationContext.containsBeanDefinition(configBeanId)) {
                BeanDefinitionBuilder cnfBuilder = BeanDefinitionBuilder.rootBeanDefinition(SimpleResourceConfigFactoryBean.class);
                cnfBuilder.addPropertyReference("client", CloudConfigCommon.ZK_CLIENT_BEAN_NAME);
                cnfBuilder.addPropertyValue("path", configPath);
                cnfBuilder.addPropertyValue("resourceType", baseDataSourceConfigType());
                cnfBuilder.addPropertyValue("validator", validator);
                cnfBuilder.addPropertyValue("autoReload", autoReload);
                cnfBuilder.setLazyInit(true);
                beanFactory.registerBeanDefinition(configBeanId, cnfBuilder.getBeanDefinition());
            }
            config = (T) applicationContext.getBean(configBeanId);
        }
        super.afterPropertiesSet();
    }

    /**
     * @return abstract bean id matched with cloud-config.xml
     */
    abstract protected String baseDataSourceConfigType();

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
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
