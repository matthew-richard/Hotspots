# Hotspots
An Android app for discovering local communities.

This app consists of three activities:
- LoginActivity
- Main Activity, with the following fragments:
  - MapHome
  - Statistics
  - Settings
  - Feed
- NewPostActivity

An overview of all activities/fragments:
- LoginActivity
  All new users are taken to this page in order to register/log in with an existing account
- Main Activity
  After successful registration/authentication, users are taken to the main activity, which consists of a frame layout that can be replaced by a fragment. We've used fragments in order to successfully implement a navigation drawer for our app.
  - MapHome - A Google maps view of the user's current location and any hotspot pins that are around them
  - Statistics - a view of the user's post statistics, which includes total number of likes on their posts, total number of posts they have created, and total number of hotspots they have been a part of
  - Settings - a fragment where users can change their username and user icon
  - Feed - a view of all the posts in a Hotspot/current location - accessible by clicking on a pin on MapHome
- NewPostActivity - an activity where the user can either post a text or picture post (accessible through a tab menu). The user also has an option to be anonymous or use their username and user icon by toggling an on/off 'Be anonymous' button. 
