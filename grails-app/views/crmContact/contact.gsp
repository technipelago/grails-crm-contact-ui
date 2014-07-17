<%@ page import="grails.plugins.crm.contact.CrmContactAddress; grails.plugins.crm.core.TenantUtils; grails.plugins.crm.contact.CrmAddressType" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmCompany.label', default: 'Contact')}"/>
    <title><g:message code="crmContact.create.contact.title" args="[crmContact.parent ?: entityName]"/></title>
    <r:require modules="googleMaps,autocomplete"/>
    <r:script>
    $(document).ready(function() {
        $("input[name='title']").autocomplete("${createLink(action: 'autocompleteTitle', params:[max:20])}", {
            remoteDataType: 'json',
            useCache: false,
            filter: false,
            preventDefaultReturn: true,
            selectFirst: true
        });

        $("#parent").autocomplete("${createLink(action: 'autocompleteCompany', params:[max:20])}", {
        remoteDataType: 'json',
        useCache: false,
        filter: false,
        preventDefaultReturn: true,
        selectFirst: true,
        onItemSelect: function(item) {
            var id = item.data[0];
            $("#parent-id").val(id);
            $.getJSON("${createLink(action: 'show')}", {id:id, format:'json'}, function(data) {
                var a = data.address;
                $.each(a, function(key, value) {
                    $("input[name='address." + key + "']").val(value);
                });
            });
        }
        });
        $(".show-visible-extra").click(function(event) {
            event.stopPropagation();
            var tab = $(this).closest(".tab-pane");
            $(".visible-extra", tab).slideDown();
            $(".hidden-extra", tab).hide();
            return false;
        });
        $(".hide-visible-extra").click(function(event) {
            event.stopPropagation();
            var tab = $(this).closest(".tab-pane");
            $(".visible-extra", tab).slideUp();
            $(".hidden-extra", tab).show();
            return false;
        });

        // Put focus in first open field after tab change.
        $('a[data-toggle="tab"]').on('shown', function (ev) {
            var tab = $(ev.target.hash);
            $(":input[type='text']:visible:enabled:first", tab).focus();
        });

        $("#firstName").focus();
        $("#parent").focus();
    });
    </r:script>
</head>

<body>

<crm:header title="crmContact.create.contact.title" subtitle="${crmContact.parent}"
            args="[crmContact.parent ?: entityName]"/>

<g:hasErrors bean="${parentContact}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${parentContact}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:hasErrors bean="${crmContact}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${crmContact}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="contact">
    <g:hiddenField name="referer" id="hiddenReferer" value="${referer}"/>

    <div class="tabbable">
        <ul class="nav nav-tabs">
            <li class="active"><a href="#main" data-toggle="tab"><g:message code="crmContact.tab.main.label"/></a></li>
            <g:each in="${addressTypes}" var="addressType" status="i">
                <li>
                    <a href="#${addressType.param ?: 'a' + addressType.id}"
                       data-toggle="tab">${addressType.encodeAsHTML()}
                    <g:if test="${crmContact.addresses?.find { it.type == addressType }}">
                        (1)
                    </g:if>
                    </a>
                </li>
            </g:each>
            <li><a href="#desc" data-toggle="tab" accesskey="d"><g:message
                    code="crmContact.tab.desc.label"/></a></li>
        </ul>

        <div class="tab-content">
            <div class="tab-pane active" id="main">
                <f:with bean="crmContact">

                    <div class="row-fluid">

                        <div class="span4">
                            <g:unless test="${crmContact.parent}">
                                <f:field property="parent" label="crmContact.parent.label">
                                    <input type="text" name="parentName" id="parent"
                                           value="${crmContact.parent?.name}"
                                           class="span11" autocomplete="off"/>
                                </f:field>
                            </g:unless>
                            <input type="hidden" name="parent.id" id="parent-id"
                                   value="${crmContact.parent?.id}"/>
                            <f:field property="firstName" label="crmContact.firstName.label" input-class="span11"
                                     required=""/>
                            <f:field property="lastName" label="crmContact.lastName.label" input-class="span11"/>
                            <f:field property="title" label="crmContact.title.label" input-class="span11"/>
                        </div>

                        <div class="span4">
                            <f:field property="telephone" input-class="span8"/>
                            <f:field property="mobile" input-class="span8"/>
                            <f:field property="email" input-class="span11"/>
                        </div>

                        <div class="span4">
                            <f:field property="number">
                                <input type="text" name="number" id="number" value="${crmContact.number}"
                                       novalidate="" autocomplete="off" class="input-medium"/>
                            </f:field>
                            <f:field property="ssn" label="crmPerson.ssn.label" input-class="input-medium"/>

                            <g:render template="birthdates" model="${[crmContact: crmContact, placeholder: true]}"/>

                            <f:field property="username">
                                <g:select name="username" from="${userList}"
                                      optionKey="username" optionValue="name" noSelection="['': '']"
                                      value="${crmContact.username}" class="input-large"/>
                            </f:field>
                        </div>

                    </div>

                </f:with>

            </div>

            <g:each in="${addressTypes}" var="addressType" status="i">
                <div class="tab-pane" id="${addressType.param ?: 'a' + addressType.id}">
                    <g:set var="parentAddr"
                           value="${crmContact.parent?.addresses?.find { it.type == addressType }}"/>
                    <g:set var="myAddr" value="${crmContact.addresses?.find { it.type == addressType }}"/>
                    <div class="row-fluid">
                        <div class="span6">
                            <g:if test="${parentAddr}">
                                <p>Avvikande ${addressType.encodeAsHTML()}</p>
                            </g:if>
                            <g:render template="address"
                                      model="${[bean: myAddr ?: new CrmContactAddress(type: addressType, contact: crmContact), row: i]}"/>

                            <a class="show-visible-extra hidden-extra"
                               href="javascript:void(0)"><g:message code="crmContact.fields.show.more" default="Show more fields"/></a>
                            <a class="hide-visible-extra visible-extra hide"
                               href="javascript:void(0)"><g:message code="crmContact.fields.show.less" default="Show less fields"/></a>
                        </div>

                        <g:if test="${parentAddr}">
                            <div class="span6">
                                <p>${addressType.encodeAsHTML()} via ${crmContact.parent.encodeAsHTML()}</p>
                                <g:render template="address"
                                          model="${[bean: parentAddr, row: i, prefix: 'parent', disabled: true]}"/>
                            </div>
                        </g:if>

                    </div>
                </div>
            </g:each>

            <div class="tab-pane" id="desc">
                <f:field property="description" label="crmContact.description.label">
                    <g:textArea name="description" rows="10" cols="80"
                                value="${crmContact.description}" class="span10"/>
                </f:field>
            </div>

        </div>
    </div>

    <div class="form-actions">
        <crm:button icon="icon-ok icon-white" visual="success" label="crmContact.button.save.label"/>
        <crm:button type="link" action="create" icon="icon-remove"
                    label="crmContact.button.cancel.label"
                    accesskey="B"/>
    </div>

</g:form>

<g:if test="${grailsApplication.config.crm.map.google.api.key}">
    <g:render template="map-selector" model="${[key: grailsApplication.config.crm.map.google.api.key]}"/>
</g:if>

</body>
</html>
