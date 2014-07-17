<g:hiddenField name="${prefix ?: ''}addresses[${row}].type.id" id="adt${row}" value="${bean.type?.id}"/>

<g:hiddenField name="${prefix ?: ''}addresses[${row}].preferred" id="adp${row}" value="${row == 0}"/>

<div class="control-group">
    <label class="control-label"><g:message code="crmAddress.address1.label"/></label>

    <div class="controls">
        <g:textField name="${prefix ?: ''}addresses[${row}].address1" value="${bean.address1}"
                     disabled="${disabled == true}"/>
    </div>
</div>

<div class="control-group">
    <label class="control-label"><g:message code="crmAddress.address2.label"/></label>

    <div class="controls">
        <g:textField name="${prefix ?: ''}addresses[${row}].address2" value="${bean.address2}"
                     disabled="${disabled == true}"/>
    </div>
</div>

<div class="control-group">
    <label class="control-label"><g:message code="crmAddress.address3.label"/></label>

    <div class="controls">
        <g:textField name="${prefix ?: ''}addresses[${row}].address3" value="${bean.address3}"
                     disabled="${disabled == true}"/>
    </div>
</div>

<div class="control-group">
    <label class="control-label"><g:message code="crmAddress.postalAddress.label"/></label>

    <div class="controls">
        <g:textField name="${prefix ?: ''}addresses[${row}].postalCode" value="${bean.postalCode}" class="input-mini"
                     disabled="${disabled == true}"/>
        <g:textField name="${prefix ?: ''}addresses[${row}].city" value="${bean.city}" class="input-medium"
                     disabled="${disabled == true}"/>
    </div>
</div>


<div class="control-group visible-extra hide">
    <label class="control-label"><g:message code="crmAddress.region.label"/></label>

    <div class="controls">
        <g:textField name="${prefix ?: ''}addresses[${row}].region" value="${bean.region}"
                     disabled="${disabled == true}"/>
    </div>
</div>

<div class="control-group visible-extra hide">
    <label class="control-label"><g:message code="crmAddress.country.label"/></label>

    <div class="controls">
        <g:textField name="${prefix ?: ''}addresses[${row}].country" value="${bean.country}"
                     disabled="${disabled == true}"/>
    </div>
</div>

<%--
<div class="control-group visible-extra hide">
    <label class="control-label"><g:message code="crmAddress.timezone.label"/></label>

    <div class="controls">
        <g:select name="${prefix ?: ''}addresses[${row}].timezone" from="${}" value="${bean.timezone}" disabled="${disabled == true}"/>
    </div>
</div>
--%>
<div class="control-group visible-extra hide">
    <label class="control-label"><g:message code="crmAddress.latitude.label"/></label>

    <div class="controls">
        <div class="input-append">
            <input type="text" name="${prefix ?: ''}addresses[${row}].latitude"
                   value="${formatNumber(number: bean.latitude, type: 'number', minFractionDigits: 6, maxFractionDigits: 6)}"
                   pattern="[0-9,\\.]+" min="-90" max="90" step="0.000001"
                   class="input-medium" ${disabled ? 'disabled=""' : ''}/>
            <g:if test="${grailsApplication.config.crm.map.google.api.key}">
                <button class="btn btn-map" type="button"
                        data-crm-latitude="${prefix ?: ''}addresses[${row}].latitude"
                        data-crm-longitude="${prefix ?: ''}addresses[${row}].longitude"><i
                        class="icon-map-marker"></i>
                </button>
            </g:if>
        </div>
    </div>
</div>

<div class="control-group visible-extra hide">
    <label class="control-label"><g:message code="crmAddress.longitude.label"/></label>

    <div class="controls">
        <div class="input-append">
            <input type="text" name="${prefix ?: ''}addresses[${row}].longitude"
                   value="${formatNumber(number: bean.longitude, type: 'number', minFractionDigits: 6, maxFractionDigits: 6)}"
                   pattern="[0-9,\\.]+" min="-180" max="180" step="0.000001"
                   class="input-medium" ${disabled ? 'disabled=""' : ''}/>
            <g:if test="${grailsApplication.config.crm.map.google.api.key}">
                <button class="btn btn-map" type="button"
                        data-crm-latitude="${prefix ?: ''}addresses[${row}].latitude"
                        data-crm-longitude="${prefix ?: ''}addresses[${row}].longitude"><i
                        class="icon-map-marker"></i>
                </button>
            </g:if>
        </div>
    </div>
</div>
