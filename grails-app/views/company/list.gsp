
<%@ page import="me.hcl.seekin.Internship.Company" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title><g:message code="company.list" /></title>
        
        <tm:resources />
    </head>
    <body>
      <h2><g:message code="company.list" /></h2>
      <g:if test="${flash.message}">
      <div class="flash_message"><g:message code="${flash.message}" transparent="true" args="${flash.args}" default="${flash.defaultMessage}" /></div>
      </g:if>
      <div class="yui-skin-sam" id="crud_panel">
                
                        <g:set var="nameInternationalized" value="${message(code:'company.name')}" />
                 
                        <g:set var="phoneInternationalized" value="${message(code:'company.phone')}" />
        <br /><br />
        <g:filterPanel id="dt_2" filters="[
                [name: nameInternationalized, field: 'name']
        ]" />
        <br />
          <gui:dataTable
              id="dt_2"
              draggableColumns="true"
              columnDefs="[
                     
                            [key: 'name', sortable: true, resizeable: true, label: nameInternationalized],
                                         
                  [key: 'urlID', sortable: false, resizeable: false, label:'Actions', formatter: 'adminPanelFormatter']
              ]"
              controller="company"
              sortedBy="name"
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
