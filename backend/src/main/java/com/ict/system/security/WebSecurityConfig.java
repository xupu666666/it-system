package com.ict.system.security;

/*
// 已禁用：如需启用请确保不与 v2 配置冲突
// @Configuration
// @EnableWebSecurity
// @EnableGlobalMethodSecurity(
//         prePostEnabled = true,
//         securedEnabled = true,
//         jsr250Enabled = true)
// public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // @Autowired
    // private UserDetailsService userDetailsService;

    // @Autowired
    // @Qualifier("jwtAuthenticationFilterV2")
    // private JwtAuthenticationFilter jwtAuthenticationFilter;

    // @Autowired
    // private CorsFilter corsFilter;

    // @Override
    // protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    //     System.out.println("配置Spring Security认证管理器...");
    //     auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    // }

    // @Override
    // protected void configure(HttpSecurity http) throws Exception {
    //     System.out.println("配置Spring Security HTTP安全配置...");

    //     // 明确指定公共路径
    //     String[] publicPaths = new String[] {
    //         "/auth/**",
    //         "/auth/login",
    //         "/auth/register",
    //         "/test/**",
    //         "/**"  // 允许所有路径，仅用于测试
    //     };
    //     System.out.println("配置公共路径: " + Arrays.toString(publicPaths));

    //     http.cors().and().csrf().disable()
    //             .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
    //             .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    //             .and()
    //             .authorizeRequests()
    //             .antMatchers(publicPaths).permitAll()
    //             .anyRequest().authenticated();

    //     http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    //     System.out.println("Spring Security HTTP安全配置完成");
    // }

    // @Bean
    // @Override
    // public AuthenticationManager authenticationManagerBean() throws Exception {
    //     System.out.println("创建AuthenticationManager Bean...");
    //     return super.authenticationManagerBean();
    // }

    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     System.out.println("创建PasswordEncoder Bean...");
    //     return new BCryptPasswordEncoder();
    // }
*/