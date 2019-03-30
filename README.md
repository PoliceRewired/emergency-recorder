<img align="right" src="https://github.com/PoliceRewired/emergency-recorder/raw/master/Screenshots/008_2019-03-28-bubblecam_seat.png" width="320px" />

# Emergency Recorder Toolkit
A personal toolkit for recording evidence during emergencies using a modern Android phone.

[![Follow PoliceRewired](https://img.shields.io/twitter/follow/policerewired.svg?style=social&label=Follow%20Police%20Rewired)](https://twitter.com/policerewired)

## What does the toolkit do?

The Emergency Recorder Toolkit is aimed at people encountering emergencies, who are on the phone to emergency services. Without any additional effort on their part, it detects the emergency call and initiates video recording.

When capture begins, the app offers 3 modes:
* Photography (allow the user to point and click to take photos). No audio.
* Burst mode (takes a series of photos, and stitches them together afterwards into a video). Audio recorded separately.
* Full video (not yet supported - see dependencies, below). Audio recorded with video.

<br clear="right" />
<img align="right" src="https://github.com/PoliceRewired/emergency-recorder/raw/master/Screenshots/007_2019-03-28-outgoing-call.png" width="320px" />

## Can you really do that?

We have working code, and the app in its current state illustrates solutions to the following challenges:

- [x] Create a camera preview, constrained to be small enough not to obstruct an emergency call.
- [x] Create an overlay ('bubble') containing the camera, drawn over the top of any other app.
- [x] Make the overlay draggable, without obstructing taps for the apps behind.
- [x] Detect calls made through the standard Android dialler.
- [x] Examine the number used for the outgoing call, and make a decision based on its content.
- [x] Take photos from the overlay.
- [x] Take a series ('burst') of photos from the overlay, and stitch them into a video afterwards.
- [x] Record audio from the overlay, whilst the photos are being taken.
- [x] Insert photos, videos, and audio into appropriate places in the Android media gallery.
- [x] Retrieve the user's current location whilst they have the bubble open.
- [x] Geocode that location to display a nearby street address for the user.
- [x] Parse that location into What3Words to display a short 3-word location to the user.

## Does this violate privacy?

_"In the United Kingdom there are no laws forbidding photography of private property from a public place."_
* See: [Photography and the law](https://en.wikipedia.org/wiki/Photography_and_the_law#United_Kingdom), Wikipedia

The app itself simply records to your phone. It is designed to make it possible to improve the quality of evidence you can give to police. _You_ choose what is shared, how, and to whom.

## How far is the project developed?

Persistence

- [x] Requests appropriate permissions.
- [x] Displays a persistent notification to assure the user that the service will respond to outgoing calls.
- [x] App is resilient against shutdowns initiated by the OS (for whatever reason).
- [x] App launches automatically when the device starts up.
- [x] Displays an unobtrusive floating button which the user can use to open the camera bubble.

Bubble camera

- [x] App can launch the camera preview in an overlay ("camera bubble").
- [x] User can take photos using the overlay.
- [x] The new photo appears in the user's phone gallery immediately.
- [x] User can take a burst of photos using the overlay.
- [ ] During the burst, each photo taken is added to the phone gallery immediately.
- [x] After a burst, photos are stitched into a video, and appear in the user's phone gallery.
- [x] App displays a notification whilst photographs are being stitched into video.
- [x] Audio recording accompanies the burst mode photography.
- [x] The new audio appears in an album called 'Emergency Audio' in the user's music app immediately.
- [x] A record of each recording made is stored in the app's internal database.
- [ ] User can take a standard video using the overlay. (See dependencies, below.)
- [ ] The new video appears in the user's phone gallery immediately.

Receive outgoing calls

- [x] App registers with Android to receive information about outgoing calls.
- [x] Outgoing calls that initiate an action in the app are recorded in the app's internal database.
- [x] The user's rules are applied to each outgoing call to determine the action to take.

Behaviours

- [x] User can specify which telephone numbers will trigger the camera overlay.
- [x] User can specify a behaviour (launch camera | start video | start burst | nothing) per number.
- [x] Rules (telephone number -> behaviour) are stored in the app's internal database.

Location

- [x] App fetches the user's current location.
- [x] App geocodes that location, and displays the closest address to the user.
- [x] App also utilises the What3Words API to make visible the closest What3Words location.
- [x] App superimposes coordinates and time onto visible portion of each stored photo.
- [ ] App stores taken time and location information in metadata for each stored photo.

User information

- [ ] Users can view a log showing a record of each recording made by the app.
- [ ] Users can view an About page detailing information about the app, and find out more about Police Rewired.
- [ ] App features a HOWTO video, showing how it works and how it can be helpful.
- [ ] App links to a website with info about how to be helpful and safe. (eg. What makes a good witness? What makes good evidence?)

### Dependencies

* [CameraKit](https://camerakit.io/) is superb, but the version 1.0.0 beta series does not yet support video recording. This is coming soon.
* Keeping services and intent listeners alive on Android is an ongoing arms-race/battle with Google's definition of 'best practises'. We are using [background-service-lib](https://github.com/front-line-tech/background-service-lib), an open source background service toolkit library from [Front-Line Tech Ltd](http://front-line-tech.com).
* We use Google Play Services to provide location facilities, and Android provides geocoding facilities.
* We also use [What3Words](https://docs.what3words.com/wrapper/android/) to provide a short, easily pronounceable location description.
* We use [jcodec](http://jcodec.org/) to stitch photos into video.

## What is Police Rewired?

[Police Rewired](https://policerewired.org) is a group of volunteer developers, designers, academics, data scientists and creative problem solvers who want to make everybody a bit safer by building new tools. We fight crime with code.

All our projects are open source. You can find out more at our website: https://policerewired.org
