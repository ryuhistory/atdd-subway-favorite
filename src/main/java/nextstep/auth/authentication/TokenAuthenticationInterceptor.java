package nextstep.auth.authentication;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import nextstep.auth.context.Authentication;
import nextstep.auth.service.CustomUserDetails;
import nextstep.auth.token.JwtTokenProvider;
import nextstep.auth.token.TokenRequest;
import nextstep.auth.token.TokenResponse;

public class TokenAuthenticationInterceptor extends AuthenticationNonChainInterceptor {

	private JwtTokenProvider jwtTokenProvider;

	public TokenAuthenticationInterceptor(CustomUserDetails customUserDetails, JwtTokenProvider jwtTokenProvider) {
		super(customUserDetails);
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		try {
			AuthenticationToken authenticationToken = convert(request);
			Authentication authentication = authenticate(authenticationToken);
			afterAuthenticate(authentication, response);
			return false;
		} catch (Exception e) {
			return true;
		}

	}

	private AuthenticationToken convert(HttpServletRequest request) throws IOException {
		String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		TokenRequest tokenRequest = new ObjectMapper().readValue(content, TokenRequest.class);
		return new AuthenticationToken(tokenRequest.getEmail(), tokenRequest.getPassword());
	}

	private void afterAuthenticate(Authentication authentication, HttpServletResponse response) throws IOException {

		String token = jwtTokenProvider.createToken((String)authentication.getPrincipal(),
			authentication.getAuthorities());

		String responseToClient = new ObjectMapper().writeValueAsString(new TokenResponse(token));
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getOutputStream().print(responseToClient);

	}

}