travelApp.controller('travelController', ['$scope', 'travelService', function ($scope, travelService) {

    $scope.keyword = "";
    $scope.geoCode = "";
    $scope.category = "default";
    $scope.keywordFocus = false;
    $scope.source = "currentLocation";
    $scope.customLocationInput = "";
    $scope.customLocationInputFocus = false;
    $scope.showPlacesList = false;
    $scope.showPlaceDetails = false;
    $scope.currentPlaces=[];
    $scope.pageNumber=0;
    $scope.placeLocation = {};
    $scope.Math = window.Math;
    $scope.mapView = true;
    $scope.travelMode = "DRIVING";
    $scope.mapStartLocation="";
    $scope.reviewType="Google Reviews";
    $scope.reviewSortType="Default Order";
    $scope.googleReviews = [];
    $scope.yelpReviews = [];
    $scope.defaultReviews = {
        google : [],
        yelp : []
    };
    $scope.tempDate = new Date(0);
    $scope.favorites = {};
    $scope.showError = false;
    $scope.showProgress = false;
    $scope.noRecordsHtml = "<div class='row'>" +
        "<div class='col-sm-12' style='background: #ECDDB8;color:#7D7048;height: 30px;display: inline-block;vertical-align: middle'>" +
        "No records." +
        "            </div>\n" +
        "        </div>";
    var map,panorama,directionsDisplay,directionsService;

    travelService.getGeoCode(function (response) {
        $scope.geoCode = {
            lat: response.data.lat,
            lon: response.data.lon
        };
    });

    function initAutoComplete() {
        var defaultBounds = new google.maps.LatLngBounds(
            new google.maps.LatLng(-90, -180),
            new google.maps.LatLng(90, 180));

        var options = {
            bounds: defaultBounds
        };

       new google.maps.places.Autocomplete(document.getElementById('mapStartLocation'), options);
       new google.maps.places.Autocomplete(document.getElementById('customLocationInput'), options);
    }

    initAutoComplete();

    function getPlaces() {
        $scope.showProgress = true;
        var radius = (($scope.distance === undefined) ? 10 : $scope.distance)* 1609.34;
        var keyWordLocation = $scope.keyword.trim().replace(/ /g, '+');
        var category = ($scope.category === "default") ? undefined : $scope.category;
        travelService.getPlaces($scope.geoCode, radius, keyWordLocation, category, function (response) {
            response = response.data;
            $scope.places = response.places;
            $scope.currentPlaces = $scope.places;
            $scope.next_page_token = [response.next_page_token];
            $scope.showProgress = false;
            $scope.showPlacesList = true;
            $scope.pageNumber = 0;
        }, function (error) {
            console.log("Error in getting places",error);
        });
    }

    $scope.formSubmit = function () {
        resetPlaceDetails();
        $scope.showPlacesList = false;
        $scope.showPlaceDetails = false;
        $scope.showError = false;
        if($scope.source === "customLocation"){
            var location = $scope.customLocationInput.trim().replace(/ /g, '+');
            travelService.getCustomLocationGeoCode(location,function (response) {
                if(response.data.lat == null || response.data.lon == null){
                    $scope.showError = true;
                }else{
                    $scope.geoCode = response.data;
                    getPlaces();
                }
            },function (error) {
                console.log("Error in getting custom location geoCode",error);
            })
        }else{
            getPlaces();
        }
    };

    $scope.nextPage = function () {
        if($scope.places.length > 20*($scope.pageNumber + 1)){
            $scope.currentPlaces = $scope.places.slice(20*($scope.pageNumber + 1),20*($scope.pageNumber + 2));
            $scope.pageNumber += 1;
        }else{
            travelService.getPaginationPlaces($scope.next_page_token[$scope.pageNumber],function (response) {
                response = response.data;
                $scope.places = $scope.places.concat(response.places);
                $scope.currentPlaces = response.places;
                $scope.next_page_token.push(response.next_page_token);
                $scope.pageNumber += 1;
            },function (error) {
                console.log("Error in getting next page places",error);
            });
        }
    };

    $scope.previousPage = function () {
        $scope.currentPlaces = $scope.places.slice(($scope.pageNumber-1)*20,$scope.pageNumber*20);
        $scope.pageNumber -=1;
    };

    function initMap(){
        directionsService = new google.maps.DirectionsService();
        directionsDisplay = new google.maps.DirectionsRenderer();
        directionsDisplay.setPanel(document.getElementById('directionsPanel'));
        map = new google.maps.Map(document.getElementById('map'), {
            center: $scope.placeLocation,
            zoom: 15
        });
        new google.maps.Marker({
            position: $scope.placeLocation,
            map: map
        });
    }

    $scope.showDetailsTab = function (place) {
      $scope.showPlacesList = false;
      $scope.placeInDetailsTab = place;
      $scope.placeLocation = place.geometry.location;
      initMap();
      var service = new google.maps.places.PlacesService(map);
      var request = {placeId: place.place_id};
      var callback = function (placeDetails, status) {
          if(status === google.maps.places.PlacesServiceStatus.OK){
              $scope.placeDetails = placeDetails;
              console.log(placeDetails);
              $scope.googleReviews = $scope.placeDetails.reviews === undefined ? [] : $scope.placeDetails.reviews ;
              setFormalDate();
              $scope.defaultReviews.google = $scope.googleReviews.slice(0);
              setYelpReviews($scope.placeDetails);
              $scope.showPlaceDetails = true;
              $scope.$apply();
          }
      };
      service.getDetails(request,callback);
    };

    function setFormalDate(){
        for(var i=0 ; i < $scope.googleReviews.length; ++i){
            var date = new Date(0);
            date.setUTCSeconds($scope.googleReviews[i].time);
            $scope.googleReviews[i].formatDate = date;
        }
        if($scope.placeDetails.opening_hours !== undefined){
            var today = new Date().getDay();
            today = (today-1)%7;
            console.log($scope.placeDetails.opening_hours.weekday_text.length);
            var temp = $scope.placeDetails.opening_hours.weekday_text.slice(0);
            $scope.placeDetails.opening_hours.weekday_text = [].concat(temp.slice(today)).concat(temp.slice(0,today));
            console.log($scope.placeDetails.opening_hours.weekday_text);
            console.log($scope.placeDetails.opening_hours.weekday_text.length);
        }
    }

    function setYelpReviews(placeDetails){
        var country="";
        var state="";
        var city="";
        for(var i =0; i<placeDetails.address_components.length; ++i){
            var component = placeDetails.address_components[i];
            if(component.types.includes("country")){
                country = component.short_name;
            }
            if(component.types.includes("administrative_area_level_1")){
                state = component.short_name;
            }
            if(component.types.includes("administrative_area_level_2")){
                city = component.short_name;
            }
        }
        travelService.getYelpReviews(placeDetails.name,placeDetails.formatted_address.split(',')[0],city,state,country,function (response) {
            response = response.data;
            $scope.yelpReviews = response;
            $scope.defaultReviews.yelp = $scope.yelpReviews.slice(0);
        },function (error) {
            console.log("Error in getting yelp reviews", error)
        })
    }

    $scope.showListTab = function () {
      $scope.showPlaceDetails = false;
      $scope.showPlacesList = true;
    };

    $scope.switchDetailsTab = function () {
        $scope.showPlacesList = false;
        $scope.showPlaceDetails = true;
    };

    $scope.getRange = function (range) {
        try{
            return new Array(range);
        }catch (e){
        }
    };

    $scope.getTotalRows = function(length){
        return window.Math.ceil(length/4.0);
    };

    $scope.getTotalColumns = function(length){
        return (length<4) ? length : 4;
    };

    $scope.getDay = function () {
        return new Date().getDay();
    };

    $scope.changeMapView = function () {
        if($scope.mapView){
            var mapOptions = {
                zoom: 15,
                center: $scope.placeLocation
            };
            new google.maps.Map(document.getElementById('streetMap'), mapOptions);
            panorama = new google.maps.StreetViewPanorama(
                document.getElementById('streetMap'), {
                    position: $scope.placeLocation,
                    pov: {
                        heading: 34,
                        pitch: 10
                    }
                });
            map.setStreetView(panorama);
            $scope.mapView = false;
        }else{
            $scope.mapView = true;
        }
    };

    function renderDirections(center){
        var mapOptions = {
            zoom: 15,
            center: center
        };
        map = new google.maps.Map(document.getElementById('map'), mapOptions);
        directionsDisplay.setMap(map);
    }
    
    $scope.calcRoute = function () {
        var origin;
        if($scope.mapStartLocation === "My Location" || $scope.mapStartLocation === $scope.customLocationInput){
            origin = {
                lat: $scope.geoCode.lat,
                lng: $scope.geoCode.lon
            };
        }else{
            origin = $scope.mapStartLocation.trim().replace(/ /g, '+');
        }
        var request = {
            origin: origin,
            destination: $scope.placeLocation,
            travelMode: $scope.travelMode,
            provideRouteAlternatives: true
        };
        directionsService.route(request, function (result, status) {
            if (status === 'OK') {
                renderDirections($scope.placeLocation);
                directionsDisplay.setDirections(result);
            }
        });
    };

    $scope.sortReviews = function(key){
        $scope.reviewSortType = key;
        if(key==='Default Order'){
            $scope.googleReviews = $scope.defaultReviews.google.slice(0);
            $scope.yelpReviews = $scope.defaultReviews.yelp.slice(0);
        }
        else if(key === 'Highest Rating'){
            $scope.googleReviews = sortArray($scope.googleReviews,'rating',true);
            $scope.yelpReviews = sortArray($scope.yelpReviews,'rating',true);
        }
        else if(key === 'Lowest Rating'){
            $scope.googleReviews = sortArray($scope.googleReviews,'rating',false);
            $scope.yelpReviews = sortArray($scope.yelpReviews,'rating',false);
        }
        else if(key === 'Most Recent'){
            $scope.googleReviews = sortArray($scope.googleReviews,'time',true);
            $scope.yelpReviews = sortArray($scope.yelpReviews,'time_created',true);
        }
        else if(key === 'Least Recent'){
            $scope.googleReviews = sortArray($scope.googleReviews,'time',false);
            $scope.yelpReviews = sortArray($scope.yelpReviews,'time_created',false);
        }
    };

    function sortArray(arr, property, flag) {
        return arr.sort(function (a, b) {
            var x = a[property];
            var y = b[property];
            if (flag)
                return ((x > y) ? -1 : ((x < y) ? 1 : 0));
            else
                return ((x < y) ? -1 : ((x > y) ? 1 : 0));
        });
    }

    $scope.toggleFavorites = function (place) {
        if($scope.favorites.hasOwnProperty(place.place_id )){
            $scope.removeFromFavorites(place);
        }else{
            $scope.addToFavorites(place);
        }
    };

    $scope.addToFavorites = function(place) {
        $scope.favorites[place.place_id] = place;
    };

    $scope.removeFromFavorites = function (place) {
        delete $scope.favorites[place.place_id];
    };
    $scope.getFavoritesSize= function(){
        var size =0;
        for(var key in $scope.favorites){
            size++;
        }
        return size;
    };

    $scope.clear =function () {
        resetForm();
        resetPlaceDetails();
        $scope.showPlacesList = false;
        $scope.showPlaceDetails = false;
        $scope.showError = false;
    };

    function resetPlaceDetails(){
        $scope.places = [];
        $scope.currentPlaces = [];
        $scope.placeDetails = undefined;
    }

    function resetForm() {
        $scope.keyword = "";
        $scope.keywordFocus = false;
        $scope.source = "currentLocation";
        $scope.customLocationInput = "";
        $scope.customLocationInputFocus = false;
        $scope.distance = undefined;
        $scope.category = "default";
    }

    $scope.postToTwitter = function () {
        var tweet="https://twitter.com/intent/tweet?text=Check out "+$scope.placeDetails.name+" Located at "+$scope.placeDetails.formatted_address+". Website:  "+$scope.placeDetails.website+" . "+"TravelAndEntertainmentSearch";
        window.open(tweet, "twitter", "height=600,width=600");
    }
}]);