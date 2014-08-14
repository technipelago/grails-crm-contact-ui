<%@ page import="org.apache.commons.lang.StringUtils" %>
<table id="relations-list" class="table table-striped">
    <thead>
    <tr>
        <th><g:message code="crmContact.name.label" default="Name"/></th>
        <th><g:message code="crmContact.address.label" default="Address"/></th>
        <th><g:message code="crmContact.telephone.label" default="Telephone"/></th>
        <th><g:message code="crmContact.email.label" default="Email"/></th>
        <th><g:message code="crmContactRelation.type.label" default="Type"/></th>
        <!--
        <th style="width:40px;"></th>
        -->
    </tr>
    </thead>
    <tbody>

    <g:if test="${bean.parent}">
        <tr>
            <td><g:link action="show"
                        id="${bean.parent.id}">${fieldValue(bean: bean.parent, field: "name")}</g:link></td>

            <td>${fieldValue(bean: bean.parent, field: "address")}</td>

            <td><a href="tel:${fieldValue(bean: bean.parent, field: "telephone").replaceAll(/\W/, '')}">${fieldValue(bean: bean.parent, field: "telephone")}</a></td>

            <td><a href="mailto:${fieldValue(bean: bean.parent, field: "email")}"><g:decorate max="20">${fieldValue(bean: bean.parent, field: "email")}</g:decorate></a></td>

            <td><g:message code="crmContact.parent.label"/></td>
            </tr>

    </g:if>

    <g:each in="${children}" status="i" var="child">
        <tr>
            <td><g:link action="show"
                        id="${child.id}">${fieldValue(bean: child, field: "name")}</g:link></td>

            <td>${fieldValue(bean: child, field: "address")}</td>

            <td><a href="tel:${fieldValue(bean: child, field: "telephone").replaceAll(/\W/, '')}">${fieldValue(bean: child, field: "telephone")}</a></td>

            <td><a href="mailto:${fieldValue(bean: child, field: "email")}"><g:decorate max="20">${fieldValue(bean: child, field: "email")}</g:decorate></a></td>

            <td><g:message code="crmContact.child.label"/></td>
        </tr>
    </g:each>

    <g:each in="${relations}" status="i" var="relation">
        <g:set var="related" value="${relation.getRelated(bean)}"/>
        <tr>
            <td>
                <g:link mapping="crm-contact-show" id="${related.id}">
                    ${fieldValue(bean: related, field: "name")}
                </g:link>
                <g:if test="${relation.isPrimaryFor(bean)}">
                    <i class="icon-star-empty"></i>
                </g:if>
            </td>

            <td>${fieldValue(bean: related, field: "address")}</td>

            <td><a href="tel:${fieldValue(bean: related, field: "telephone").replaceAll(/\W/, '')}">${fieldValue(bean: related, field: "telephone")}</a>
            </td>

            <td><a href="mailto:${fieldValue(bean: related, field: "email")}"><g:decorate
                    max="20">${fieldValue(bean: related, field: "email")}</g:decorate></a></td>

            <td>
                <a class="crm-edit" data-crm-id="${relation.id}" href="#" title="Click to edit relation details">
                    ${fieldValue(bean: relation, field: "type")}
                </a>
                <g:if test="${relation.description}">
                    <i class="icon-comment" title="${StringUtils.abbreviate(relation.description, 80).encodeAsHTML()}"></i>
                </g:if>
            </td>
        <!--
            <td style="width:40px;">
                <a class="crm-edit" data-crm-id="${relation.id}" href="#"><i class="icon-pencil"></i></a>
                <a class="crm-delete" data-crm-id="${relation.id}" href="#"><i class="icon-trash"></i></a>
            </td>
        -->
        </tr>
    </g:each>
    </tbody>
</table>
