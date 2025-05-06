<%@ page import="grails.plugins.crm.contact.CrmContact" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmContact.label', default: 'CrmContact')}"/>
    <title><g:message code="crmContact.list.title" args="[entityName]"/></title>
    <r:script>
        $(document).ready(function () {
            $.getJSON("${createLink(action: 'filter', params: [q: selection])}", function(data) {
                var $menu = $('#selection-menu');
                for(i = 0; i < data.length; i++) {
                    var item = data[i];
                    var $a = $('<a/>');
                    var $li = $('<li/>');
                    $a.text(item.name);
                    $a.attr('href', item.url + '&q=' + "${select.encode(selection: selection)}&referer=${request.forwardURI.encodeAsIsoURL()}");
                    $li.append($a);
                    $menu.append($li);
                }
            });
        });
    </r:script>
    <style type="text/css">
    table.crm-list td:first-child, th:first-child {
        width: 16px;
    }
    </style>
</head>

<body>

<crm:header title="crmContact.list.title" subtitle="crmContact.totalCount.label"
            args="[entityName, crmContactTotal]"/>

<table class="table table-striped crm-list">
    <thead>
    <tr>
        <th></th>
        <crm:sortableColumn property="name"
                            title="${message(code: 'crmContact.name.label', default: 'Name')}"/>

        <crm:sortableColumn property="title"
                            title="${message(code: 'crmContact.title.label', default: 'Title')}"/>

        <th><g:message code="crmContactRelation.type.label" default="Relation"/></th>

        <th class="nowrap"><g:message code="crmContact.telephone.label" default="Telephone"/></th>

        <th><g:message code="crmAddress.city.label" default="City"/></th>

        <th><g:message code="crmContact.tags.label" default="Tags"/></th>
        <crm:sortableColumn property="number"
                            title="${message(code: 'crmContact.number.label', default: '#')}"/>
    </tr>
    </thead>
    <tbody>
    <g:each in="${crmContactList}" status="i" var="crmContact">
        <g:set var="parentContact" value="${crmContact.primaryRelation}"/>
        <g:set var="preferredPhone" value="${crmContact.preferredPhone}"/>
        <tr>
            <td style="width:16px;padding-right:0;">
                <i class="icon-${crmContact.person ? 'user' : 'home'}"></i>
            </td>
            <td>
                <select:link action="show" id="${crmContact.id}" selection="${selection}">
                    ${fieldValue(bean: crmContact, field: "name")}<g:if
                        test="${parentContact}">, ${parentContact.name}</g:if>
                </select:link>
            </td>

            <td>${fieldValue(bean: crmContact, field: "title")}</td>

            <td>${parentContact?.relation?.name}</td>

            <td class="nowrap">
                <g:if test="${preferredPhone}">
                    <a href="tel:${preferredPhone}">${preferredPhone}</a>
                </g:if>
            </td>

            <td>${fieldValue(bean: crmContact.address, field: "city")}</td>

            <td><crm:tags bean="${crmContact}"/></td>

            <td>${fieldValue(bean: crmContact, field: "number")}</td>
        </tr>
    </g:each>
    </tbody>
</table>

<crm:paginate total="${crmContactTotal}"/>

<g:form>

    <div class="form-actions">
        <input type="hidden" name="offset" value="${params.offset ?: ''}"/>
        <input type="hidden" name="max" value="${params.max ?: ''}"/>
        <input type="hidden" name="sort" value="${params.sort ?: ''}"/>
        <input type="hidden" name="order" value="${params.order ?: ''}"/>

        <g:each in="${selection.selectionMap}" var="entry">
            <input type="hidden" name="${entry.key}" value="${entry.value}"/>
        </g:each>

        <crm:selectionMenu visual="primary"/>

        <g:if test="${crmContactTotal}">
            <div class="btn-group">
                <select:link action="export" accesskey="p" selection="${selection}" class="btn btn-info">
                    <i class="icon-print icon-white"></i>
                    <g:message code="crmContact.button.export.label" default="Print/Export"/>
                </select:link>
            </div>

            <g:if test="${functions}">
                <div class="btn-group">
                    <a class="btn btn-warning dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="icon-play-circle icon-white"></i>
                        <g:message code="crmContact.selection.process.label" default="Process"/>
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <g:each in="${functions}" var="f">
                            <li>
                                <select:link controller="${f.controller ?: controllerName}" action="${f.action ?: 'index'}"
                                             selection="${selection}" params="${[entityName: CrmContact.name, totalCount: crmContactTotal]}" title="${message(code: f.description)}">
                                    <g:message code="${f.name}"/>
                                </select:link>
                            </li>
                        </g:each>
                    </ul>
                </div>

            </g:if>
        </g:if>

        <crm:hasPermission permission="crmContact:create">
            <crm:button type="link" group="true" action="create" visual="success"
                        icon="icon-file icon-white"
                        label="crmContact.button.create.label" permission="crmContact:create">
                <button class="btn btn-success dropdown-toggle" data-toggle="dropdown">
                    <span class="caret"></span>
                </button>
                <ul class="dropdown-menu">
                    <li>
                        <g:link controller="crmContact" action="company"><g:message
                                code="crmContact.button.create.company.label" default="Company"/></g:link>
                    </li>
                    <li>
                        <g:link controller="crmContact" action="contact"><g:message
                                code="crmContact.button.create.contact.label" default="Contact"/></g:link>
                    </li>
                    <li>
                        <g:link controller="crmContact" action="person"><g:message
                                code="crmContact.button.create.person.label" default="Individual"/></g:link>
                    </li>
                </ul>
            </crm:button>
        </crm:hasPermission>

        <g:if test="${crmContactTotal}">
            <shiro:hasRole name="admin">
                <select:link action="deleteAll" selection="${selection}" style="margin-left: 12px; color: #990000;">
                    <g:message code="crmContact.button.deleteAll.label" default="Delete all"/>
                </select:link>
            </shiro:hasRole>
        </g:if>

    </div>
</g:form>

</body>
</html>
