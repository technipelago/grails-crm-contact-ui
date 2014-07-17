<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmContact.label', default: 'Contact')}"/>
    <title><g:message code="crmContact.create.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="crmContact.create.title" args="[entityName]"/>

<h2><g:message code="crmContact.create.type.message" args="[entityName]"/></h2>

<div class="row-fluid">
    <div class="span6">
        <dl>
            <dt><g:message code="crmCompany.label" default="Company"/></dt>
            <dd><g:message code="crmContact.select.create.company.message" default="A company is an organisation that can (but are not forced to) have associated contacts/employees"/></dd>
            <dt><g:message code="crmContact.label" default="Contact"/></dt>
            <dd><g:message code="crmContact.select.create.contact.message" default="A contact is employed by or associated in som other way with a company"/></dd>
            <dt><g:message code="crmIndividual.label" default="Individual"/></dt>
            <dd><g:message code="crmContact.select.create.person.message" default="An individual is a person not associated with a company"/></dd>
        </dl>
    </div>

    <div class="span6">
    </div>
</div>

<g:form>

    <div class="form-actions">
        <crm:button type="link" action="company" icon="icon-home icon-white" visual="success"
                    label="crmContact.button.create.company.label" params="${linkParams}"/>
        <crm:button type="link" action="contact" icon="icon-user icon-white" visual="success"
                    label="crmContact.button.create.contact.label" params="${linkParams}"/>
        <crm:button type="link" action="person" icon="icon-user icon-white" visual="success"
                    label="crmContact.button.create.person.label" params="${linkParams}"/>
    </div>

</g:form>

</body>
</html>
