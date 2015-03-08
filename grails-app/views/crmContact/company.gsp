<%@ page import="grails.plugins.crm.contact.CrmContactAddress; grails.plugins.crm.core.TenantUtils; grails.plugins.crm.contact.CrmAddressType" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmCompany.label', default: 'Company')}"/>
    <title><g:message code="crmContact.create.company.title" args="[entityName]"/></title>
    <r:require modules="googleMaps,autocomplete,select2"/>
    <r:script>
        function addCategoryInput() {
            var $div = $('<div class="row-fluid"/>');
            var $newInput = $('<input type="text" name="category" value="" class="crm-category span11" autocomplete="off"/>');
            $newInput.autocomplete("${createLink(action: 'autocompleteCategoryType', params: [max: 20])}", {
                remoteDataType: 'json',
                useCache: false,
                filter: false,
                preventDefaultReturn: true,
                selectFirst: true
            });
            $div.append($newInput);
            $("#category-container").append($div);
            $newInput.focus();
        }
        $(document).ready(function() {
            $("input.crm-category").autocomplete("${createLink(action: 'autocompleteCategoryType', params: [max: 20])}", {
                remoteDataType: 'json',
                useCache: false,
                filter: false,
                preventDefaultReturn: true,
                selectFirst: true
            });
            $("#btn-add-category").click(function(ev) {
                ev.preventDefault();
                addCategoryInput();
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
        });
    </r:script>
</head>

<body>

<crm:header title="crmContact.create.company.title" args="[entityName]"/>

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

<g:form action="company">
    <g:hiddenField name="referer" id="hiddenReferer" value="${referer}"/>

    <div class="tabbable">
        <ul class="nav nav-tabs">
            <li class="active"><a href="#main" data-toggle="tab" accesskey="n"><g:message
                    code="crmContact.tab.main.label"/></a></li>
            <g:each in="${addressTypes}" var="addressType" status="i">
                <li>
                    <a href="#${addressType.param ?: 'a' + addressType.id}"
                       data-toggle="tab"
                       accesskey="${addressType.name[0].toLowerCase()}">${addressType.encodeAsHTML()}
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
                            <f:field property="name" label="crmCompany.name.label" input-class="span11"
                                     input-autofocus=""/>
                            <f:field property="telephone" input-class="span8"/>
                            <f:field property="fax" input-class="span8"/>
                        </div>

                        <div class="span4">
                            <f:field property="email" input-class="span11"/>
                            <f:field property="url" input-class="span11"/>

                            <div class="control-group">
                                <label class="control-label"><g:message code="crmContact.category.label"
                                                                        default="Category"/>
                                    <a href="#" id="btn-add-category"><i class="icon-plus-sign"></i></a>
                                </label>

                                <div class="controls" id="category-container">
                                    <g:set var="categories"
                                           value="${crmContact.categories?.sort { it.toString() } ?: []}"/>
                                    <g:if test="${categories}">
                                        <g:each in="${categories}" var="c" status="i">
                                            <div class="row-fluid">
                                                <input type="text" name="category" value="${c.toString()}"
                                                       class="crm-category span11" autocomplete="off"/>
                                            </div>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        <div class="row-fluid">
                                            <input type="text" name="category" value=""
                                                   class="crm-category span11" autocomplete="off"/>
                                        </div>
                                    </g:else>
                                </div>
                            </div>
                        </div>

                        <div class="span4">
                            <f:field property="number">
                                <g:textField name="number" value="${crmContact.number}"
                                       novalidate="" autocomplete="off" class="span8"/>
                            </f:field>
                            <f:field property="ssn" label="crmCompany.ssn.label" input-class="span8"/>
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
