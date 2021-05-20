# Setup
1. Get an Open Weather Map API key from https://openweathermap.org/
2. Get a Google Maps API key from https://developers.google.com/maps/documentation/javascript/get-api-key (optional)
   - This is to see the precipitation tiles over Google Maps
3. Create the file "api_keys.xml"
4. Add the following to the contents of the api_keys.xml:
 ```xml
 <resources>
     <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">{insert google maps api key here}</string>
     <string name="open_weather_map_key" templateMergeStrategy="preserve" translatable="false">{insert open weather map api key here}</string>
 </resources>
 ```
5. Put the api_keys.xml within the values resource folder of the project.
6. Wait between 10 minutes to 2 hours for the Open Weather Map API key to activate upon succesful registration before running the app.
