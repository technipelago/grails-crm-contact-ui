<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmContactRelationType.label', default: 'Relation Type')}"/>
    <title><g:message code="crmContactRelationType.edit.title" args="[entityName, crmContactRelationType]"/></title>
</head>

<body>

<crm:header title="crmContactRelationType.edit.title" args="[entityName, crmContactRelationType]"/>

<div class="row-fluid">
    <div class="span9">

        <g:hasErrors bean="${crmContactRelationType}">
            <crm:alert class="alert-error">
                <ul>
                    <g:eachError bean="${crmContactRelationType}" var="error">
                        <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                                error="${error}"/></li>
                    </g:eachError>
                </ul>
            </crm:alert>
        </g:hasErrors>

        <g:form class="form-horizontal" action="edit"
                id="${crmContactRelationType?.id}">
            <g:hiddenField name="version" value="${crmContactRelationType?.version}"/>

            <f:with bean="crmContactRelationType">
                <f:field property="name" input-autofocus=""/>
                <f:field property="description"/>
                <f:field property="param"/>
                <f:field property="icon"/>
                <f:field property="orderIndex"/>
                <f:field property="enabled"/>
            </f:with>

            <div class="form-actions">
                <crm:button visual="primary" icon="icon-ok icon-white" label="crmContactRelationType.button.update.label"/>
                <crm:button action="delete" visual="danger" icon="icon-trash icon-white"
                            label="crmContactRelationType.button.delete.label"
                            confirm="crmContactRelationType.button.delete.confirm.message"
                            permission="crmContactRelationType:delete"/>
                <crm:button type="link" action="list"
                            icon="icon-remove"
                            label="crmContactRelationType.button.cancel.label"/>
            </div>
        </g:form>
    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>
