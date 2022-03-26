package com.codegym.config;

import com.codegym.formatter.ProvinceFormatter;
import com.codegym.service.provice.ProvinceService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.format.FormatterRegistry;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan ("com.codegym")
@EnableWebMvc
@EnableTransactionManagement //annoutation sử dụng để quản lý transaction
@EnableSpringDataWebSupport //Để hỗ trợ phân trang
@EnableJpaRepositories ("com.codegym.repository")//Để quét các repository
@PropertySource("classpath:upload-file.properties")// cấu hình đường dẫn upload file

public class AppConfig implements WebMvcConfigurer, ApplicationContextAware {
    ApplicationContext applicationContext;
    @Value("${file-upload}")
    String uploadPath;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    //1. Cấu hình Thymleaf
    @Bean
    public SpringResourceTemplateResolver templateResolver (){
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }
    @Bean
    public SpringTemplateEngine templateEngine(){
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver viewResolver (){
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCharacterEncoding("UTF-8");
        return viewResolver;
    }
      //2. Cấu hình Uploadfile

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //Override phương thức add resource handler để cấu hình đường dẫn file tĩnh
        //=> để lưu trữ các file ảnh, mp3, mp4 khi được upload lên server
        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:" + uploadPath);//Để khai báo cấu hình lữu trữ trên server
    }

    @Bean
    public CommonsMultipartResolver multipartResolver (){
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSizePerFile(-1);
        return multipartResolver;
    }

    //3. Cấu hình JPA,Hibernate
    @Bean  //Bean để khởi tạo entity manager factory
    public EntityManager entityManager (EntityManagerFactory entityManagerFactory){
        return entityManagerFactory.createEntityManager();
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory (){
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(datasource());
        em.setPackagesToScan("com.codegym.model");
        //VenderAdapter sử dụng để cấu hình liên quan đến hibernate
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionProperties());
        return em;
    }

    @Bean
    public DataSource datasource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/customer_management_module4_lesson7");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        return dataSource;
    }
    @Bean //Bean để khai báo quản lý transaction => JPA tự quản lý
    public PlatformTransactionManager transactionManager (EntityManagerFactory entityManagerFactory){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
    //Phương thức để khai báo các cấu hình liên quan hibernate
    public Properties additionProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        return properties;
    }
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new ProvinceFormatter(applicationContext.getBean(ProvinceService.class)));
    }

}
