package pk.training.basit.configuration;

import static org.springframework.security.config.Customizer.withDefaults;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import pk.training.basit.service.UserPrincipalService;

@EnableGlobalMethodSecurity(
    prePostEnabled = true, 
    order = 0, 
    mode = AdviceMode.PROXY,
    proxyTargetClass = false
)
@EnableWebSecurity
public class SecurityConfiguration {

	private static final Logger LOGGER = LogManager.getLogger(SecurityConfiguration.class);

	@Autowired 
	private UserPrincipalService userPrincipalService;
	
	// If no passwordEncoder bean is defined then you have to prefix password like {noop}secret1, or {bcrypt}password
	/**
	 * spring-security-oauth2-authorization-server:0.2.0. If I am using the following bean with h2 database then for registered 
	 * client I am getting error password not look like bcrypt. I think it's a bug. Because JdbcRegisteredClient mapper is using
	 * password delegating encoder which prefix the client secret with prefix {bcrypt}. And when for token it tries to matches
	 * the password with the defined encoder which is bcrypt. Then there is an error password not look like bcrypt.
	 * 
	 * To reporduce issue. Uncomment the following bean. remove the the {bcrypt} prefix from database/scripts/test-data.sql and
	 * then try to get the token.
	 * 
	 * JdbcRegisteredClientRepository$RegisteredClientParametersMapper
	 */
	/**
	@Bean
    public PasswordEncoder passwordEncoder() {
		LOGGER.debug("in passwordEncoder");
        return new BCryptPasswordEncoder();
    };
    */
	
	@Autowired
	protected void configureGlobal(AuthenticationManagerBuilder builder) throws Exception {
		LOGGER.debug("in configureGlobal");
		 builder
             .userDetailsService(this.userPrincipalService)
                // .passwordEncoder(passwordEncoder())
         .and()
             .eraseCredentials(true);
	}
	
	@Bean
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		LOGGER.debug("in configure HttpSecurity");
		http.authorizeRequests(authorizeRequests -> authorizeRequests.requestMatchers(EndpointRequest.toAnyEndpoint(),PathRequest.toH2Console()).permitAll()
		    .anyRequest().authenticated()
		)
		.formLogin(withDefaults())
		.csrf().ignoringRequestMatchers(PathRequest.toH2Console())
		.and().headers().frameOptions().sameOrigin();
		
		return http.build();
	}
	
}
