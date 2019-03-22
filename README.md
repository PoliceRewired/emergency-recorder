# Emergency Recorder Toolkit
A personal toolkit for recording evidence during emergencies using a modern Android phone.

[![Follow PoliceRewired](https://img.shields.io/twitter/follow/policerewired.svg?style=social&label=Follow%20Police%20Rewired)](https://twitter.com/policerewired)

## What does the toolkit do?

The Emergency Recorder Toolkit is aimed at people encountering emergencies, who are on the phone to emergency services. Without any additional effort on their part, it detects the emergency call and initiates video recording.

When capture begins, the app offers 3 modes:
* Photography (allow the user to point and click to take photos). No audio.
* Burst mode (takes a series of photos, and stitches them together afterwards into a video). Audio recorded separately.
* Full video (not yet supported - see dependencies, below). Audio recorded with video.

## How far is the project developed?

- [x] Requests appropriate permissions.
- [x] Displays a persistent notification to assure the user that the service will respond to outgoing calls.
- [x] App registers with Android to receive information about outgoing calls.
- [ ] App launches automatically when the device starts up.
- [ ] App is resilient against shutdowns initiated by the OS (for whatever reason).
- [x] App can launch the camera preview in an overlay, in response to a phone call.
- [x] User can take photos using the overlay.
- [x] The new photo appears in the user's phone gallery immediately.
- [x] User can take a burst of photos using the overlay.
- [x] After a burst, photos are stitched into a video, and appear in the user's phone gallery.
- [x] Audio recording accompanies the burst mode photography.
- [x] The new audio appears in an album called 'Emergency Audio' in the user's music app immediately.
- [ ] User can take a standard video using the overlay. (See dependencies, below.)
- [ ] The new video appears in the user's phone gallery immediately.
- [ ] User can specify which telephone numbers will trigger the camera overlay.
- [ ] User can specify behaviour (launch camera | start video | start burst | nothing) per number.
- [ ] App also geocodes the user's current location, and displays the closest address(es) to the user.

### Dependencies

* [CameraKit](https://camerakit.io/) is superb, but the version 1.0.0 beta series does not yet support video recording. This is coming soon.
* Keeping services and intent listeners alive on Android is an ongoing arms-race/battle with Google's definition of 'best practises'. We are using [background-service-lib](https://github.com/front-line-tech/background-service-lib), an open source background service toolkit library from [Front-Line Tech Ltd](http://front-line-tech.com).
* We will be using Google Maps for their geocoding facilities.
* We use [jcodec](http://jcodec.org/) to stitch photos into video.

## What is Police Rewired?

[Police Rewired](https://policerewired.org) is a group of volunteer developers, designers, academics, data scientists and creative problem solvers who want to make everybody a bit safer by building new tools. We fight crime with code.

All our projects are open source. You can find out more at our website: https://policerewired.org
