<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?key=${key ?: ''}&v=3&sensor=false"></script>

<r:script>

    function openMap(latitude, longitude, updateLat, updateLong) {
        document.getElementById('map-canvas').innerHTML = '';
        // Setup Google maps to allow user to specify geographical location for this contact.
        var map = new google.maps.Map(document.getElementById('map-canvas'), {
            zoom: 9,
            center: new google.maps.LatLng(latitude, longitude),
            mapTypeId: google.maps.MapTypeId.ROADMAP
        });

        var myMarker = new google.maps.Marker({
            position: new google.maps.LatLng(latitude, longitude),
            draggable: true
        });

        google.maps.event.addListener(myMarker, 'dragend', function(evt){
            $('#mapModal input[name="latitude"]').val("" + evt.latLng.lat().toFixed(6));
            $('#mapModal input[name="longitude"]').val("" + evt.latLng.lng().toFixed(6));
        });

        map.setCenter(myMarker.position);
        myMarker.setMap(map);

        $('#mapModal input[name="latitude"]').val("" + latitude);
        $('#mapModal input[name="longitude"]').val("" + longitude);
        $('#mapModal input[name="updateLat"]').val(updateLat);
        $('#mapModal input[name="updateLong"]').val(updateLong);
        $('#mapModal').modal('show');
    }

    $(document).ready(function() {
        var decimalSeparator = "${new java.text.DecimalFormatSymbols(request.locale ?: Locale.getDefault()).getDecimalSeparator()}";

        $(".btn-map").click(function(event) {
            event.stopPropagation();
            var updateLat = $(this).data('crm-latitude');
            var updateLong = $(this).data('crm-longitude');
            var latitude = $('input[name="' + updateLat + '"]').val();
            var longitude = $('input[name="' + updateLong + '"]').val();
            if(latitude && (decimalSeparator != '.')) {
                latitude = latitude.replace(decimalSeparator, '.');
            } else {
                latitude = "${grailsApplication.config.crm.map.default.latitude ?: 59.326808}";
            }
            if(longitude && (decimalSeparator != '.')) {
                longitude = longitude.replace(decimalSeparator, '.');
            } else {
                longitude = "${grailsApplication.config.crm.map.default.longitude ?: 18.071682}";
            }
            openMap(latitude, longitude, updateLat, updateLong);
            return false;
        });

        $("#mapModal .crm-save").click(function(event) {
            event.stopPropagation();
            var latitude = $('#mapModal input[name="latitude"]').val();
            var longitude = $('#mapModal input[name="longitude"]').val();
            var updateLat = $('#mapModal input[name="updateLat"]').val();
            var updateLong = $('#mapModal input[name="updateLong"]').val();
            if(latitude && (decimalSeparator != '.')) {
                latitude = latitude.replace('.', decimalSeparator);
            }
            if(longitude && (decimalSeparator != '.')) {
                longitude = longitude.replace('.', decimalSeparator);
            }
            $('input[name="' + updateLat + '"]').val(latitude);
            $('input[name="' + updateLong + '"]').val(longitude);
            $('#mapModal').modal('hide');
            return false;
        });
    });

</r:script>

<div class="modal hide" id="mapModal">

    <div class="modal-header">
        <a class="close" data-dismiss="modal">×</a>

        <h3><g:message code="crmContact.edit.location.title" default="Edit location on map"/></h3>
    </div>

    <div class="modal-body">
        <div id="map-canvas"></div>
        <input type="hidden" name="latitude"/>
        <input type="hidden" name="longitude"/>
        <input type="hidden" name="updateLat"/>
        <input type="hidden" name="updateLong"/>
    </div>

    <div class="modal-footer">
        <a href="#" class="btn" data-dismiss="modal"><i class="icon-remove"></i>
            <g:message code="default.button.cancel.label" default="Cancel"/></a>
        <a href="#" class="btn btn-primary crm-save"><i class="icon-ok icon-white"></i>
            <g:message code="default.button.save.label" default="Save"/></a>
    </div>

</div>
