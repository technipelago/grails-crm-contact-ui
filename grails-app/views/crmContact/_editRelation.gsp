<g:form action="editRelation">

    <input type="hidden" name="id" value="${crmContact.id}"/>
    <input type="hidden" name="r" value="${bean.id}"/>
    <input type="hidden" name="version" value="${bean.version}"/>

    <g:set var="entityName" value="${message(code: 'crmContactRelation.label', default: 'Relation')}"/>

    <div class="modal-header">
        <a class="close" data-dismiss="modal">×</a>

        <h3><g:message code="crmContactRelation.edit.title" default="Edit relation" args="${[entityName, bean]}"/></h3>
    </div>

    <div class="modal-body">
        <div class="control-group">
            <label class="control-label">Typ av relation</label>

            <div class="controls">
                <g:select name="type.id" value="${bean.type.id}" from="${relationTypes}" optionKey="id" class="input-large"/>
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
        <crm:button action="editRelation" visual="success" icon="icon-ok icon-white"
                    label="crmContactRelation.button.save.label" default="Save"/>
        <crm:button action="deleteRelation" visual="danger" icon="icon-trash icon-white"
                            label="crmContactRelation.button.delete.label" default="Delete"
        confirm="crmContactRelation.button.delete.confirm.message"/>
        <a href="#" class="btn" data-dismiss="modal"><i class="icon-remove"></i> <g:message
                code="default.button.close.label" default="Close"/></a>
    </div>
</g:form>
