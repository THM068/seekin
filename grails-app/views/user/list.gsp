
<%@ page import="me.hcl.seekin.Auth.User" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="user.list" /></title>
        <gui:resources components="dataTable"/>
        <g:javascript src="datatable.js" />
        <g:YUIButtonRessource />
    </head>
    <body>
      <h2><g:message code="user.list" /></h2>
      <g:if test="${flash.message}">
      <div class="flash_message"><g:message code="${flash.message}" args="${flash.args}" default="${flash.defaultMessage}" /></div>
      </g:if>
      <div class="yui-skin-sam" id="crud_panel">
          <g:buildListButtons />
          
                        <g:set var="idInternationalized" value="${message(code:'user.id')}" />
                 
                        <g:set var="emailInternationalized" value="${message(code:'user.email')}" />
                 
                        <g:set var="passwordInternationalized" value="${message(code:'user.password')}" />
                 
                        <g:set var="enabledInternationalized" value="${message(code:'user.enabled')}" />
                 
                        <g:set var="profileInternationalized" value="${message(code:'user.profile')}" />
                 
                        <g:set var="showEmailInternationalized" value="${message(code:'user.showEmail')}" />
                 
          <gui:dataTable
              id="dt_2"
              draggableColumns="true"
              columnDefs="[
                  
                            [key: 'id', sortable: true, resizeable: true, label: idInternationalized],
                     
                            [key: 'email', sortable: true, resizeable: true, label: emailInternationalized],
                     
                            [key: 'password', sortable: true, resizeable: true, label: passwordInternationalized],
                     
                            [key: 'enabled', sortable: true, resizeable: true, label: enabledInternationalized],
                     
                            [key: 'profile', sortable: true, resizeable: true, label: profileInternationalized, formatter: 'customLinkFormatter'],
                     
                            [key: 'showEmail', sortable: true, resizeable: true, label: showEmailInternationalized],
                     
                  [key: 'urlID', sortable: false, resizeable: false, label:'Actions', formatter: 'adminPanelFormatter']
              ]"
              controller="user"
              action="dataTableDataAsJSON"
              paginatorConfig="[
                  template:'{PreviousPageLink} {PageLinks} {NextPageLink} {CurrentPageReport}',
                  pageReportTemplate:'{totalRecords} ' + message(code:'list.total.records')
              ]"
              rowExpansion="false"
              rowsPerPage="10"
          />

        </div>
    </body>
</html>
