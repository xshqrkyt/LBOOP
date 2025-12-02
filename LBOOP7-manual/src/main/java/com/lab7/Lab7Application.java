package com.lab7;

import com.lab7.servlet.BasicAuthServlet;
import com.lab7.servlet.CompositeFunctionLinkServlet;
import com.lab7.servlet.CompositeFunctionServlet;
import com.lab7.servlet.FunctionServlet;
import com.lab7.servlet.OperationServlet;
import com.lab7.servlet.PointsServlet;
import com.lab7.servlet.UserServlet;
import com.lab7.util.DatabaseInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Lab7Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Lab7Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Lab7Application.class);
    }

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> DatabaseInitializer.ensureReady();
    }

    @Bean
    public ServletRegistrationBean<UserServlet> userServlet() {
        ServletRegistrationBean<UserServlet> bean = new ServletRegistrationBean<>(new UserServlet(), "/users");
        bean.setName("UserServlet");
        return bean;
    }

    @Bean
    public ServletRegistrationBean<FunctionServlet> functionServlet() {
        ServletRegistrationBean<FunctionServlet> bean = new ServletRegistrationBean<>(new FunctionServlet(), "/functions");
        bean.setName("FunctionServlet");
        return bean;
    }

    @Bean
    public ServletRegistrationBean<PointsServlet> pointsServlet() {
        ServletRegistrationBean<PointsServlet> bean = new ServletRegistrationBean<>(new PointsServlet(), "/points");
        bean.setName("PointsServlet");
        return bean;
    }

    @Bean
    public ServletRegistrationBean<CompositeFunctionServlet> compositeFunctionServlet() {
        ServletRegistrationBean<CompositeFunctionServlet> bean = new ServletRegistrationBean<>(new CompositeFunctionServlet(), "/composite-functions");
        bean.setName("CompositeFunctionServlet");
        return bean;
    }

    @Bean
    public ServletRegistrationBean<CompositeFunctionLinkServlet> compositeFunctionLinkServlet() {
        ServletRegistrationBean<CompositeFunctionLinkServlet> bean = new ServletRegistrationBean<>(new CompositeFunctionLinkServlet(), "/composite-function-links");
        bean.setName("CompositeFunctionLinkServlet");
        return bean;
    }

    @Bean
    public ServletRegistrationBean<OperationServlet> operationServlet() {
        ServletRegistrationBean<OperationServlet> bean = new ServletRegistrationBean<>(new OperationServlet(), "/operations");
        bean.setName("OperationServlet");
        return bean;
    }

    @Bean
    public FilterRegistrationBean<BasicAuthServlet> basicAuthFilter() {
        FilterRegistrationBean<BasicAuthServlet> bean = new FilterRegistrationBean<>(new BasicAuthServlet());
        bean.addUrlPatterns("/*");
        bean.setName("BasicAuthServlet");
        bean.setOrder(1);
        return bean;
    }
}
