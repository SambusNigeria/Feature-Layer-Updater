# Feature-Layer-Updater
In this sample, we demonstrate how you can update feature attributes for layers hosted on ArcgGIS Online using the ArcGIS Android Runtime SDK for Android
# Usage
First add the service url of the layer you want to update in the strings.xml file 

    <!-- Sample Strings -->
    <string name="service_url">PASTE_LAYER_SERVICE_URL_HERE</string>
    
Add your ArcGIS license key in your app level build.gradle file

    buildTypes {
        release {
            buildConfigField("String", "ARCGIS_LICENSE_KEY", '"PASTE_YOUR_KEY_HERE"')
        }
        debug {
            buildConfigField("String", "ARCGIS_LICENSE_KEY", '"PASTE_YOUR_KEY_HERE"')
        }
    }
    
# Attributes
Next study the String fields declared in the MainActivity.java and modify it to match the field names in your hosted layer's attribute table. Here, only the fields to be updated and displayed are referenced.

    private static final String KEY_COUNTRY = "NAME_0";
    private static final String KEY_NAME = "NAME_1";
    private static final String KEY_CONFIRMED = "ConfCases";
    private static final String KEY_ACTIVE = "Active_Cases";
    private static final String KEY_RECOVERED = "Recovery";
    private static final String KEY_DEATHS = "Deaths";

Build your project and run. If your attribute names are correct, you should not have any problem implementing this sample.
Other parts of the project can be tweaked as you wish.
