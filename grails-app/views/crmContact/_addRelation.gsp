<g:form action="addRelation">

    <input type="hidden" name="id" value="${crmContact.id}"/>

    <g:set var="entityName" value="${message(code: 'crmContactRelation.label', default: 'Relation')}"/>

    <div class="modal-header">
        <a class="close" data-dismiss="modal">×</a>

        <h3><g:message code="crmContactRelation.create.title" default="Add relation"
                       args="${[entityName, crmContact]}"/></h3>
    </div>

    <div id="add-relation-body" class="modal-body" style="overflow: auto;">

        <div class="control-group">
            <label class="control-label">Kontakt</label>
            <div class="controls">
                <input type="hidden" name="related" style="width: 75%;"/>
            </div>
        </div>
<%--
        <g:each in="${recentContacts}" var="recent">
            <g:set var="obj" value="${recent.object}"/>
            <option value="${recent.id}">${obj.fullName.encodeAsHTML()} ${obj.address}</option>
        </g:each>
--%>
        <div class="control-group">
            <label class="control-label">Typ av relation</label>

            <div class="controls">
                <g:select name="type" value="${bean.type?.param}" from="${relationTypes}" optionKey="param"
                          class="input-large"/>
            </div>
        </div>

        <div class="control-group">
            <label class="checkbox">
                <g:checkBox name="primary" value="true" checked="${bean.primary}"/>
                Primär relation
            </label>
        </div>

        <div class="control-group">
            <label class="control-label">Beskrivning</label>

            <div class="controls">
                <g:textArea name="description" value="${bean.description}" cols="70" rows="3" class="input-xlarge"/>
            </div>
        </div>
    </div>

    <div class="modal-footer">
        <crm:button action="addRelation" visual="success" icon="icon-ok icon-white"
                    label="crmContactRelation.button.save.label" default="Save"/>
        <a href="#" class="btn" data-dismiss="modal"><i class="icon-remove"></i> <g:message
                code="default.button.close.label" default="Close"/></a>
    </div>
</g:form>
