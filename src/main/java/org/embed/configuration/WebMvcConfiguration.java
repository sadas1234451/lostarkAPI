package org.embed.configuration;

import org.embed.interceptor.LoggerInterceptor;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// TODO Auto-generated method stub
		registry.addInterceptor(new LoggerInterceptor());
	}
	//공지 작성탭 
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/page/notice/write").setViewName("page/notice_write");
    }
	
	
	
	@Bean
	public MultipartResolver multipartResolver() {
		
		return new StandardServletMultipartResolver();
	}
	
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setLocation("C:\\myProject\\lostarkAPI");
		factory.setMaxRequestSize(DataSize.ofMegabytes(100L));
		factory.setMaxFileSize(DataSize.ofMegabytes(100L));
		
		return factory.createMultipartConfig();
	}
	
	@Bean
	public HiddenHttpMethodFilter httpMethodFilter() {
		
		HiddenHttpMethodFilter hiddenHttpMethodFilter = new HiddenHttpMethodFilter();
		
		return hiddenHttpMethodFilter;
	}
	// @Bean
    // public InternalResourceViewResolver viewResolver() {
    //     InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    //     resolver.setPrefix("/WEB-INF/views/");
    //     resolver.setSuffix(".jsp");
    //     return resolver;
    // }

}









