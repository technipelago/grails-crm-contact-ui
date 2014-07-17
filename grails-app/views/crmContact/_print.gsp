<g:applyLayout name="print">
<html>
<head>
    <g:set var="entityName" value="${message(code: 'crmContact.label', default: 'Contact')}"/>
    <title><g:message code="crmContact.list.title" args="[entityName]"/></title>
</head>

<body>

<crm:printedBy/>

<rendering:inlineJpeg bytes="${logo}" height="32" />

<h1 style="text-align: center;margin-top:0;text-transform: uppercase;"><g:message code="crmContact.list.title" default="Contacts"/></h1>

<table width="100%">
    <thead>
    <tr>
        <th style="white-space: nowrap;">${message(code: 'crmContact.name.label', default: 'Name')}</th>
        <th style="white-space: nowrap;">${message(code: 'crmContact.parent.label', default: 'Relation')}</th>
        <th style="white-space: nowrap;">${message(code: 'crmContact.telephone.label', default: 'Telephone')}</th>
        <th style="white-space: nowrap;">${message(code: 'crmContact.address.label', default: 'Address')}</th>
        <th style="white-space: nowrap;">${message(code: 'crmContact.username.label', default: 'Responsible')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${crmContactList}" var="crmContact" status="i">
        <tr style="${(i % 2) ? '' : 'background-color:#f3f3f3;'}">
            <td>${fieldValue(bean: crmContact, field: "name")}</td>
            <td>${fieldValue(bean: crmContact, field: "parent")}</td>
            <td style="white-space: nowrap;">${fieldValue(bean: crmContact, field: "preferredPhone")}</td>
            <td>${fieldValue(bean: crmContact, field: "address")}</td>
            <td><crm:user username="${crmContact.username}">${name}</crm:user></td>
        </tr>
    </g:each>
    </tbody>
</table>

</body>
</html>
</g:applyLayout>