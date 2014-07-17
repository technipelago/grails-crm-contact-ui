<%@ page import="grails.plugins.crm.contact.CrmContact" contentType="text/html;charset=UTF-8" defaultCodec="html" %>

<g:if test="${companyList}">

    <p>
        <g:message code="crmContact.change.parent.body.2"
                   default="Select one of the following companies or use the standard search form to find the new employer"
                   args="${[crmContact]}"/>
    </p>

    <ul class="nav nav-list">
        <li class="nav-header"><g:message code="crmContact.change.parent.list.title"/></li>

        <g:each in="${companyList}" var="c">
            <li>
                <label class="radio"><input type="radio" name="parent.id" value="${c.id}"/>
                    <g:link action="show" id="${c.id}">${c}</g:link> <span class="muted">${c?.address}</span></label>
            </li>
        </g:each>
    </ul>
</g:if>

<g:else>
    <p>
        <g:message code="crmContact.change.parent.body.1"
                   default="Use the standard search page to find the new employer, or create a company if it doesn't exist. Then click [Confirm new employer] button to change employer for ${0}"
                   args="${[crmContact]}"/>
    </p>
</g:else>