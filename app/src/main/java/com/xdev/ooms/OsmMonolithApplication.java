package com.xdev.ooms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

@SpringBootApplication(
        scanBasePackages = "com.xdev.ooms",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
@EntityScan(basePackages = "com.xdev.ooms")
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = "com.xdev.ooms",
        repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class,
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
        bootstrapMode = BootstrapMode.LAZY
)
public class OsmMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(OsmMonolithApplication.class, args);
    }
}
