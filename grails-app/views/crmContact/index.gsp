<%@ page import="grails.plugins.crm.contact.CrmContact" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmContact.label', default: 'Contact')}"/>
    <title><g:message code="crmContact.find.title" args="[entityName]"/></title>
    <r:require module="autocomplete"/>
    <r:script>
    var CRM = {
        showParentFields: function() {
            var $company = $('input[name="company"]');
            var $person = $('input[name="person"]');
            if($company.is(':checked') && $person.is(':checked')) {
                $("#parent").val('');
                $("#related").val('');
                $("#title").val('');
                $(".company-group").hide();
                $(".person-group").hide();
            } else if($company.is(':checked')) {
                $("#related").val('');
                $("#title").val('');
                $(".person-group").hide();
                $(".company-group").show();
            } else if($person.is(':checked')) {
                $("#parent").val('');
                $(".company-group").hide();
                $(".person-group").show();
            } else {
                $("#parent").val('');
                $("#related").val('');
                $("#title").val('');
                $(".company-group").hide();
                $(".person-group").hide();
            }
        }
    };
    $(document).ready(function() {
        $("input[name='title']").autocomplete("${createLink(action: 'autocompleteTitle', params: [max: 20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            minChars: 1,
            preventDefaultReturn: true,
            selectFirst: true
        });
        $("input[name='category']").autocomplete("${createLink(action: 'autocompleteCategoryType', params: [max: 20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            minChars: 1,
            preventDefaultReturn: true,
            selectFirst: true
        });
        $("input[name='role']").autocomplete("${createLink(action: 'autocompleteRelationType', params: [max: 20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            minChars: 1,
            preventDefaultReturn: true,
            selectFirst: true
        });
        $("input[name='tags']").autocomplete("${createLink(action: 'autocompleteTags', params: [max: 20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            minChars: 1,
            preventDefaultReturn: true,
            selectFirst: true
        });
        $("input[name='username']").autocomplete("${createLink(action: 'autocompleteUsernameSimple', params: [max: 20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            minChars: 1,
            preventDefaultReturn: true,
            selectFirst: true
        });
        $('input[name="company"]').on('change', function(ev) {
            CRM.showParentFields();
            $("#name").focus();
        });
        $('input[name="person"]').on('change', function(ev) {
            CRM.showParentFields();
            $("#name").focus();
        });
        CRM.showParentFields();
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

    <div class="span3">
        <div class="row-fluid">
            <div class="control-group" style="padding-bottom: 15px;">
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

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContact.name.label"/>
                </label>

                <div class="controls">
                    <g:textField name="name" value="${cmd.name}" autofocus="" autocorrect="off" class="span12"/>
                </div>
            </div>

            <div id="parentField" class="control-group company-group">
                <label class="control-label">
                    <g:message code="crmContact.parent.label"/>
                </label>

                <div class="controls">
                    <g:textField name="parent" value="${cmd?.parent}" autocorrect="off" class="span12"/>
                </div>
            </div>

            <div id="relatedField" class="control-group person-group">
                <label class="control-label">
                    <g:message code="crmContact.related.label"/>
                </label>

                <div class="controls">
                    <g:textField name="related" value="${cmd?.related}" autocorrect="off" class="span12"/>
                </div>
            </div>

            <div id="titleField" class="control-group person-group">
                <label class="control-label">
                    <g:message code="crmContact.title.label"/>
                </label>

                <div class="controls">
                    <g:textField name="title" value="${cmd.title}" class="span12"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContact.email.label"/>
                </label>

                <div class="controls">
                    <g:textField name="email" value="${cmd.email}" autocorrect="off" class="span12"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContact.telephone.label"/>
                </label>

                <div class="controls">
                    <g:textField name="telephone" value="${cmd.telephone}" autocorrect="off" class="span12"/>
                </div>
            </div>
        </div>
    </div>

    <div class="span3">
        <div class="row-fluid">
            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmAddress.address1.label"/>
                </label>

                <div class="controls">
                    <g:textField name="address1" value="${cmd.address1}" autocorrect="off" class="span12"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmAddress.address2.label"/>
                </label>

                <div class="controls">
                    <g:textField name="address2" value="${cmd.address2}" autocorrect="off" class="span12"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmAddress.address3.label"/>
                </label>

                <div class="controls">
                    <g:textField name="address3" value="${cmd.address3}" autocorrect="off" class="span12"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label"><g:message code="crmAddress.postalAddress.label"/></label>

                <div class="controls">
                    <g:textField name="postalCode" value="${cmd.postalCode}" autocorrect="off" class="span4"/>
                    <g:textField name="city" value="${cmd.city}" autocorrect="off" class="span8"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmAddress.country.label"/>
                </label>

                <div class="controls">
                    <g:textField name="country" value="${cmd.country}" class="span12"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContact.username.label"/>
                </label>

                <div class="controls">
                    <g:textField name="username" value="${cmd.username}" autocorrect="off" class="span12"/>
                </div>
            </div>
        </div>
    </div>

    <div class="span3">
        <div class="row-fluid">

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContactRelation.type.label"/>
                </label>

                <div class="controls">
                    <g:textField name="role" value="${cmd.role}" class="span11" autocomplete="off" autocorrect="off"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContact.category.label"/>
                </label>

                <div class="controls">
                    <g:textField name="category" value="${cmd.category}" class="span11" autocomplete="off" autocorrect="off"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContact.tags.label"/>
                </label>

                <div class="controls">
                    <g:textField name="tags" value="${cmd.tags}" class="span11" autocomplete="off" autocorrect="off"
                                 placeholder="${message(code: 'crmContact.tags.placeholder', default: '')}"/>
                </div>
            </div>

        </div>
    </div>

    <div class="span3">
        <div class="row-fluid">
            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContact.number.label"/>
                </label>

                <div class="controls">
                    <g:textField name="number" value="${cmd.number}" autocorrect="off" class="span11"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContact.number2.label"/>
                </label>

                <div class="controls">
                    <g:textField name="number2" value="${cmd.number2}" autocorrect="off" class="span11"/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">
                    <g:message code="crmContact.ssn.label"/>
                </label>

                <div class="controls">
                    <g:textField name="ssn" value="${cmd.ssn}" autocorrect="off" class="span11"/>
                </div>
            </div>

            <g:render template="birthdates" model="${[crmContact: cmd, placeholder: true]}"/>

        </div>
    </div>

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
