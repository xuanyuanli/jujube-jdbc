package org.jujubeframework.jdbc.spring;

import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.ClassMetadata;

import java.util.Arrays;
import java.util.Set;

/**
 * @author John Li
 * @see ClassPathBeanDefinitionScanner
 */
public class ClassPathDaoScanner extends ClassPathBeanDefinitionScanner {
    public ClassPathDaoScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void registerFilters() {
        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            ClassMetadata classMetadata = metadataReader.getClassMetadata();
            return classMetadata.getClassName().equals(BaseDaoSupport.class.getName());
        });
        addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            ClassMetadata classMetadata = metadataReader.getClassMetadata();
            //必须是接口，且继承自BaseDao
            return classMetadata.isInterface() && Arrays.stream(classMetadata.getInterfaceNames()).anyMatch(t -> BaseDao.class.getName().equals(t));
        });
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            logger.warn("No Dao sql was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            for (BeanDefinitionHolder holder : beanDefinitions) {
                GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
                if (logger.isDebugEnabled()) {
                    logger.info("Creating DaoFactoryBean with name '" + holder.getBeanName() + "' and '" + definition.getBeanClassName() + "' daoInterface");
                }
                // the dao interface is the original class of the bean
                // but, the actual class of the bean is DaoFactoryBean
                definition.getPropertyValues().add("daoInterfaceClass", definition.getBeanClassName());
                definition.setBeanClass(DaoFactoryBean.class);
            }
        }
        return beanDefinitions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            logger.warn("Skipping DaoFactoryBean with name '" + beanName + "' and '" + beanDefinition.getBeanClassName() + "' daoInterface" + ". Bean already defined with the same name!");
            return false;
        }
    }
}
