package shop.mtcoding.blog._core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;



@Configuration // 컴포넌트 스캔
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder(); // IoC 등록, 시큐리티가 로그인할 때 어떤 해시로 비교해야하는지 알게 됨
    }

//    @Bean
//    public WebSecurityCustomizer ignore(){ // 정적파일 security filter에서 제외시키기
//        // 필터링 하지 마라
//         return w -> w.ignoring().requestMatchers("/board/*", "/static/**", "/h2-console/**");
//    }

    @Bean
    SecurityFilterChain config(HttpSecurity http) throws Exception {

        http.csrf(c -> c.disable());

        // 인증 필요 url
        http.authorizeHttpRequests(a -> {
            a.requestMatchers(RegexRequestMatcher.regexMatcher("/board/\\d+")).permitAll() // 순서 유의
                    .requestMatchers("/user/**", "/board/**").authenticated()
                    .anyRequest().permitAll();

        });

        http.formLogin(f -> {
            f.loginPage("/loginForm").loginProcessingUrl("/login").defaultSuccessUrl("/").failureUrl("/loginForm");
        });

        return http.build();

    }
}
