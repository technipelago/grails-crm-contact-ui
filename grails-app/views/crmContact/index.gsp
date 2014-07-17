<%@ page import="grails.plugins.crm.contact.CrmContact" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmContact.label', default: 'Contact')}"/>
    <title><g:message code="crmContact.find.title" args="[entityName]"/></title>
    <r:require module="autocomplete"/>
    <r:script>
    $(document).ready(function() {
        $("input[name='title']").autocomplete("${createLink(action: 'autocompleteTitle', params: [max: 20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            preventDefaultReturn: true,
            selectFirst: true
        });
        $("input[name='category']").autocomplete("${createLink(action: 'autocompleteCategoryType', params: [max: 20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            preventDefaultReturn: true,
            selectFirst: true
        });
        $("input[name='tags']").autocomplete("${createLink(action: 'autocompleteTags', params: [max: 20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            preventDefaultReturn: true,
            selectFirst: true
        });
        $("input[name='username']").autocomplete("${createLink(action: 'autocompleteUsernameSimple', params: [max: 20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            preventDefaultReturn: true,
            selectFirst: true
        });
        $('input[name="company"]').on('change', function(ev) {
            $("#name").focus();
        });
        $('input[name="person"]').on('change', function(ev) {
            if($(this).is(':checked')) {
                $("#parentField").show();
            } else {
                $("#parent").val('');
                $("#parentField").hide();
            }
            $("#name").focus();
        });
        if($("#parent").val() || $('input[name="person"]').is(':checked')) {
            $("#parentField").show();
        }
    });
    </r:script>
</head>

<body>

<g:hasErrors bean="${crmContact}">
    <ul class="errors" role="alert">
        <g:eachError bean="${crmContact}" var="error">
            <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                    error="${error}"/></li>
        </g:eachError>
    </ul>
</g:hasErrors>

<crm:header title="crmContact.find.title" args="[entityName]"/>

<g:form action="list">

    <div class="row-fluid">

        <f:with bean="cmd">
            <div class="span4">
                <div class="row-fluid">
                    <div class="control-group">
                        <label class="control-label"><g:message code="crmContact.type.label"
                                                                default="Type"/></label>

                        <div class="controls">
                            <label class="checkbox inline">
                                <g:checkBox name="company" value="true" checked="${cmd.company}"/>
                                <g:message code="crmCompany.label" default="Company"/>
                            </label>
                            <label class="checkbox inline">
                                <g:checkBox name="person" value="true" checked="${cmd.person}"/>
                                <g:message code="crmPerson.label" default="Person"/>
                            </label>
                        </div>
                    </div>
                    <f:field property="name" label="crmContact.name.label">
                        <g:textField name="name" value="${cmd?.name}" autofocus="" class="span12"/>
                    </f:field>
                    <div id="parentField" class="control-group hide">
                        <label class="control-label">
                            <g:message code="crmContact.parent.label"/>
                        </label>

                        <div class="controls">
                            <g:textField name="parent" value="${cmd?.parent}" class="span12"/>
                        </div>
                    </div>
                    <f:field property="title" label="crmContact.title.label" input-class="span12"/>
                    <f:field property="email" label="crmContact.email.label" input-class="span12"/>
                    <f:field property="telephone" label="crmContact.telephone.label" input-class="span12"/>
                </div>
            </div>

            <div class="span4">
                <div class="row-fluid">
                    <f:field property="address1" label="crmAddress.address1.label" input-class="span12"/>
                    <f:field property="address2" label="crmAddress.address2.label" input-class="span12"/>
                    <f:field property="address3" label="crmAddress.address3.label" input-class="span12"/>

                    <div class="control-group">
                        <label class="control-label"><g:message code="crmAddress.postalAddress.label"/></label>

                        <div class="controls">
                            <g:textField name="postalCode" value="${cmd.postalCode}" class="span4"/>
                            <g:textField name="city" value="${cmd.city}" class="span8"/>
                        </div>
                    </div>

                    <f:field property="country" label="crmAddress.country.label" input-class="span12"/>

                    <f:field property="username" label="crmContact.username.label" input-class="span12"/>
                </div>
            </div>

            <div class="span3">
                <div class="row-fluid">
                    <f:field property="number" label="crmContact.number.label" input-class="span10"/>
                    <f:field property="number2" label="crmContact.number2.label" input-class="span10"/>
                    <f:field property="ssn" label="crmContact.ssn.label" input-class="span10"/>
                    <g:render template="birthdates"
                              model="${[crmContact: cmd, placeholder: true]}"/>

                    <f:field property="category" label="crmContact.category.label">
                        <g:textField name="category" value="${cmd.category}" class="span10" autocomplete="off"/>
                    </f:field>

                    <f:field property="tags" label="crmContact.tags.label">
                        <g:textField name="tags" value="${cmd.tags}" class="span10" autocomplete="off"
                                     placeholder="${message(code: 'crmContact.tags.placeholder', default: '')}"/>
                    </f:field>
                </div>
            </div>
        </f:with>

    </div>

    <div class="form-actions btn-toolbar">
        <crm:selectionMenu visual="primary">
            <crm:button action="list" icon="icon-search icon-white" visual="primary"
                        label="crmContact.button.search.label" accesskey="s"/>
        </crm:selectionMenu>

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

        <g:link action="clearQuery" class="btn btn-link"><g:message code="crmContact.button.query.clear.label"
                                                                    default="Reset fields"/></g:link>
    </div>

</g:form>

</body>
</html>
