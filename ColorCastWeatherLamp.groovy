/**
 *  ColorCast Weather Lamp
 *
 *  Inspired by and based in large part on the original Color Changing Smart Weather lamp by Jim Kohlenberger and subsequently Joe DiBenedetto.
 *  See Jim's original SmartApp at http://community.smartthings.com/t/color-changing-smart-weather-lamp-app/12046 which includes an option for high pollen notifications
 *  
 *  This weather lantern app turns a lamp different colors based on the weather.     
 *  It uses OpenWeather API to micro-target weather. 
 *
 *  With special thanks to insights from the SmartThings Hue mood lighting script and the light on motion script by kennyyork@centralite.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
import java.util.regex.*

definition(
    name: "ColorCast Weather Lamp",
    namespace: "",
    author: "David Meck",
    description: "Get a simple visual indicator of the days weather before you leave home. ColorCast will change the color of one or lights to match the weather forecast whenever it's triggered.",
    category: "Convenience",
    iconUrl: "https://img.icons8.com/color/48/000000/rgb-circle-1--v1.png",
    iconX2Url: "https://img.icons8.com/color/96/000000/rgb-circle-1--v1.png",
    iconX3Url: "https://img.icons8.com/color/96/000000/rgb-circle-1--v1.png"
)

preferences {
    page(name: "pageMain")
    page(name: "pageAPI")
    page(name: "pageDisplayTriggers")
    page(name: "pageWeatherTriggers")
    page(name: "pageLightSettings")
    page(name: "pageUnderTheHood")
    page(name: "pageColorDefinitions")
}

def  pageMain() {
    dynamicPage(
        name: "pageMain", 
        title: "App Settings", 
        install: true, 
        uninstall: true
    ) {
    
        state.hslWhite  = [0, 0]
        state.hslBlue   = [68, 100]
        state.hslAqua   = [56, 100]
        state.hslTeal   = [45, 100]
        state.hslGreen  = [36, 100]
        state.hslLime   = [22, 100]
        state.hslYellow = [14, 100]
        state.hslOrange = [10, 100]
        state.hslRed    = [0, 100]
        state.hslPurple = [78, 100]
        state.hslMagenta = [88, 100]
        state.hslPink   = [100, 60]
        state.colorList = ["White","Blue","Aqua","Teal","Green","Lime","Yellow","Orange","Red","Purple","Magenta","Pink"]
        

        section("App Status") {
            input (
                name:           "enabled", 
                type:           "bool", 
                title:          "Enabled",
                required:       false,
                defaultValue:   true
            )
            
        }
        
        section (
            title:      "API Setup",
            hideable:   isValidApiKey() ? true : false,
            hidden:     isValidApiKey() ? true : false
        ) {         
            href(
                title:          "OpenWeather API Key",
                name:           "hrefApi", 
                page:           "pageAPI",
                description:    getKey(),
                required:       !isValidApiKey(),
                state:          isValidApiKey() ? "complete" : ""
            )
        }
        
        section("When to display") {
            href(
                title:          "Display weather when...",
                name:           "toPageDisplayTriggers", 
                page:           "pageDisplayTriggers",
                description:    getDisplayTriggers(),
                required:       !isValidDisplayTriggers(),
                state:          isValidDisplayTriggers() ? "complete" : ""
            )
        }
        
        section("What to display") {
            href(
                title:          getForecastTitle(),
                name:           "toPageWeatherTriggers", 
                page:           "pageWeatherTriggers",
                description:    getWeatherTriggers(),
                required:       !isValidWeatherTriggers(),
                state:          isValidWeatherTriggers() ? "complete" : ""
            )
        }
        
        section("Where to display") {
            href(
                title:          getLightSettingsTitle(),
                name:           "toPageLightSettings", 
                page:           "pageLightSettings",
                description:    getLightSettings(),
                required:       !isValidLights(),
                state:          isValidLights() ? "complete" : ""
            )
        }

        section("Additional Settings", mobileOnly:true) {
            label ( //Allow custom name for app. Usefull if the app is installed multiple times for different modes
                title:      "Assign a name",
                required:   false
            )
            mode ( //Allow app to be assigned to different modes. Usefull if user wants different setting for different modes
                name:       "modeName",
                title:      "Set for specific mode(s)",
                required:   false
            )
        }
        
        section("Under the hood") {
            href(
                title:          "Advanced options",
                name:           "toPageUnderTheHood", 
                page:           "pageUnderTheHood",
                description:    "",
                required:       false
            )
        }

    }
}
def pageAPI() {
    dynamicPage(
        name: "pageAPI", 
        title: "API Key", 
        install: false, 
        uninstall: false
    ) {
    
        section("OpenWeather") {
            paragraph "To use this SmartApp, you need an API Key from OpenWeather (https://openweathermap.org)."
        }
    
        section("API Key") {
            input (
                name:           "apiKey", 
                type:           "text", 
                title:          "Enter your new key",
                required:       true,
                defaultValue:   ""
            )
        }
    }
}
def pageDisplayTriggers() {
    dynamicPage(
        name: "pageDisplayTriggers",
        install: false, 
        uninstall: false
    ) {
        
        section("Display weather when...") { //Select motion sensor(s). Optional because app can be triggered manually
            
            input (
                name:           "motion_detector", 
                type:           "capability.motionSensor", 
                title:          "Motion is detected",
                multiple:       true,
                required:       false
            )
            
            input (
                name:           "contact", 
                type:           "capability.contactSensor", 
                title:          "Door is opened", 
                multiple:       true,
                required:       false
            )
        }
        
        section("Always On") {
            paragraph "When enabled, the light(s) will remain on continuosly while the app is active. If more than one weather condition is met, the light(s) will cycle through all applicable colors.\n\nWeather alerts will be displayed as a separate state when this option is enabled."
            input (
                name:           "alwaysOn", 
                type:           "bool", 
                title:          "Always on",
                required:       false,
                defaultValue:   false
            )
            
            input (
                name:           "alwaysOnSwitch",
                type:           "capability.switch",
                title:          "Optionally, only while this switch is turned on",
                required:       false,
                multiple:       false
            )
        }

    }
}
def pageWeatherTriggers() {
    dynamicPage(
        name: "pageWeatherTriggers", 
        title: "Set Weather Triggers", 
        install: false, 
        uninstall: false
    ) {

        section ("Forecast Range") {
            // Get the number of hours to look ahead. Weather for the next x hours will be parsed to compare against user specified values.
            input (
                name:           "forecastRange", 
                type:           "enum", 
                title:          "Forecast Range (Note: hourly ranges will include current conditions.)",
                defaultValue:   "Current conditions",
                options: [
                    "Current conditions", 
                    "1 Hour",
                    "2 Hours",
                    "4 Hours",
                    "8 Hours",
                    "12 Hours",
                    "16 Hours",
                    "24 Hours"
                ],
                required:       true
            )
        }
        
        section (
            title:      "All Clear",
            hideable:   true,
            hidden:     ((settings.allClearEnabled instanceof Boolean) && !settings.allClearEnabled) ? true : false
        ) {         
                input (
                name:           "allClearEnabled", 
                type:           "bool", 
                title:          "Enabled",
                defaultValue:   true,
                required:       false
            )
        
            input (
                name:           "allClearColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Lime",
                required:       false,
                multiple:       false
            )            
        }
        
        section (
            title:      "Low Temperature",
            hideable:   true,
            hidden:     ((settings.lowTempEnabled instanceof Boolean) && !settings.lowTempEnabled) ? true : false
        ) {
            input (
                name:           "lowTempEnabled", 
                type:           "bool", 
                title:          "Enabled",
                defaultValue:   true,
                required:       false
            )
            input (
                name:           "tempMinTrigger", 
                type:           "number", 
                title:          "Low Temperature - °F",
                defaultValue:   35,
                range:          "-20..120",
                required:       true
            )
            input (
                name:           "tempMinType", 
                type:           "enum", 
                title:          "Temperature Type",
                defaultValue:   "Actual",
                options: [
                    "Actual",
                    "Feels like"
                ],
                required:       true
            )
            input (
                name:           "tempMinColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Blue",
                required:       true,
                multiple:       false
            )
        }
        
        section (
            title:      "High Temperature",
            hideable:   true,
            hidden:     ((settings.highTempEnabled instanceof Boolean) && !settings.highTempEnabled) ? true : false
        ) {
            input (
                name:           "highTempEnabled", 
                type:           "bool", 
                title:          "Enabled",
                defaultValue:   true,
                required:       false
            )
            input (
                name:           "tempMaxTrigger", 
                type:           "number", 
                title:          "High Temperature - °F",
                defaultValue:   80,
                range:          "-20..120",
                required:       true
            )
            input (
                name:           "tempMaxType", 
                type:           "enum", 
                title:          "Temperature Type",
                defaultValue:   "Actual",
                options: [
                    "Actual",
                    "Feels like"
                ],
                required:       true
            )
            input (
                name:           "tempMaxColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Orange",
                required:       true,
                multiple:       false
            )
        }
        
        section (
            title:      "Rain",
            hideable:   true,
            hidden:     ((settings.rainEnabled instanceof Boolean) && !settings.rainEnabled) ? true : false
        ) {
            input (
                name:           "rainEnabled", 
                type:           "bool", 
                title:          "Enabled",
                defaultValue:   true,
                required:       false
            )
            input (
                name:           "rainColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Aqua",
                required:       true,
                multiple:       false
            )
        }
        
        section (
            title:      "Snow",
            hideable:   true,
            hidden:     ((settings.snowEnabled instanceof Boolean) && !settings.snowEnabled) ? true : false
        ) {
            input (
                name:           "snowEnabled", 
                type:           "bool", 
                title:          "Enabled",
                defaultValue:   false,
                required:       false
            )
            input (
                name:           "snowColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Purple",
                required:       true,
                multiple:       false
            )
        }
        
        section (
            title:      "Sleet\r\n(applies to freezing rain, ice pellets, wintery mix, or hail)",
            hideable:   true,
            hidden:     ((settings.sleetEnabled instanceof Boolean) && !settings.sleetEnabled) ? true : false
        ) {
            input (
                name:           "sleetEnabled", 
                type:           "bool", 
                title:          "Enabled",
                defaultValue:   false,
                required:       false
            )
            input (
                name:           "sleetColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Magenta",
                required:       true,
                multiple:       false
            )
        }
        
        section (
            title:      "Cloudy",
            hideable:   true,
            hidden:     ((settings.cloudyEnabled instanceof Boolean) && !settings.cloudyEnabled) ? true : false
        ) {
            input (
                name:           "cloudyEnabled", 
                type:           "bool", 
                title:          "Enabled",
                defaultValue:   false,
                required:       false
            )
            input (
                name:           "cloudPercentTrigger", 
                type:           "number", 
                title:          "Cloud Cover %",
                defaultValue:   50,
                range:          "1..100",
                required:       true
            )
            input (
                name:           "cloudPercentColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Teal",
                required:       true,
                multiple:       false
            )
        }
        
        section (
            title:      "Dew Point\r\n(Sometimes refered to as humidity)",
            hideable:   true,
            hidden:     ((settings.dewPointEnabled instanceof Boolean) && !settings.dewPointEnabled) ? true : false
        ) {
            input (
                name:           "dewPointEnabled", 
                type:           "bool", 
                title:          "Enabled",
                defaultValue:   false,
                required:       false
            )
            input (
                name:           "dewPointTrigger", 
                type:           "number", 
                title:          "Dew Point - °F",
                defaultValue:   65,
                range:          "50..120",
                required:       true
            )
            input (
                name:           "dewPointColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Green",
                required:       true,
                multiple:       false
            )
            href (
                name: "hrefNotRequired",
                title: "Learn more about \"Dew Point\"",
                required: false,
                style: "external",
                url: "http://www.washingtonpost.com/blogs/capital-weather-gang/wp/2013/07/08/weather-weenies-prefer-dew-point-over-relative-humidity-and-you-should-too/",
                description: "A Dew Point above 65° is generally considered \"muggy\"\r\nTap here to learn more about dew point"
            )
        }
        
        section (
            title:      "Wind",
            hideable:   true,
            hidden:     ((settings.windEnabled instanceof Boolean) && !settings.windEnabled) ? true : false
        ) {
            input (
                name:           "windEnabled", 
                type:           "bool", 
                title:          "Enabled",
                defaultValue:   false,
                required:       false
            )
            input (
                name:           "windTrigger", 
                type:           "number", 
                title:          "High Wind Speed",
                defaultValue:   24,
                range:          "1..100",
                required:       true
            )
            input (
                name:           "windColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Yellow",
                required:       true,
                multiple:       false
            )
        }

        section ("Weather Alerts") {     
            input (
                name:           "alertFlash", 
                type:           "enum", 
                title:          "Show alerts for...",
                options:        [
                    "warning":"Warnings", 
                    "watch":"Watches", 
                    "advisory":"Advisories"
                ],
                required:       false,
                multiple:       true
            )
            input (
                name:           "alertColor", 
                type:           "enum", 
                title:          "Color",
                options:        state.colorList,
                defaultValue:   "Red",
                required:       true,
                multiple:       false
            )
            input (
            	name:			"flashEnabled",
                type:           "bool", 
                title:          "Flash with White",
                defaultValue:   false,
                required:       false
            )
        }    
    }
}
def pageLightSettings() {
    dynamicPage(
        name: "pageLightSettings", 
        title: "Set up lights", 
        install: false, 
        uninstall: false
    ) {
        section("Control these bulbs...") {
            input ( //Select bulbs
                name:           "hues", 
                type:           "capability.colorControl", 
                title:          "Select bulbs?",
                required:       true,
                multiple:       true
            )
            input ( //Select brightness
                name:           "brightnessLevel", 
                type:           "number", 
                title:          "Brightness Level (1-100)?",
                required:       false,
                range:          "1..100",
                defaultValue:   100
            )
            paragraph   "Do you want to set the light(s) back to the color/level they were at before the weather was displayed? Due to the way SmartThings polls devices this may not always work as expected."
            input (
                name:           "rememberLevel", 
                type:           "bool", 
                title:          "Remember light settings",
                required:       false,
                defaultValue:   true
            )
        }
    
    }
}
def pageUnderTheHood() {
    dynamicPage(
        name: "pageUnderTheHood", 
        title: "Advanced options", 
        install: false, 
        uninstall: false
    ) {     
        section("Color Definitions") {
            paragraph "Set hue and saturation for each color. Different lights display colors differently so use this section to fine tune the color definitions if your colors don't look right."
            
            for (i in state.colorList) {
            debug(i)
                href(
                    title:          i,
                    name:           "topageColorDefinitions"+i, 
                    page:           "pageColorDefinitions",
                    description:    getColorDefinitionsLabel(i),
                    params:         [color: i]
                )
            }
        }
        section("Debug Mode") {
            paragraph "Enabling debug mode will cause select debug messages to be sent to the notifications section of the app."
            input (
                name:           "debugMode", 
                type:           "bool", 
                title:          "Debug Mode",
                required:       false,
                defaultValue:   false
            )       
        }

    }
}

def pageColorDefinitions(params) {
    dynamicPage(
        name: "pageColorDefinitions", 
        title: params.color + " Color Definition", 
        install: false, 
        uninstall: false,
        required: true
    ) {
    
        def defaultValues = getColorDefinitions(params.color);

        section ("Hue - default " + state["hsl"+params.color][0]) {
            paragraph  "Hue is a numeric representation of the position of a color on the chart below starting at red with a value of 0 and ending back at red with a value of 100."
            
            input (
                name:           "color_hue[${params.color}]",
                type:           "number", 
                title:          "Hue (0-100)?",
                required:       true,
                range:          "0..100",
                defaultValue:   defaultValues[0]
            )
        }
        
        section ("Saturation - default " + state["hsl"+params.color][1]) {
            paragraph "Saturation is the intensity of a color from 0% to 100%. At 0%, hue is meaningless as all hues would be completely desaturated and become white. 100% would be the deepest color saturation for the selected hue."
            input (
                name:           "color_saturation[${params.color}]",
                type:           "number", 
                title:          "Saturation (0-100)?",
                required:       true,
                range:          "0..100",
                defaultValue:   defaultValues[1]
            )
        }
        
        section ("Brightness") {
            paragraph "Brightness is a global setting that can be changed on the \"Light selection\" page."
        }
    }
}

def getColorDefinitions(colorName) {
    def hsl = []
    
    
    if (settings."color_hue[${colorName}]" instanceof Integer) {
        hsl.push(settings."color_hue[${colorName}]")
        hsl.push(settings."color_saturation[${colorName}]")
    } else {
        hsl.push(state["hsl"+colorName][0])
        hsl.push(state["hsl"+colorName][1])
    }

    return hsl
}

def getColorDefinitionsLabel(colorName) {
    
    def hsl = getColorDefinitions(colorName)
    
    return "Hue: " + hsl[0] + "\nSaturation: " + hsl[1]
}

def getKey() {
    if (apiKey instanceof String) {
        return apiKey
    } else {
        return "Create/Enter API Key"
    }
}

def getDisplayTriggers() {
    def output = ""
    if (isValidDisplayTriggers()) {
        if (alwaysOn) {
            output += "Weather is displayed continuously."
        } else {
            if (motion_detector instanceof Object) {
                if (output != "") output += "\n"
                output += "Motion is detected:\n"
                motion_detector.eachWithIndex { it, i -> 
                    if (i > 0) output += "\n"
                    output += "  " + it.displayName
                }
            }
            if (contact instanceof Object) {
                if (output != "") output += "\n"
                output += "Door is opened:\n"
                contact.eachWithIndex { it, i -> 
                    if (i > 0) output += "\n"
                    output += "  " + it.displayName
                }
            }
        }
    } else {
        output = "Choose when to display weather"
    }
    return output
}

def getWeatherTriggers() {
    def outputList = []
    if (isValidWeatherTriggers()) {
        
        if (allClearEnabled) {
            outputList.add(allClearColor + "\t- all clear")
        }
        
        if (lowTempEnabled) {
            outputList.add(tempMinColor + "\t- temperature " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + tempMinTrigger + "° or below")
        }
        
        if (highTempEnabled) {
            outputList.add(tempMaxColor + "\t- temperature " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + tempMaxTrigger + "° or above")
        }
        
        if (rainEnabled) {
            outputList.add(rainColor + "\t- it's raining")
        }
        if (snowEnabled) {
            outputList.add(snowColor + "\t- it's snowing")
        }
        
        if (sleetEnabled) {
            if (forecastRange == "Current conditions") {
                outputList.add(sleetColor + "\t- it's sleeting, hailing, etc.")
            } else {
                outputList.add(sleetColor + "\t- sleet, hail, etc is expected")
            }
        }
        
        if (cloudyEnabled) {
            outputList.add(cloudPercentColor + "\t- cloud cover " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + cloudPercentTrigger + "% or above")
        }
        
        if (dewPointEnabled) {
            outputList.add(dewPointColor + "\t- dew point " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + dewPointTrigger + "°+")
        }
        
        if (windEnabled) {
            outputList.add(windColor + "\t- wind " + ((forecastRange == "Current conditions") ? "is" : "will be") + " " + windTrigger + "mph or greater")
        }
        
        if ((alertFlash instanceof Object) && alertFlash.size() > 0) {
            def alertoutputList = []
            alertFlash.each{ //Iterate through all user specified alert types
                
                switch (it) {
                    case "warning":
                        alertoutputList.add("Warnings")
                        break
                    case "watch":
                        alertoutputList.add("Watches")
                        break
                    case "advisory":
                        alertoutputList.add("Advisories")
                        break
                }
                
            }
            
            if (alertoutputList.size() > 0)
            {
                def j = 0;
                def alertoutput = ""
                alertoutputList.each {
                    if (j++ > 0) alertoutput += ", "
                    alertoutput += it
                }
                
                if (flashEnabled) {
                	alertoutput = "Alert " + alertColor + " flashing with White - " + alertoutput
                }
                else {
                	alertoutput = "Alert " + alertColor + " - " + alertoutput
                }
                outputList.add(alertoutput)
            }
        }
    } else {
        outputList.add("Choose weather conditions to display")
    }
    
    def output = ""
    
    def i = 0;
    outputList.each {
        if (i++ > 0) output += "; "
        output += it
    }
    
    return output
    
}

def getLightSettings() {
    def output = ""
    
    if (hues instanceof Object) {
        hues.eachWithIndex { it, i -> 
            if (i > 0) output += "\n"
            output += it.displayName
        }
    } else {
        output = "No lights selected"
    }
    return output
}

def getLightSettingsTitle() {
    def output = ""
    if (hues instanceof Object) {
        output = "Use these lights at " + settings.brightnessLevel + "% brightness"
    } else {
        output = "Use these lights..."
    }
    
    return output
 
}

def getForecastTitle() {
    def output = ""
    if (isValidWeatherTriggers()) {
        if (forecastRange != "Current conditions") {
            output = "Display forecast for the next " + forecastRange
        } else {
            output = "Display the current weather conditions"
        }
    } else {
        output = "Display forecast for..."
    }
    
    return output
}

def isValidApiKey() {
    if (apiKey instanceof String) {
        return true
    } else {
        return false
    }
}

def isValidLights() {
    if (hues instanceof Object) {
        return true
    } else {
        return false
    }
}

def isValidDisplayTriggers() {
    if (motion_detector instanceof Object || contact instanceof Object || alwaysOn) {
        return true
    } else {
        return false
    }
}

def isValidWeatherTriggers() {
    if (allClearEnabled || lowTempEnabled || highTempEnabled || rainEnabled || snowEnabled || sleetEnabled || cloudyEnabled || dewPointEnabled || windEnabled) {
        return true
    } else {
        return false
    }
}

def installed() {
    debug "Installed with settings: ${settings}"
    initialize()
}

def initialize() {

    if (enabled) {

        state.current = []
        state.colors = []
        state.isDisplaying = false
        
        schedule("0 0/5 * * * ?", getWeather)

        if (!alwaysOn) {
            getWeather()
            if (motion_detector != null) subscribe(motion_detector, "motion", motionHandler)
            if (contact != null) subscribe(contact, "contact", contactHandler)
        } else {
            if (alwaysOnSwitch != null) subscribe(alwaysOnSwitch, "switch", switchHandler)
            state.colorIndex = 0
            getWeather(true)
            state.isDisplaying = false
            subscribe(location, modeChangeHandler)
        }
    } else {
        debug ("App is disabled. Forecast will not be displayed.", true)
        if (alwaysOn) hues.off()
    }
}

def modeChangeHandler(evt) {
    updated()
}

def updated() {
    debug "Updated with settings: ${settings}"
    unsubscribe()
    try{unschedule()} catch(err) {debug("-----Unschedule failed")}
    initialize()
}

def alwaysOnDisplay() {
    debug ("Running Always On")
    debug ('state.colors.size: ' + state.colors.size())
    debug ('state.colorIndex: ' + state.colorIndex)
    
    if (alwaysOnSwitch instanceof Object && alwaysOnSwitch.currentValue('switch').contains('off'))
    {
        unschedule(alwaysOnDisplay)
        hues*.off()
    }
    else
    {
        int delay = 5
    
        if (state.colors.size() > 0) {
            if (state.colors[state.colorIndex] == "Alert")
            {
            	if (flashEnabled) {
                	delay = 0
                }
                sendcolor(alertColor, flashEnabled)
            }
            else
            {
                sendcolor(state.colors[state.colorIndex], false)
            }
            state.colorIndex = state.colorIndex + 1
            if (state.colorIndex >= state.colors.size()) state.colorIndex = 0
        }

        if (state.colors.size() > 1) {
            debug('canSchedule(): ' + canSchedule())
            runIn(delay, alwaysOnDisplay)
            //schedule("0 0/2 * * * ?", alwaysOnDisplay)
            debug ("Multiple weather conditions exist. Scheduling color cycling.", true)
        }
        else
        {
            debug ("Single weather condition. Color cycling disabled until forecast refresh", true)
        }

        debug ('state.colorIndex: ' + state.colorIndex)
    }
}

// Weather Processing
def getWeather(firstRun) {
    def forecastUrl="https://api.openweathermap.org/data/2.5/onecall?lat=$location.latitude&lon=$location.longitude&exclude=daily,minutely&units=imperial&appid=$apiKey"
    
    debug (forecastUrl)
    
    httpGet(forecastUrl) {response -> 
        if (response.data) {
            state.weatherData = response.data
            def d = new Date()
            state.forecastTime = d.getTime()
            debug("Successfully retrieved weather.", true)
            if (alwaysOn) {
                displayWeather(true)
            }
        } else {
            runIn(60, getWeather)
            debug("Failed to retrieve weather.", true)
        }
    }
    
}

def displayWeather(newCycle) {

    debug('isDisplaying: ' + state.isDisplaying)

    if ((!alwaysOn && !state.isDisplaying) || newCycle) {

        state.isDisplaying = true;
        hues*.refresh()

        def d = new Date()
        if ((d.getTime() - state.forecastTime) / 1000 / 60 > 30) {
            try {unschedule()} catch(err){debug("-Unschedule failed.", true)}
            schedule("0 0/60 * * * ?", getWeather)
            getWeather()
        }
        
        if (!alwaysOn && rememberLevel) {
            state.current.clear()
            hues.each {
                state.current.add([switch: it.currentValue('switch'), hue: it.currentValue('hue'), saturation: it.currentValue('saturation'), level: it.currentValue('level')] )
            }
        }

        //Initialize weather events
        def willRain = false
        def willSnow = false
        def willSleet = false
        def windy = false
        def tempLow
        def tempHigh
        def cloudy = false
        def humid = false
        def weatherAlert = false

        def response = state.weatherData

        if (state.weatherData) { //API response was successful

            state.colors.clear()
            state.colorIndex = 0
            debug ("Unscheduling alwaysOn")
            try {
                unschedule("alwaysOnDisplay")
            } catch(err) {
                debug("Cant unschedule always on", true)
                debug(err)
            }

            def i = 0
            def lookAheadHours = 1
            def forecastData = []
            
			//Get current weather conditions
            forecastData.push(response.current)
            
			if (forecastRange != "Current conditions") {
                lookAheadHours = forecastRange.replaceAll(/\D/,"").toInteger() //Need to strip non-numeric characters(i.e. "hours") from string so we can cast to an integer
				for (hour in response.hourly) {
					if (lookAheadHours < ++i) { //Break if we've processed all of the specified look ahead hours.
						break
					} else {
						forecastData.push(hour)
					}
				}
            }

            for (hour in forecastData){ //Iterate over hourly data

				def weatherId = hour.weather.id[0]
		
				if (rainEnabled && ((weatherId >= 200 && weatherId < 600) || weatherId == 701)) {
					willRain = true
				}

				if (snowEnabled && weatherId >= 600 && weatherId < 602) {
					willSnow = true
				}

				if (sleetEnabled && weatherId >= 611 && weatherId < 614) {
					willSleet = true
				}

				if ((rainEnabled || snowEnabled) && weatherId >= 615 && weatherId < 622) {
					willRain = rainEnabled
					willSnow = snowEnabled
				}

				if (lowTempEnabled) {
					if (tempMinType == 'Actual') {
						if (tempLow == null || tempLow > hour.temp) tempLow = hour.temp //Compare the stored low temp to the current iteration temp. If it's lower overwrite the stored low with this temp
					}
                    else {
						if (tempLow == null || tempLow > hour.feels_like) tempLow = hour.feels_like //Compare the stored low temp to the current iteration temp. If it's lower overwrite the stored low with this temp
					}
				}

				if (highTempEnabled) {
					if (tempMaxType == 'Actual') {
						if (tempHigh == null || tempHigh < hour.temp) tempHigh = hour.temp //Compare the stored low temp to the current iteration temp. If it's lower overwrite the stored low with this temp
					}
                    else {
						if (tempHigh == null || tempHigh < hour.feels_like) tempHigh = hour.feels_like //Compare the stored low temp to the current iteration temp. If it's lower overwrite the stored low with this temp
					}
				}

				if (windEnabled && (hour.wind_speed >= windTrigger || hour.wind_gust >= windTrigger + 5)) windy = true //Compare to user defined value for wind speed.
                
				if (!willRain && !willSnow && !willSleet && cloudyEnabled && hour.clouds >= cloudPercentTrigger) { //Compare to user defined value for wind speed.
                	cloudy = true
                }
                else if (willRain || willSnow || willSleet) //No need to show the cloudy color if it's precipitating, that can be assumed
                {
                	cloudy = false
                }
                
				if (dewPointEnabled && hour.dew_point >= dewPointTrigger) humid = true //Compare to user defined value for wind speed.
            }

            if (response.alerts) { //See if Alert data is included in response
                response.alerts.each { //If it is iterate through all Alerts
                    def thisAlert=it.event;
                    debug thisAlert
                    alertFlash.each{ //Iterate through all user specified alert types
                        if (thisAlert instanceof Object && thisAlert.toLowerCase().indexOf(it)>=0) { //If this user specified alert type matches this alert response
                            debug ("ALERT: "+it, true)
                            weatherAlert=true //Is there currently a weather alert
                        }
                    }
                }
            }

            //Add color strings to the colors array to be processed later
            if (lowTempEnabled && tempLow <= tempMinTrigger.floatValue()) {
                state.colors.push(tempMinColor)
                debug ("Cold - " + tempMinColor, true)
            }
            if (highTempEnabled && tempHigh >= tempMaxTrigger.floatValue()) {
                state.colors.push(tempMaxColor)
                debug ("Hot - " + tempMaxColor, true)
            }
            if (dewPointEnabled && humid) {
                state.colors.push(dewPointColor)
                debug ("Humid - " + dewPointColor, true)
            }
            if (snowEnabled && willSnow) {
                state.colors.push(snowColor)
                debug ("Snow - " + snowColor, true) 
            }
            if (sleetEnabled && willSleet) {
                state.colors.push(sleetColor)
                debug ("Sleet - " + sleetColor, true)
            }
            if (rainEnabled && willRain) {
                state.colors.push(rainColor)
                debug ("Rain - " + rainColor, true)
            }
            if (windEnabled && windy) {
                state.colors.push(windColor)
                debug ("Windy - " + windColor, true)
            }
            if (cloudyEnabled && cloudy) {
                state.colors.push(cloudPercentColor)
                debug ("Cloudy - " + cloudPercentColor, true)
            }
            if (weatherAlert) {
                state.colors.push("Alert")
                debug ("Alert - " + alertColor, true)
            }
        }

        //If the colors array is empty, assign the "all clear" color
        if ((state.colors.size() == 0 && allClearEnabled) || (state.colors.size() == 1 && weatherAlert))
        {
        	state.colors.push(allClearColor)
        }
        state.colors.unique()
        debug state.colors
             
        int duration = 5 //The amount of time to leave each color on
        int maxDisplay = 25

        duration *= 1000
        maxDisplay *= 1000
        def displayCount = state.colors.size()

        if (duration * displayCount >= maxDisplay) {
            duration = Math.floor(maxDisplay / displayCount)
        }

        if (!alwaysOn) {
            state.colors.each { //Iterate over each color
                for (int i = 0; i<iterations; i++) {
                    if (it == "Alert")
                    {
                        sendcolor(alertColor, flashEnabled)
                    }
                    else
                    {
                        sendcolor(it, false) //Turn light on with specified color
                    }
                    pause(duration) //leave the light on for the specified time
                }
            }

            state.isDisplaying = false

            setLightsToOriginal() //The colors have been sent to the lamp and all colors have been shown. Now revert the lights to their original settings

        } else {
            alwaysOnDisplay()
        }
    }
}


// Light Control
def sendcolor(color, flash) {
    //Initialize the hue and saturation
    def hueColor = 0
    def saturation = 100

    //Use the user specified brightness level. If they exceeded the min or max values, overwrite the brightness with the actual min/max
    if (brightnessLevel<1) {
        brightnessLevel=1
    } else if (brightnessLevel>100) {
        brightnessLevel=100
    }
    
    def hsl = getColorDefinitions(color)
    
    hueColor = hsl[0]
    saturation = hsl[1]
    
    debug ("Setting color to " + color, true)

    //Change the color of the light
    try {
        hues*.on()
        if (hues[0].currentSaturation > saturation)
        {
            hues*.setSaturation(saturation)
            hues*.setHue(hueColor)
        }
        else
        {
            hues*.setHue(hueColor)
            hues*.setSaturation(saturation)
        }
        hues*.setLevel(brightnessLevel)
        
        if (flash)
        {
            int flashDelay = 1000
            pause(flashDelay)
            
            int i = 0
            while (i < 5)
            {
                // If the saturation of the chosen alert color is 0 (they picked white), change the brightness
                if (saturation == 0)
                {
                    hues*.level(brightnessLevel / 2)
                    pause(flashDelay)
                    hues*.level(brightnessLevel)
                    pause(flashDelay)
                }
                // Otherwise if the chosen color is not white, alternate their chosen color with white
                else
                {
                    hues*.setSaturation(0)
                    pause(flashDelay)
                    hues*.setSaturation(saturation)
                    pause(flashDelay)
                }
                i++
            }
        }
    } catch (err) {
        debug(err)
        debug("There was a problem changing bulb color", true)
    }
}

def setLightsToOriginal() {
    if (rememberLevel) {     
        hues.eachWithIndex { it, i ->           
            
            it.setHue(state.current[i].hue)
            it.setSaturation(state.current[i].saturation)
            it.setLevel(state.current[i].level)
            if (state.current[i].switch == "off") {
                it.off()
            }

            debug ("RESET: " + state.current[i])
        }
        //hues.refresh()
    } else {
        hues.off()
    }
}

// HANDLE EVENT
def motionHandler(evt) {
    if (evt.value == "active") {// If there is movement then trigger the weather display
        debug ("Motion detected, turning on light", true)
        displayWeather()
    } 
}

def contactHandler(evt) {
    if (evt.value == "open") {
        debug ("Contact sensor open, turning on light", true)
        displayWeather()
    }
}

def switchHandler(evt) {
    if (evt.value == "on")
    {
        getWeather()
    }
    else if (evt.value == "off")
    {
    	unschedule(alwaysOnDisplay)
        hues*.off()
    }
}

// Debug
def debug(msg) {
    debug(msg, false)
}

def debug(msg, toNotifications) {
    //Enable debugging. Comment out line below to disable output.
    //log.debug(msg)
    log.debug(msg)
    
    //Uncomment the next line to send debugging messages to hello, home. I use this when live logging breaks, which is often for me, and when I need a way to view data that's logged when I'm not logged in. 
    if (debugMode && toNotifications) sendNotificationEvent("DEBUG COLORCAST: " + msg)
}
