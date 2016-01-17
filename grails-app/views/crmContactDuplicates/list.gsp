<%@ page import="grails.plugins.crm.contact.CrmContact" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmContact.label', default: 'CrmContact')}"/>
    <title><g:message code="crmContact.duplicates.title" args="[entityName]"/></title>
    <style type="text/css">
    table.crm-list td:first-child, th:first-child {
        width: 16px;
    }
    </style>
</head>

<body>

<crm:header title="crmContact.duplicates.title" subtitle="crmContact.totalCount.label"
            args="[entityName, crmContactTotal]"/>

<g:form controller="${params.matchController}" action="${params.matchAction}">

    <input type="hidden" name="id" value="${params.matchId}"/>
    <input type="hidden" name="referer" value="${params.referer}"/>

    <table class="table table-striped crm-list">
        <thead>
        <tr>
            <th></th>
            <th></th>
            <crm:sortableColumn property="name"
                                title="${message(code: 'crmContact.name.label', default: 'Name')}"/>

            <th><g:message code="crmContact.address.label" default="Address"/></th>

            <th><g:message code="crmContact.telephone.label" default="Telephone"/></th>

            <th><g:message code="crmContact.email.label" default="Email"/></th>

            <crm:sortableColumn property="title"
                                title="${message(code: 'crmContact.title.label', default: 'Title')}"/>
            <crm:sortableColumn property="number"
                                title="${message(code: 'crmContact.number.label', default: '#')}"/>
        </tr>
        </thead>
        <tbody>
        <g:each in="${crmContactList}" status="i" var="crmContact">
            <g:set var="parentContact" value="${crmContact.primaryRelation}"/>
            <g:set var="preferredPhone" value="${crmContact.preferredPhone}"/>
            <tr>
                <td>
                    <input type="radio" name="selected" value="${crmContact.id}"/>
                </td>
                <td>
                    <g:if test="${crmContact.person}">
                        <i class="icon-user"></i>
                    </g:if>
                </td>
                <td>
                    <select:link action="show" id="${crmContact.id}" selection="${selection}">
                        ${fieldValue(bean: crmContact, field: "name")}<g:if
                            test="${parentContact}">, ${parentContact.name.encodeAsHTML()}</g:if>
                    </select:link>
                </td>

                <td>${fieldValue(bean: crmContact, field: "address")}</td>

                <td>
                    <g:if test="${preferredPhone}">
                        <a href="tel:${crmContact.telephone}">${preferredPhone.encodeAsHTML()}</a>
                    </g:if>
                </td>

                <td>
                    <g:if test="${crmContact.email}">
                        <a href="mailto:${crmContact.email}"><g:decorate include="abbreviate" max="30"><g:fieldValue
                            bean="${crmContact}" field="email"/></g:decorate>
                    </g:if>
                </td>

                <td>${fieldValue(bean: crmContact, field: "title")}</td>

                <td>${fieldValue(bean: crmContact, field: "number")}</td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="form-actions btn-toolbar">
        <crm:button controller="${params.matchController}" action="${params.matchAction}"
                    label="crmContact.duplicates.match.label" visual="primary"
                    confirm="crmContact.duplicates.match.confirm"/>
        <g:if test="${params.referer}">
            <g:link class="btn" url="${params.referer}">
                <i class="icon-remove"></i>
                <g:message code="crmContact.button.back.label" default="Back"/>
            </g:link>
        </g:if>
    </div>
</g:form>

</body>
</html>
