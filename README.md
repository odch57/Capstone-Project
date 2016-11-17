# NOTE

Rewrote the app; code repo here: https://github.com/tripleducke/UCITransit

# UCI Transit

An Android transit app for UC Irvine and Anteater Express.  Users can quickly view nearby stops for arrival times, or click into an individual route for additional info on stops.  Each route also has a dedicated route map, with updated bus and stop markers.

## Play Store Link
https://play.google.com/store/apps/details?id=com.robsterthelobster.ucibustracker&hl=en

## Data and Endpoints
The app is actually compatible with a large number of colleges and universities, all using Syncromatics tracking system.  UCI Transit polls the JSON files via Retrofit and parses them for use.  If you wish to use the bus data for another app or bus feed, the endpoints are as such:

* http://www.ucishuttles.com/Region/0/Routes
* http://www.ucishuttles.com/Route/{ROUTE_ID}/Direction/0/Stops
* http://www.ucishuttles.com/Route/{ROUTE_ID}/Stop/{STOP_ID}/Arrivals
* http://www.ucishuttles.com/Route/{ROUTE_ID}/Vehicles

The Intent Service first pulls data from Routes, and uses the route ID to find the stops.  The stop ID is then used to find each individual stop's arrival times.  Vehicle/shuttle data is also pulled from using the route ID.  If these endpoints were to change, they were originally extracted through ucishuttles.com

Depending on the school, the only thing that has to be replaced would be the root url found in <a href="https://github.com/tripleducke/Capstone-Project/blob/master/app/src/main/java/com/robsterthelobster/ucibustracker/data/UciBusIntentService.java">UciBusIntentService.java</a>. The app should then work, displaying the routes and times for the new school. School colors would have also have to be changed in colors.xml.  A sample app using this would be my <a href="https://github.com/tripleducke/BroncoTransit">Bronco Transit</a> app, where http://www.ucishuttles.com is replaced with http://broncoshuttle.com/

##Libraries Used:

* Retrofit - http://square.github.io/retrofit/

* GsonConverter

* Gson

* LocationServices

* GoogleMaps 

* RecyclerView Animators - https://github.com/wasabeef/recyclerview-animators

##License:

UCI Transit is released under the <a href="https://github.com/tripleducke/Capstone-Project/blob/master/LICENSE">MIT License</a>.
