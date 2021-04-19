<div style="width:100%">
	<div style="width:50%; display:inline-block">
    <p align="center">
      <img align="center" src="https://github.com/dwarshb/WebRTC-Sample/blob/master/app/src/main/res/drawable/icon_512px.png" width="200px"/>
    </p>
	</div>	
</div>

# WebRTC-Sample
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](#)
[![Platform](https://img.shields.io/badge/Language-Kotlin-yellowgreen.svg)](#)
[![](https://img.shields.io/github/issues/dwarshb/WebRTC-Sample)](#)
[![](https://shields.io/badge/v-v1.1-red?lstyle=flat)](#)
[![](https://img.shields.io/github/stars/dwarshb/WebRTC-Sample?style=social)](#)
[![](https://img.shields.io/github/contributors/dwarshb/WebRTC-Sample?style=social)](#)
[![](https://shields.io/badge/webrtc-v1.0.32006-green?logo=webrtc&style=flat)](https://webrtc.github.io/webrtc-org/native-code/android/)


An android application which uses WebRTC and Firebase to support real time media communication. You can start one-one call between users for now.

---

## Pre-requisites
 :heavy_check_mark: Android studio installed in your system.<br/>
 :heavy_check_mark: Android Device or Emulator to run your app.<br/>
 :heavy_check_mark: Setup Account on Firebase and integrate app with your Firebase Project.<br/>
 
--- 

## Setup :hammer:

- You can clone the project from the WebRTC Sample repository.

```// Clone this repository
  git clone https://github.com/dwarshb/WebRTC-Sample.git
```

- Please make sure to create a Firebase Project and set-up with this app. You need to add `google-service.json` file of your Firebase project in your `app` folder.
<br/>For more details please check the below link.<br/>
https://firebase.google.com/docs/android/setup

Once the setup is done you can run the project in Android Studio.

---

## WebRTC ![](https://avatars.githubusercontent.com/u/10526312?s=24&v=4)
A platform which supports video, voice, and generic data to be sent between peers, allowing developers to build powerful voice- and video-communication solutions.
I have used native WebRTC library for WebRTC Support.
Please check below link for reference.
https://webrtc.github.io/webrtc-org/native-code/android/

---

## Firebase Cloud Firestore ![](https://avatars.githubusercontent.com/u/1335026?s=24&v=4)
I have used Firestore to store the information for `createOffer()`, `createAnswer()` and IceCandidates. This information is used to maintain one-one call connection.
Please check below link for Firestore Reference.<br/>
https://firebase.google.com/docs/firestore/manage-data/add-data

---

<img src="https://github.com/dwarshb/WebRTC-Sample/blob/master/images/call_layout.png"/>

