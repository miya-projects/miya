package com.miya.websocket.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author 杨超辉
 * @date 2018/8/15
 * @description
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractSessionWebSocketMessageBrokerConfigurer<Session> {

    /**
     * 接入第三方服务
     * https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/web.html#websocket-stomp-message-flow
     * @return
     */
    // private ReactorNettyTcpClient<byte[]> createTcpClient() {
    //     return new ReactorNettyTcpClient<>(
    //             client -> client,
    //             new StompReactorNettyCodec());
    // }
    @Resource
    private AuthChannelInterceptorAdapter authChannelInterceptorAdapter;
    @Resource
    private TaskScheduler messageBrokerTaskScheduler;

    @Bean
    public SessionRepository<MapSession> sessionRepository(){
        return new MapSessionRepository(new HashMap<>());
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // config.enableStompBrokerRelay("/topic").setTcpClient(createTcpClient());
        config.enableSimpleBroker("/topic")
                //服务端10s一次心跳 客户端20s一次心跳
                .setHeartbeatValue(new long[]{10000, 20000})
                .setTaskScheduler(this.messageBrokerTaskScheduler);
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    protected void configureStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptorAdapter);
        // registration.interceptors(  new ChannelInterceptor() {
        //     @Override
        //     public Message<?> preSend(Message<?> message, MessageChannel channel) {
        //         StompHeaderAccessor accessor =
        //                 MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        //         assert accessor != null;
        //         Object authToken = accessor.getMessageHeaders().get("simpUser");
        //         if (StompCommand.CONNECT.equals(accessor.getCommand())) {
        //             Authentication user = null ;
        //             accessor.setUser(user);
        //         }
        //         return message;
        //     }
        // });
    }

    // @Bean
    // public ClientRegistrationRepository clientRegistrationRepository() {
    //     return new ClientRegistrationRepository() {
    //         @Override
    //         public ClientRegistration findByRegistrationId(String registrationId) {
    //
    //             return ClientRegistration.withRegistrationId(registrationId).build();
    //         }
    //     };
    // }


    // @Bean
    // public OAuth2AuthorizedClientService authorizedClientService(
    //         ClientRegistrationRepository clientRegistrationRepository) {
    //     return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    // }
    //
    // @Bean
    // public OAuth2AuthorizedClientRepository authorizedClientRepository(
    //         OAuth2AuthorizedClientService authorizedClientService) {
    //     return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    // }

    // @Bean
    // public ClientRegistrationRepository clientRegistrationRepository() {
    //     return new InMemoryClientRegistrationRepository(this.googleClientRegistration());
    // }
    // private ClientRegistration googleClientRegistration() {
    //     return CommonOAuth2Provider.GOOGLE.getBuilder("google")
    //             .clientId("google-client-id")
    //             .clientSecret("google-client-secret")
    //             .build();
    // }

}

// @Configuration
// class WebSecurityConfig extends WebSecurityConfigurerAdapter{
//     @Override
//     protected void configure(HttpSecurity http) throws Exception {
//         http.authorizeRequests()
//                 .regexMatchers("/")
//                 .permitAll();
//     }
// }


// @Configuration
class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        // messages
        //         .nullDestMatcher().authenticated()
        //         .simpSubscribeDestMatchers("/user/queue/errors").permitAll()
        //         .simpDestMatchers("/app/**").hasRole("USER")
        //         .simpSubscribeDestMatchers("/user/**", "/topic/friends/*").hasRole("USER")
        //         .simpTypeMatchers(MESSAGE, SUBSCRIBE).denyAll()
        //         .anyMessage().denyAll();

        // Any message without a destination (i.e. anything other than Message type of MESSAGE or SUBSCRIBE) will require the user to be authenticated
        // Anyone can subscribe to /user/queue/errors
        // Any message that has a destination starting with "/app/" will be require the user to have the role ROLE_USER
        // Any message that starts with "/user/" or "/topic/friends/" that is of type SUBSCRIBE will require ROLE_USER
        // Any other message of type MESSAGE or SUBSCRIBE is rejected. Due to 6 we do not need this step, but it illustrates how one can match on specific message types.
        // Any other Message is rejected. This is a good idea to ensure that you do not miss any messages.
    }
}
