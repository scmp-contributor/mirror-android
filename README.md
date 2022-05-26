# mirror-android
SCMP Mirror real time tracking platform sdk for Android

<br/><br/>

# Table of Contents
<!-- MarkdownTOC -->
- [Mirror Library Deployment](#mirror-library-deployment)
- [Quick Setup Guide](#quick-setup-guide)
- [Install Mirror to the project](#1-install-mirror-to-the-project)
- [Initialize Mirror](#2-initialize-mirror)
- [Send Data](#3-send-data)
- [Check Result](#4-check-result)

<br/><br/>
# Mirror Library Deployment
Modify the code and update the version code in 
- `Constants` -> `AGENT_VERSION`
- `gradle.properties` -> `VERSION_NAME`

Commit and Push the code the branch

Tag the new version of the node and push to server

Go to https://jitpack.io/ and Look up 
```
scmp-contributor/mirror-android
```

Find the released version and click `Get it` to build and wait for completion

<br/><br/>

# Quick Setup Guide

## 1. Install Mirror to the project

**Step 1 - Add the mirror-android library as a gradle dependency:**

To install the library inside Android Studio, you can simply declare it as dependency in your build.gradle file.

Please make sure you've specified `maven { url "https://www.jitpack.io" }` as a repository in your `build.gradle`.

Add the following lines to the `dependencies` section in *app/build.gradle*

```gradle
implementation "com.github.scmp-contributor:mirror-android:(release-tag-version)"
```
 
Once you've updated your build.gradle file, you can force Android Studio to sync with your new configuration by clicking the Sync Project with Gradle Files icon at the top of the window.

**Step 2 - Add permissions to your AndroidManifest.xml:**
In order for the library to work you'll need to ensure that you're requesting the following permissions in your AndroidManifest.xml:

```java
<uses-permission
  android:name="android.permission.INTERNET" />
```
At this point, you're ready to use the Mirror Android library inside Android Studio.
## 2. Initialize Mirror
```kotlin
import com.scmp.mirror.MirrorAPI

class SCMPApplication : MultiDexApplication()  {

    override fun onCreate() {
        super.onCreate()
		MirrorAPI(application = this,
				oraganizationId = "YOUR_ORGANIZATION_ID", 
				domain = "YOUR_DOMAIN", 
				isDebug = BuildConfig.DEBUG,
				isTablet = "IS_PAD")
    }
}
```

Pass the touch event for mirror to track user behavior
```kotlin
class BaseAcitivity: Activity() {
	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        MirrorAPI.instance.dispatchTouchEvent(ev)
        super.dispatchTouchEvent(ev)
}
```
<br/><br/>
## 3. Send Data

Send Ping Event
```kotlin
val trackData = TrackData(
        path = "CURRENT_PAGE_PATH",
        visitorType = "VISITOR_TYPE", // defined in mirror library enum class VisitorType
        pageTitle = "PAGE_TITLE",
        section = "ARTICLE_SECTION",
        author = "ARTICLE_AUTHOR",
        internalReferrer = "INTERNAL_REFERRER"
    )
MirrorAPI.instance.ping(trackerData)
```

Send Click Event

```kotlin
val trackData = TrackData(
        path = "CURRENT_PAGE_PATH",
        visitorType = "VISITOR_TYPE", // defined in mirror library enum class VisitorType
        clickUrl = "FULL_DESTINATION_URL" // including domain

MirrorAPI.instance.click(trackerData)
```

Update domain url when switching environments by using below method
```kotlin
MirrorAPI.instance.updateDomain(domain)
```
## 4. Check Result
There will be print logs for variety mirror events. By filtering `Mirror` to check the print logs to validate the result.

<br/><br/>
Ping log sample
```
Mirror ping Request Response Success
====== Mirror Request Body Start ======
     k : 1
    d : scmp.com
    p : /news/hong-kong/health-environment/article/3179276/coronavirus-hong-kong-prepared-rebound-infections
    u : kBUA76KcIWr1tmitsi8T1
    vt : gst
    eg : 0
    sq : 1
    s : articles only, News, Hong Kong, Health & Environment
    a : Sammy Heung, Nadia Lam
    pt : Coronavirus: Hong Kong prepared for rebound in infections even as fifth wave of pandemic subsides, health minister says; 251 cases logged
    pi : lZ3ij5STaj3SCOl2TaENC
    ir : https://scmp.com/hk
    et : ping
    nc : true
    ff : 45
    v : ma-0.2.1
====== Mirror Request Body End ======
```
<br/><br/>
Click log sample
```
Mirror click Request Response Success
====== Mirror Request Body Start ======
     k : 1
    d : scmp.com
    p : /hk
    u : kBUA76KcIWr1tmitsi8T1
    vt : gst
    eg : 1
    sq : 2
    pi : JhhDi1UVl47yUTPqr3rj2
    et : click
    nc : true
    ci : https://scmp.com/news/hong-kong/health-environment/article/3179276/coronavirus-hong-kong-prepared-rebound-infections
    v : ma-0.2.1
D/MirrorCallback: ====== Mirror Request Body End ======
```
