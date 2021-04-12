# WeatherProject
Android Weather Project

<Setup>
1. Get an Open Weather Map API key from https://openweathermap.org/
2. Get a Google Maps API key from https://developers.google.com/maps/documentation/javascript/get-api-key
3. Create the file "api_keys.xml"
4. Add the following to the contents of the api_keys.xml:
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">{insert google maps api key here}</string>
            <string name="open_weather_map_key" templateMergeStrategy="preserve" translatable="false">{insert open weather map api key here}</string>
        </resources>
4. Put the api_keys.xml within the values resource folder of the project.
</Setup>
