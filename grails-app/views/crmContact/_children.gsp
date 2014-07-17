<table class="table table-striped">
    <thead>
    <tr>

        <crm:sortableColumn params="${params}" property="name" prefix="c"
                          title="${message(code: 'crmContact.name.label', default: 'Name')}"/>

        <crm:sortableColumn params="${params}" property="title" prefix="c"
                          title="${message(code: 'crmContact.title.label', default: 'Title')}"/>

        <th><g:message code="crmContact.address.label" default="Address"/></th>

        <crm:sortableColumn params="${params}" property="telephone" prefix="c"
                                  title="${message(code: 'crmContact.telephone.label', default: 'Telephone')}"/>

        <crm:sortableColumn params="${params}" property="email" prefix="c"
                                  title="${message(code: 'crmContact.email.label', default: 'Email')}"/>
    </tr>
    </thead>
    <tbody>
    <g:each in="${result}" status="i" var="child">
        <tr>

            <td><g:link action="show"
                        id="${child.id}">${fieldValue(bean: child, field: "name")}</g:link></td>

            <td>${fieldValue(bean: child, field: "title")}</td>

            <td>${fieldValue(bean: child, field: "address")}</td>

            <td><a href="tel:${fieldValue(bean: child, field: "telephone").replaceAll(/\W/, '')}">${fieldValue(bean: child, field: "telephone")}</a></td>

            <td><a href="mailto:${fieldValue(bean: child, field: "email")}"><g:decorate max="20">${fieldValue(bean: child, field: "email")}</g:decorate></a></td>

        </tr>
    </g:each>
    </tbody>
</table>
