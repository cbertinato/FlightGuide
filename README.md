# FlightGuide

*Work in Progress*

An Android tablet application for pilot guidance.

Designed to provide pilots on LC-130 aircraft with route guidance
prescribed by the sensor operator when flying aero-geophysical surveys.

Current position is provided by text-based position input over Ethernet.

Guidance is given in the form of a visual indication of deviation off course,
much like that of an ILS localizer as well as a numerical indication.  
Distance to go and bearing to next point are also indicated on the screen.

Waypoints are loaded from text file in the tablet and can be updated by
the sensor operator.

Waypoint shapes and colors indicate how to handle a turn:
* green triangle = fly through
* green square = fly to
* green circle = turn inside
* double green triangle = fly through (reoccupy)
* double green square = fly to (reoccupy)
* orange x = start of lead-in

Built for Nexus 7 (2012), 7.0", 800x1200 tvdpi.
Tested on Android 7.1.1 x86 (Nougat).

## License
MIT.
