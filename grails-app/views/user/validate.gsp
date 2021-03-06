
<%@ page import="me.hcl.seekin.Auth.User" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="user.validate" /></title>
        
        <tm:resources />
    </head>
    <body>
      <h2><g:message code="user.validate" /></h2>
      <g:if test="${flash.message}">
      <div class="flash_message"><g:message code="${flash.message}" transparent="true" args="${flash.args}" default="${flash.defaultMessage}" /></div>
      </g:if>
      <g:form class="yui-skin-sam" action="validate">
          <g:buildListButtons />
                        <g:set var="idInternationalized" value="${message(code:'user.id')}" />
                        <g:set var="showEmailInternationalized" value="${message(code:'user.showEmail')}" />
                        <g:set var="roleInternationalized" value="${message(code:'user.role')}" />
                        <g:set var="firstNameInternationalized" value="${message(code:'user.firstName')}" />
                        <g:set var="lastNameInternationalized" value="${message(code:'user.lastName')}" />
          <gui:dataTable
              id="dt_2"
              draggableColumns="true"
              columnDefs="[
                            [key: 'id', sortable: true, resizeable: true, label: idInternationalized],
                            [key: 'firstName', sortable: true, resizeable: true, label: firstNameInternationalized],
                            [key: 'lastName', sortable: true, resizeable: true, label: lastNameInternationalized],
                            [key: 'roles', sortable: false, resizeable: true, label: roleInternationalized],
                            [key: 'urlID', sortable: false, resizeable: false, label:'Actions', formatter: 'validatePanelFormatter']
              ]"
              controller="user"
              action="dataTableDataAsJSON"
              paginatorConfig="[
                  template:'{PreviousPageLink} {PageLinks} {NextPageLink} {CurrentPageReport}',
                  pageReportTemplate:'{totalRecords} ' + message(code:'list.total.records')
              ]"
              rowExpansion="false"
              rowsPerPage="10"
              params="[enabled:false, validated:false]"
          />
          <g:YUISubmitbutton action="validate" value="validate" />
        </g:form>
    </body>
</html>
