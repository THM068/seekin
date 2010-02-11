security {

	// see DefaultSecurityConfig.groovy for all settable/overridable properties

	active = true

	loginUserDomainClass = "me.hcl.seekin.Auth.User"
	authorityDomainClass = "me.hcl.seekin.Auth.Role.Role"
	requestMapClass = "me.hcl.seekin.Auth.RequestMap"
	
	useRequestMapDomainClass = false
	
	useControllerAnnotations = true 
	
	defaultRole = "ROLE_ROLE"
	
	authenticationFailureUrl = "/user/authFail?login_error=1"
	loginFormUrl = "/user/auth" 
	errorPage = "/user/accessDenied"
	userName = "email"
	password = "password"
	
}
