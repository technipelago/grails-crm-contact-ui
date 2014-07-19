<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmContact.label', default: 'CrmContact')}"/>
    <title><g:message code="crmContact.export.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="crmContact.export.title" subtitle="crmContact.export.subtitle" args="[entityName]"/>

<g:each in="${layouts}" var="l">
    <g:form action="export" class="well">
        <input type="hidden" name="q" value="${select.encode(selection: selection)}"/>
        <input type="hidden" name="namespace" value="${l.namespace}"/>
        <input type="hidden" name="topic" value="${l.topic}"/>

        <div class="row-fluid">
            <div class="span7">
                <h3>${l.name?.encodeAsHTML()}</h3>

                <p class="lead">
                    ${l.description?.encodeAsHTML()}
                </p>

                <button type="submit" class="btn btn-info">
                    <i class="icon-ok icon-white"></i>
                    <g:message code="crmContact.button.select.label" default="Select"/>
                </button>
            </div>

            <div class="span5">
                <g:if test="${l.thumbnail}">
                    <img src="${l.thumbnail}" class="pull-right"/>
                </g:if>
            </div>
        </div>

    </g:form>
</g:each>

</body>
</html>