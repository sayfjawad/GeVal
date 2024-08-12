package nl.rotterdam.service.geval;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.rotterdam.service.geval.service.RequestContextService;
import nl.rotterdam.service.geval.service.RequestContextServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication(scanBasePackages = {"nl.rotterdam.logme", "nl.rotterdam.service.geval"})
@ComponentScan(basePackages = { "nl.rotterdam" })
@PropertySource(value = { "classpath:version.properties" })
@IntegrationComponentScan
public class GevalApplication implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(GevalApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GevalApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // TBD
    }

    @Bean(name = "errmConfig")
    public PropertiesFactoryBean errormessages() {
        PropertiesFactoryBean result = new PropertiesFactoryBean();
        result.setLocation(new ClassPathResource("errormessages.properties"));
        return result;
    }

    @Bean
    public RequestContextService getRequestContextService() {
        return new RequestContextServiceImpl();
    }

    @Bean
    Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPaths(
                "nl.rotterdam.service.geval.api.v1.xml");
        return jaxb2Marshaller;
    }

    @Bean
    Unmarshaller unmarshaller() {
        return marshaller();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Bean
    public SimpleClientHttpRequestFactory requestFactory() {
        return new SimpleClientHttpRequestFactory();
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

}
