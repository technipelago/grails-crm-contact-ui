<p>
    <g:message code="crmContact.vcard.qrcode.message" default="Scan this image to add {0} to your address book"
               args="${[crmContact]}"/>
</p>
<qrcode:image height="320" text="${crmContact.vcard}" alt="${crmContact.name}"/>