package com.taotao.miaosha.config;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
 
import com.taotao.miaosha.domain.MiaoshaUser;
import com.taotao.miaosha.service.MiaoshaUserService;;
 
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver{
	
	@Autowired
	private MiaoshaUserService miaoshaUserService;
 
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == MiaoshaUser.class;
	}
 
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
		
		String paramToken = request.getParameter(MiaoshaUserService.COOKIE_TOKEN_NAME);
		String cookieToken = getCookieValue(request, MiaoshaUserService.COOKIE_TOKEN_NAME);
		
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
			return null;
		}
		
		String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
		return miaoshaUserService.getByToken(response, token);
	}
 
	private String getCookieValue(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if(cookies != null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(cookieName)){
					return cookie.getValue();
				}
			}
		}
		return null;
	}
 
}
