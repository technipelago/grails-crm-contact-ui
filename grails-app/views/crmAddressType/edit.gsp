<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmAddressType.label', default: 'Address Type')}"/>
    <title><g:message code="crmAddressType.edit.title" args="[entityName, crmAddressType]"/></title>
</head>

<body>

<crm:header title="crmAddressType.edit.title" args="[entityName, crmAddressType]"/>

<div class="row-fluid">
    <div class="span9">

        <g:hasErrors bean="${crmAddressType}">
            <crm:alert class="alert-error">
                <ul>
                    <g:eachError bean="${crmAddressType}" var="error">
                        <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                                error="${error}"/></li>
                    </g:eachError>
                </ul>
            </crm:alert>
        </g:hasErrors>

        <g:form class="form-horizontal" action="edit"
                id="${crmAddressType?.id}">
            <g:hiddenField name="version" value="${crmAddressType?.version}"/>

            <f:with bean="crmAddressType">
                <f:field property="name" input-autofocus=""/>
                <f:field property="description"/>
                <f:field property="param"/>
                <f:field property="icon"/>
                <f:field property="orderIndex"/>
                <f:field property="enabled"/>
            </f:with>

            <div class="form-actions">
                <crm:button visual="primary" icon="icon-ok icon-white" label="crmAddressType.button.update.label"/>
                <crm:button action="delete" visual="danger" icon="icon-trash icon-white"
                            label="crmAddressType.button.delete.label"
                            confirm="crmAddressType.button.delete.confirm.message"
                            permission="crmAddressType:delete"/>
                <crm:button type="link" action="list"
                            icon="icon-remove"
                            label="crmAddressType.button.cancel.label"/>
            </div>
        </g:form>
    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>
