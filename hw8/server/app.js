const express = require('express');
const request = require('request');
const cors = require('cors');
const app = express();
app.use(cors());

/*Client ID
mSx5jRQT1z1gJznIQR7xdA

API Key
QyLAxX4rOycms8aGtmrCXnlakHYjkk82iYhkeDHu3nnyEOya8kAiKNjgQ4-lCXtX2QPbIm1AZTXQhSC2vueA9xgFM4-yrKlsIFhBUHXhJUfVA57JuWElufhXl5XOWnYx*/

var placesKey = "AIzaSyB9syddmfdLtCW6th5dedBvvi-Ka_zmYmw";
var geoCodeKey = "AIzaSyDhm9vuozFuJ-YqMgX1UTkOk0QND4hkL3E";
var yelpKey="QyLAxX4rOycms8aGtmrCXnlakHYjkk82iYhkeDHu3nnyEOya8kAiKNjgQ4-lCXtX2QPbIm1AZTXQhSC2vueA9xgFM4-yrKlsIFhBUHXhJUfVA57JuWElufhXl5XOWnYx";

app.get('/', function (req, res) {
    res.send("TravelService");
});
app.listen(3000, function () {
    console.log("Starting")
});

app.get('/places/:lat/:long/:radius/:keyword/:category', function (req, res) {
    var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + req.params.lat + "," + req.params.long + "&radius=" + req.params.radius + "&keyword=" + req.params.keyword + "&key=" + placesKey;
    if(req.params.category !== "undefined"){
        url+="&type="+req.params.category;
    }
    getPlaces(url, res);
});

app.get('/places/pageToken/:pageToken', function (req, res) {
    var url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + placesKey + "&pagetoken=" + req.params.pageToken;
    getPlaces(url, res);
});

function getPlaces(url, res) {
    request(url, function (error, response, body) {
        var result = JSON.parse(response.body);
        var resultObj = {};
        resultObj.places = result.results;
        if ('next_page_token' in result) {
            resultObj.next_page_token = result.next_page_token;
        } else {
            resultObj.next_page_token = null;
        }
        res.send(resultObj)
    });

}

app.get('/geoCode/customLocation/:location', function (req, res) {
    var url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + req.params.location + "&key=" + geoCodeKey;
    request(url, function (error, response, body) {
        var result = JSON.parse(response.body);
        if (result.results.length === 0) {
            res.send({lat: null, lon: null});
        } else {
            var geoLocation = result["results"][0]["geometry"]["location"];
            res.send({lat: geoLocation.lat, lon: geoLocation.lng});
        }
    })
});

app.get('/reviews/:name/:address1/:city/:state/:country',function (req,res) {
   var options ={
       url : "https://api.yelp.com/v3/businesses/matches/best?name="+req.params.name+"&address1="+req.params.address1+"&city="+req.params.city+"&state="+req.params.state+"&country="+req.params.country,
       headers : {
           'Authorization' : 'Bearer '+yelpKey
       }
   };
   request(options,function(error, response,body){
      var result= JSON.parse(response.body);
      var reviews = [];
      if(result.businesses.length === 1){
          options.url = "https://api.yelp.com/v3/businesses/"+result.businesses[0].id+"/reviews";
          request(options,function(e,r,b){
             var reviewResult = JSON.parse(r.body);
             if(reviewResult.total <= 5){
                reviews = reviewResult.reviews;
             }else{
                 reviews = reviewResult.reviews.splice(0,5);
             }
             res.send(reviews);
          });
      }else{
            res.send(reviews);
      }
   })
});