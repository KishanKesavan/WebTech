var travelApp = angular.module('travelApp',['ngAnimate']);

travelApp.service("travelService",['$http',function($http){
    var url = "https://travel-200920.appspot.com";
    var getGeoCode = function (success) {
        $http.get("http://ip-api.com/json").then(success);
    };
    var getPlaces = function (geoCode, radius, keyWord, category, success, failure) {
        $http.get(url+"/places/"+geoCode.lat+"/"+geoCode.lon+"/"+radius+"/"+keyWord+"/"+category).then(success,failure);
    };
    var getPaginationPlaces = function(pageToken,success,failure){
        $http.get(url+"/places/pageToken/"+pageToken).then(success,failure);
    };
    var getCustomLocationGeoCode = function (location,success,failure) {
        $http.get(url+"/geoCode/customLocation/"+location).then(success,failure);
    };
    var getYelpReviews = function(name,address1,city,state,country,success,failure){
        $http.get(url+"/reviews/"+name+"/"+address1+"/"+city+"/"+state+"/"+country).then(success,failure);
    };
    return {
        getGeoCode : getGeoCode,
        getPlaces : getPlaces,
        getPaginationPlaces : getPaginationPlaces,
        getCustomLocationGeoCode : getCustomLocationGeoCode,
        getYelpReviews : getYelpReviews
    };
}]);
