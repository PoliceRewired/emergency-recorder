# Emergency Recorder Toolkit
A personal toolkit for recording evidence during emergencies using a modern Android phone.

[![Follow PoliceRewired](https://img.shields.io/twitter/follow/policerewired.svg?style=social&label=Follow%20Police%20Rewired)](https://twitter.com/policerewired)

## What does the toolkit do?

The Emergency Recorder Toolkit is aimed at people encountering emergencies, who are on the phone to emergency services. Without any additional effort on their part, it detects the emergency call and initiates video recording.

When capture begins, the app offers 3 modes:
* Photography (allow the user to point and click to take photos). No audio.
* Burst mode (takes a series of photos, and stitches them together afterwards into a video). Audio recorded separately.
* Full video (not yet supported - see dependencies, below). Audio recorded with video.

![Crimes against code?](https://github.com/PoliceRewired/emergency-recorder/raw/master/Screenshots/001_2019-03-24_bubblecam.jpg)

## How far is the project developed?

Persistence
- [x] Requests appropriate permissions.
- [x] Displays a persistent notification to assure the user that the service will respond to outgoing calls.
- [x] App is resilient against shutdowns initiated by the OS (for whatever reason).
- [x] App launches automatically when the device starts up.

Receive outgoing calls
- [x] App registers with Android to receive information about outgoing calls.
- [x] Outgoing calls that initiate an action in the app are recorded in the app's internal database.

Bubble camera
- [x] App can launch the camera preview in an overlay ("camera bubble"), in response to a phone call.
- [x] User can take photos using the overlay.
- [x] The new photo appears in the user's phone gallery immediately.
- [x] User can take a burst of photos using the overlay.
- [x] After a burst, photos are stitched into a video, and appear in the user's phone gallery.
- [x] App displays a notification whilst photographs are being stitched into video. _(untested)_
- [x] Audio recording accompanies the burst mode photography.
- [x] The new audio appears in an album called 'Emergency Audio' in the user's music app immediately.
- [ ] User can take a standard video using the overlay. (See dependencies, below.)
- [ ] The new video appears in the user's phone gallery immediately.
- [x] All recordings made are also stored in the app's internal database.

Rules
- [ ] User can specify which telephone numbers will trigger the camera overlay.
- [ ] User can specify behaviour (launch camera | start video | start burst | nothing) per number.
- [ ] Rules (telephone number -> behaviour) and configuration are stored in the app's internal database.

Location
- [x] App fetches the user's current location.
- [x] App geocodes that location, and displays the closest address to the user.
- [ ] App also utilises the What3Words API to make visible the closest What3Words location.
- [ ] App stores taken time and location information in metadata for each stored photo.

User information
- [ ] Users can view an About page detailing information about the app, and find out more about Police Rewired.
- [ ] App features a HOWTO video, showing how it works and how it can be helpful.
- [ ] App links to a website with info about how to be helpful and safe. (eg. What makes a good witness? What makes good evidence?)

### Dependencies

* [CameraKit](https://camerakit.io/) is superb, but the version 1.0.0 beta series does not yet support video recording. This is coming soon.
* Keeping services and intent listeners alive on Android is an ongoing arms-race/battle with Google's definition of 'best practises'. We are using [background-service-lib](https://github.com/front-line-tech/background-service-lib), an open source background service toolkit library from [Front-Line Tech Ltd](http://front-line-tech.com).
* We will be using Google Maps for their geocoding facilities.
* We use [jcodec](http://jcodec.org/) to stitch photos into video.

## What is Police Rewired?

[Police Rewired](https://policerewired.org) is a group of volunteer developers, designers, academics, data scientists and creative problem solvers who want to make everybody a bit safer by building new tools. We fight crime with code.

All our projects are open source. You can find out more at our website: https://policerewired.org
