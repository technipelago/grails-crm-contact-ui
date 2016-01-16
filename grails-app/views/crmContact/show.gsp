<%@ page import="grails.plugins.crm.core.TenantUtils; grails.plugins.crm.contact.CrmContact" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'crmContact.label', default: 'Contact')}"/>
    <title><g:message code="crmContact.show.title" args="[entityName, crmContact]"/></title>
    <r:require module="select2"/>
    <r:script>
        $(document).ready(function () {
            $('#editModal').on('shown', function () {
                // If there's no employers in the list, hide the accept button.
                if (!$(":radio", $(this))) {
                    $("button[name='_action_changeParent']").hide();
                }
            });
            $("#relations-list a.crm-delete").click(function(ev) {
                ev.preventDefault();

                if(confirm("${message(code: 'crmContactRelation.delete.confirm', default: 'Remove relation?')}")) {
                    var id = $(this).data("crm-id");
                    $.post("${createLink(action: 'deleteRelation', id: crmContact.id)}", {r: id}, function(data) {
                        window.location.href = "${createLink(action: 'show', id: crmContact.id, fragment: 'relations')}";
                    });
                }
            });
            $("#relations-list a.crm-edit").click(function(ev) {
                ev.preventDefault();
                var $modal = $("#relationModal");
                var id = $(this).data('crm-id');
                $modal.load("${createLink(action: 'editRelation', id: crmContact.id)}?r=" + id, function() {
                    $modal.modal('show');
                });
            });
            $("#add-relation").click(function(ev) {
                ev.preventDefault();
                var $modal = $("#relationModal");
                $modal.load("${createLink(action: 'addRelation', id: crmContact.id)}", function() {

                    var $searchField = $('input[name="related"]', $modal);

                    $searchField.select2({
                        ajax: {
                            url: "${createLink(action: 'autocompleteContact', id: crmContact.id)}",
                            dataType: 'json',
                            data: function (term, page) {
                                return {
                                    q: term, // search term
                                    limit: 10
                                };
                            },
                            results: function (data, page) {
                                return {results: data};
                            }
                        },
                        placeholder: "${message(code: 'crmContactRelation.create.placeholder')}",
                        allowClear: true,
                        minimumInputLength: 1,
                        createSearchChoice: function(term) {
                            var sanitized = term.replace(/,/g, " ")
                            return {id: sanitized, name: sanitized};
                        },
                        createSearchChoicePosition: "top",
                        escapeMarkup: function (m) { return m; },
                        formatResult: function(data) { return data.recent ? '<strong>' + data.name + '</strong>' : data.name; },
                        formatSelection: function(data) { return data.name; },
                        formatNoMatches: function (term) { return "${message(code: 'crmContact.search.noresult')}"; },
                        formatInputTooShort: function (input, min) { return "${message(code: 'crmContact.search.help')}"; },
                        formatInputTooLong: function (input, max) { return "${message(code: 'crmContact.search.help')}"; },
                        formatLoadMore: function (pageNumber) { return "${message(code: 'crmContact.search.loading')}"; },
                        formatSearching: function () { return "${message(code: 'crmContact.search.searching')}"; }
                    });

                    $modal.modal('show');
                });
            });
        });
    </r:script>
</head>

<body>

<div class="row-fluid">
<div class="span9">

<header class="page-header clearfix">
    <g:if test="${crmContact.person && crmContact.email}">
        <avatar:gravatar email="${crmContact.email}" size="64" id="avatar" cssClass="avatar pull-right"
                         defaultGravatarUrl="mm"/>
    </g:if>

    <g:if test="${crmContact.company}">
        <img src="${resource(dir: 'images', file: 'company-avatar.png')}" class="avatar pull-right"
             width="64" height="64"/>
    </g:if>

    <crm:user>
        <h1>
            ${crmContact.encodeAsHTML()}
            <crm:favoriteIcon bean="${crmContact}"/>
            <small>${crmContact.title?.encodeAsHTML()}</small>
        </h1>
    </crm:user>

    <g:if test="${primaryRelation}">
        <h2>${primaryRelation.name.encodeAsHTML()}</h2>
    </g:if>
</header>

<div class="tabbable">

<ul class="nav nav-tabs">
    <li class="active"><a href="#main" data-toggle="tab"><g:message code="crmContact.tab.main.label"/></a>
    </li>
    <li>
        <a href="#relations" data-toggle="tab">
            <g:message code="crmContact.tab.relations.label"/>
            <crm:countIndicator count="${relations.size() + children.size() + (crmContact.parent ? 1 : 0)}"/>
        </a>
    </li>
    <crm:pluginViews location="tabs" var="view">
        <crm:pluginTab id="${view.id}" label="${view.label}" count="${view.model?.totalCount}"/>
    </crm:pluginViews>
</ul>

<div class="tab-content">

<div class="tab-pane active" id="main">
<div class="row-fluid">
    <div class="span4">
        <dl>
            <dt><g:message code="crmContact.name.label"/></dt>
            <dd>${crmContact.name.encodeAsHTML()}</dd>

            <g:if test="${crmContact.title}">
                <dt><g:message code="crmContact.title.label" default="Title"/></dt>
                <dd><g:fieldValue bean="${crmContact}" field="title"/></dd>
            </g:if>

            <g:if test="${crmContact.parent}">
                <dt><g:message code="crmCompany.parent.label" default="Parent Company"/></dt>
                <dd><g:link action="show" id="${crmContact.parent.id}"><g:fieldValue
                        bean="${crmContact}"
                        field="parent"/></g:link></dd>
            </g:if>

            <g:if test="${primaryRelation}">
                <dt>${primaryRelation.relation.name}</dt>
                <dd>
                    <g:link action="show" id="${primaryRelation.id}">
                        ${primaryRelation.name.encodeAsHTML()}
                    </g:link>
                </dd>
            </g:if>

            <g:if test="${crmContact.addresses}">
                <g:each in="${crmContact.addresses.sort { it.type.orderIndex }}" var="address" status="i">
                    <dt>${address.type}</dt>
                    <dd>${address}
                        <g:if test="${address.latitude && address.longitude}">
                            <a href="http://maps.google.com/?q=${crmContact.encodeAsURL()}@${address.latitude},${address.longitude}&z=16&t=m"
                               target="map"
                               title="${message(code: 'crmContact.map.show.label', default: 'Show on map')}"><i
                                    class="icon-map-marker"></i>
                            </a>
                        </g:if>
                    </dd>
                </g:each>
            </g:if>
            <g:else>
                <dt>${crmContact.address?.type}</dt>
                <dd>${crmContact.address?.encodeAsHTML()}</dd>
            </g:else>
        </dl>
    </div>

    <div class="span4">
        <dl>
            <g:if test="${crmContact.email}">
                <dt><g:message code="crmContact.email.label" default="Email"/></dt>
                <dd><a href="mailto:${crmContact.email}"><g:decorate include="abbreviate" max="30"><g:fieldValue
                        bean="${crmContact}"
                        field="email"/></g:decorate></a>
                </dd>
            </g:if>
            <g:if test="${crmContact.telephone}">
                <dt><g:message code="crmContact.telephone.label" default="Telephone"/></dt>
                <dd><a href="tel:${crmContact.telephone}"><g:fieldValue bean="${crmContact}"
                                                                        field="telephone"/></a>
                </dd>
            </g:if>
            <g:if test="${crmContact.mobile}">
                <dt><g:message code="crmContact.mobile.label" default="Mobile"/></dt>
                <dd><a href="tel:${crmContact.mobile}"><g:fieldValue bean="${crmContact}"
                                                                     field="mobile"/></a>
                </dd>
            </g:if>
            <g:if test="${crmContact.fax}">
                <dt><g:message code="crmContact.fax.label" default="Fax"/></dt>
                <dd><a href="tel:${crmContact.fax}"><g:fieldValue bean="${crmContact}"
                                                                  field="fax"/></a>
                </dd>
            </g:if>
            <g:if test="${crmContact.url}">
                <dt><g:message code="crmContact.url.label" default="Web"/></dt>
                <dd><g:decorate include="url"><g:fieldValue bean="${crmContact}" field="url"/></g:decorate></dd>
            </g:if>
        </dl>
    </div>

    <div class="span4">
        <dl>
            <g:if test="${crmContact.categories}">
                <dt><g:message code="crmContact.category.label" default="Categorys"/></dt>
                <dd>${crmContact.categories.sort { it.toString() }.join(', ').encodeAsHTML()}</dd>
            </g:if>
            <g:if test="${crmContact.number}">
                <dt><g:message code="crmContact.number.label" default="Customer ID"/></dt>
                <dd><g:fieldValue bean="${crmContact}" field="number"/></dd>
            </g:if>
            <g:if test="${crmContact.number2}">
                <dt><g:message code="crmContact.number2.label" default="Reference Number"/></dt>
                <dd><g:fieldValue bean="${crmContact}" field="number2"/></dd>
            </g:if>
            <g:if test="${crmContact.ssn}">
                <dt><g:message code="crmContact.ssn.label" default="Social Security Number"/></dt>
                <dd><g:fieldValue bean="${crmContact}" field="ssn"/></dd>
            </g:if>
            <g:if test="${crmContact.duns}">
                <dt><g:message code="crmContact.duns.label" default="D-U-N-S Number"/></dt>
                <dd><g:fieldValue bean="${crmContact}" field="duns"/></dd>
            </g:if>
            <g:if test="${crmContact.birthYear || crmContact.birthMonth || crmContact.birthDay}">
                <dt><g:message code="crmContact.birthDate.label" default="Date of Birth"/></dt>
                <dd>
                    <g:if test="${crmContact.birthDay}">${crmContact.birthDay}</g:if>
                    <g:if test="${crmContact.birthMonth}">${message(code: 'default.monthName.' + crmContact.birthMonth + '.long', default: crmContact.birthMonth.toString())}</g:if>
                    <g:if test="${crmContact.birthYear}">${crmContact.birthYear}</g:if>
                </dd>
            </g:if>
            <g:if test="${crmContact.username}">
                <dt><g:message code="crmContact.username.label" default="Owner"/></dt>
                <dd><crm:user username="${crmContact.username}">${name}</crm:user></dd>
            </g:if>
        </dl>

        <div class="vcard hide">
            ${crmContact.vcard.replace('\n', '<br/>\n')}
        </div>
    </div>
</div>

<g:if test="${crmContact.description}">
    <div class="row-fluid">
        <div class="span8">
            <dl>
                <dt><g:message code="crmContact.description.label" default="Description"/></dt>
                <dd><g:decorate encode="HTML" nlbr="true">${crmContact.description}</g:decorate></dd>
            </dl>
        </div>
    </div>
</g:if>

<div class="form-actions btn-toolbar">

    <crm:selectionMenu location="crmContact" visual="primary">
        <crm:button type="link" controller="crmContact" action="index"
                    visual="primary" icon="icon-search icon-white"
                    label="crmContact.find.label" permission="crmContact:show"/>
    </crm:selectionMenu>

    <crm:button type="link" group="true" action="edit" id="${crmContact?.id}" visual="warning"
                icon="icon-pencil icon-white" accesskey="r"
                label="crmContact.button.edit.label" permission="crmContact:edit">
        <button class="btn btn-warning dropdown-toggle" data-toggle="dropdown">
            <span class="caret"></span>
        </button>
        <ul class="dropdown-menu">
            <li>
                <g:link action="changeType" id="${crmContact.id}"
                        title="${message(code: 'crmContact.change.type.help', default: '')}"
                        onclick="return confirm('${message(code: 'crmContact.change.type.confirm', default: 'Are you sure you want to change type?')}')">
                    <g:message code="crmContact.button.change.type.label" default="Change type to {1}"
                               args="${[crmContact.toString(), crmContact.company ? message(code: 'crmPerson.label', default: 'Person') : message(code: 'crmCompany.label', default: 'Company')]}"/>
                </g:link>
            </li>
        </ul>
    </crm:button>

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
                    <g:link controller="crmContact" action="contact"
                            params="${crmContact.company ? ['parent.id': crmContact.id, referer: g.createLink(action: "show", id: crmContact.id, fragment: "children")] : [:]}"><g:message
                            code="crmContact.button.create.contact.label" default="Contact"/></g:link>
                </li>
                <li>
                    <g:link controller="crmContact" action="person"><g:message
                            code="crmContact.button.create.person.label" default="Individual"/></g:link>
                </li>
            </ul>
        </crm:button>
    </crm:hasPermission>

    <div class="btn-group">
        <button class="btn btn-info dropdown-toggle" data-toggle="dropdown">
            <i class="icon-info-sign icon-white"></i>
            <g:message code="crmContact.button.view.label" default="View"/>
            <span class="caret"></span></button>
        <ul class="dropdown-menu">
            <g:if test="${selection}">
                <li>
                    <select:link action="list" selection="${selection}" params="${[view: 'list']}">
                        <g:message code="crmContact.show.result.label" default="Show result in list view"/>
                    </select:link>
                </li>
            </g:if>
            <crm:hasPermission permission="crmContact:createFavorite">
                <crm:user>
                    <g:if test="${crmContact.isUserTagged('favorite', username)}">
                        <li>
                            <g:link action="deleteFavorite" id="${crmContact.id}"
                                    title="${message(code: 'crmContact.button.favorite.delete.help', args: [crmContact])}">
                                <g:message code="crmContact.button.favorite.delete.label"/></g:link>
                        </li>
                    </g:if>
                    <g:else>
                        <li>
                            <g:link action="createFavorite" id="${crmContact.id}"
                                    title="${message(code: 'crmContact.button.favorite.create.help', args: [crmContact])}">
                                <g:message code="crmContact.button.favorite.create.label"/></g:link>
                        </li>
                    </g:else>
                </crm:user>
            </crm:hasPermission>

            <li>
                <a href="${createLink(action: 'qrcode', id: crmContact.id)}" data-toggle="modal"
                   data-target="#qrcodeModal">
                    <g:message code="crmContact.button.vcard.label" default="Show vCard"/>
                </a>
            </li>

            <g:if test="${externalLink}">
                <li>
                    <a href="${externalLink.link}" target="ext">${externalLink.label}</a>
                </li>
            </g:if>
        </ul>
    </div>

</div>

<crm:timestamp bean="${crmContact}"/>

</div>

<div class="tab-pane" id="relations">
    <tmpl:relations bean="${crmContact}" children="${children}" relations="${relations}"/>

    <g:form>
        <g:hiddenField name="id" value="${crmContact?.id}"/>
        <div class="form-actions btn-toolbar">
            <crm:hasPermission permission="crmContact:create">
                <g:link action="addRelation" id="${crmContact.id}" class="btn btn-success" elementId="add-relation">
                    <i class="icon-resize-small icon-white"></i>
                    <g:message code="crmContactRelation.create.label" default="Add Relation"/>
                </g:link>
            </crm:hasPermission>
        </div>
    </g:form>
</div>

<crm:pluginViews location="tabs" var="view">
    <div class="tab-pane tab-${view.id}" id="${view.id}">
        <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
    </div>
</crm:pluginViews>

</div>
</div>

</div>

<div class="span3">

    <g:render template="/tags" plugin="crm-tags" model="${[bean: crmContact]}"/>

    <crm:pluginViews location="sidebar" var="view">
        <g:render template="${view.template}" model="${view.model}" plugin="${view.plugin}"/>
    </crm:pluginViews>

</div>

</div>

<div class="modal hide fade" id="relationModal"></div>

<div class="modal hide fade" id="qrcodeModal">

    <div class="modal-header">
        <a class="close" data-dismiss="modal">×</a>

        <h3><g:message code="crmContact.vcard.qrcode.title" default="vCard" args="${[crmContact]}"/></h3>
    </div>

    <div class="modal-body"><!-- This space is loaded by crmContact/qrcode --></div>

    <div class="modal-footer">
        <a href="#" class="btn btn-primary" data-dismiss="modal"><i class="icon-ok icon-white"></i> <g:message
                code="default.button.close.label" default="Close"/></a>
    </div>

</div>

</body>
</html>
