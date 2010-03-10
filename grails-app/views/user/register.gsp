<head>
	<meta name='layout' content='main' />
	<title><g:message code="login.page.title" /></title>
	<gui:resources components="accordion, autoComplete"/>
        <g:YUIButtonRessource />
</head>

<body>
        <g:form name="registerForm" action="register" >
              <g:hiddenField name="usertype" value="${usertype}" />
              <div id="register_form" class="yui-skin-sam">
                    <gui:expandablePanel title="${message(code:'register.form.global')}" expanded="true" bounce="false">
                            <div class="boxed_form">
                                    <g:if test="${flash.message}">
                                        <div class="flash_message">${flash.message}</div>
                                    </g:if>
                                    <g:hasErrors bean="${userInstance}">
                                        <div class="flash_message">
                                                <g:renderErrors bean="${userInstance}" />
                                        </div>
                                    </g:hasErrors>
                                    <p>
                                            <label for="email"><g:message code="user.enteryourmail" /></label>
                                            <g:textField name="email" value="${userInstance?.email?.encodeAsHTML()}" class="field${hasErrors(bean:userInstance,field:'email','error')}"/>
                                    </p>
                                    <p>
                                            <label for='password'><g:message code="user.password" /></label>
                                            <g:passwordField name='password' class="field${hasErrors(bean:userInstance,field:'password','error')}"/>
                                    </p>
                                    <p>
                                            <label for='password'><g:message code="user.repassword" /></label>
                                            <g:passwordField name='repassword' />
                                    </p>
                            </div>
                    </gui:expandablePanel>
              </div>
              <div id="captcha_form" class="yui-skin-sam">
                      <gui:expandablePanel title="${message(code:'register.form.validation')}" expanded="true" bounce="false">
                            <div class="boxed_form">
                                    <p>
                                            <label for='captcha'><g:message code="register.form.captcha" /></label>
                                            <g:textField name="captcha" />
											<img src="${createLink(action:'generateCaptcha')}" alt="Captcha" /><br />
                                    </p>
									<p class="submit">
									        <g:buildSubmitButton value="register.form.submit" action="register"/>
									</p>
                            </div>
                    </gui:expandablePanel>

              </div>
              <div id="profile_form" class="yui-skin-sam">
                      <gui:expandablePanel title="${message(code:'register.form.profile')}" expanded="true" bounce="false">
                            <div class="boxed_form">
                                    <p>
                                            <label for="firstName"><g:message code="user.firstName" /></label>
                                            <g:textField name="firstName" value="${userInstance?.firstName?.encodeAsHTML()}" class="field${hasErrors(bean:userInstance,field:'firstName','error')}"/>
                                    </p>
                                    <p>
                                            <label for="lastName"><g:message code="user.lastName" /></label>
                                            <g:textField name="lastName" value="${userInstance?.lastName?.encodeAsHTML()}" class="field${hasErrors(bean:userInstance,field:'lastName','error')}"/>
                                    </p>
                                    <p>
                                            <label for="address.street"><g:message code="address.street" /></label>
                                            <g:textField name="address.street" value="${userInstance?.address?.street?.encodeAsHTML()}" class="field${hasErrors(bean:userInstance,field:'address.street','error')}"/>
                                    </p>
                                    <p>
                                            <label for="address.zipCode"><g:message code="address.zipcode" /></label>
                                            <g:textField name="address.zipCode" value="${userInstance?.address?.zipCode?.encodeAsHTML()}" class="field${hasErrors(bean:userInstance,field:'address.zipCode','error')}"/>
                                    </p>
                                    <p>
                                            <label for="address.town"><g:message code="address.city" /></label>
                                            <g:textField name="address.town" value="${userInstance?.address?.town?.encodeAsHTML()}" class="field${hasErrors(bean:userInstance,field:'address.town','error')}"/>
                                    </p>
                                    <p>
                                            <label for="phone"><g:message code="user.phone" /></label>
                                            <g:textField name="phone" value="${userInstance?.phone?.encodeAsHTML()}" class="field${hasErrors(bean:userInstance,field:'phone','error')}"/>
                                    </p>
                                    <p>
                                      <label for="showEmail"><g:message code="user.showEmail" default="Show Email" /></label>
                                      <g:checkBox name="showEmail" value="${user?.showEmail}" />
                                    </p>
                                    <g:if test="${usertype == '3'}">
                                    <div>
                                        <label for="company"><g:message code="external.company" /></label>
                                        <gui:autoComplete
                                          id="company"
                                          controller="company"
                                          action="listCompanyAsJSON"
                                          minQueryLength='2'
                                          queryDelay='0.5'
                                          value="${company}"

                                        />
                                    </div>
                                    <p>
                                      <label for="formerStudent"><g:message code="external.formerstudent" /></label>
                                      <g:checkBox name="formerStudent" value="${profile?.formerStudent}" />
                                    </p>
                                    </g:if>
                                    <g:if test="${usertype == '1'}">
                                          <p>
                                            <label for="formation"><g:message code="student.formation" /></label>
                                            <g:select name="promotion" from="${formations}"  optionKey="id" optionValue="value" value="${params.promotion}"/>
                                          </p>
                                          <p>
                                            <label for="visible"><g:message code="student.visible" /></label>
                                            <g:checkBox name="visible" value="${profile?.visible}" />
                                          </p>
                                    </g:if>
                            </div>
                    </gui:expandablePanel>
                </div>
        </g:form>
</body>
