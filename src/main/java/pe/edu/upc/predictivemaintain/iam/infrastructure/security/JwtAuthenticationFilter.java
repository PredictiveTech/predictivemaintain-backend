package pe.edu.upc.predictivemaintain.iam.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upc.predictivemaintain.iam.domain.model.aggregates.UserAccount;
import pe.edu.upc.predictivemaintain.iam.domain.repositories.UserAccountRepository;
import pe.edu.upc.predictivemaintain.iam.interfaces.acl.AuthenticatedUser;

import java.io.IOException;
import java.util.List;

/**
 * Reads the "Authorization: Bearer ..." header, validates the token and loads the user.
 * The roles come from the database, not from the token, so a deactivated account or a role
 * change takes effect on the next request. If anything fails, the request stays anonymous
 * and the security configuration answers 401.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenAdapter jwtTokenAdapter;
    private final UserAccountRepository userAccountRepository;

    public JwtAuthenticationFilter(JwtTokenAdapter jwtTokenAdapter, UserAccountRepository userAccountRepository) {
        this.jwtTokenAdapter = jwtTokenAdapter;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            jwtTokenAdapter.parseUserId(header.substring(BEARER_PREFIX.length()).trim())
                    .flatMap(userAccountRepository::findById)
                    .filter(UserAccount::isActive)
                    .ifPresent(this::authenticate);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(UserAccount user) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getTenantId(), user.getRoles());
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }
}