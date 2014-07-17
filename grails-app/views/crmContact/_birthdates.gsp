<div class="control-group">
    <label class="control-label"><g:message code="crmContact.birthDate.label"
                                            default="Birth Date"/></label>

    <div class="controls controls-row">
        <g:textField name="birthYear" class="span4" size="4"
                     value="${crmContact.birthYear}"
                     placeholder="${placeholder ? message(code: 'crmContact.birthYear.placeholder', default: 'yyyy') : ''}"/>
        <g:textField name="birthMonth" class="span3" size="2"
                     value="${crmContact.birthMonth ?: ''}"
                     placeholder="${placeholder ? message(code: 'crmContact.birthMonth.placeholder', default: 'mm') : ''}"/>
        <g:textField name="birthDay" class="span3" size="2"
                     value="${crmContact.birthDay ?: ''}"
                     placeholder="${placeholder ? message(code: 'crmContact.birthDay.placeholder', default: 'dd') : ''}"/>
    </div>
</div>