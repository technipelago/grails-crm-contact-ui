<%@ page import="grails.plugins.crm.contact.CrmContact" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmContact.label', default: 'CrmContact')}"/>
    <title><g:message code="crmContact.deleteAll.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="crmContact.deleteAll.title" subtitle="crmContact.deleteAll.count"
            args="[entityName, crmContactTotal]"/>

<div class="alert alert-danger" style="margin-top: 50px; margin-bottom: 50px;">
    <p class="lead">
        <g:message code="crmContact.button.deleteAll.confirm" default="Are you sure you want to delete all selected contacts?"/>
    </p>
</div>

<g:form action="deleteAll">

    <div class="form-actions">
        <input type="hidden" name="q" value="${select.encode(selection: selection)}"/>

        <crm:button action="deleteAll" visual="danger" icon="icon-trash icon-white"
                    label="crmContact.button.deleteAll.label"
                    confirm="crmContact.button.deleteAll.confirm" permission="crmContact:delete"/>

    </div>
</g:form>

</body>
</html>
