---
name: cas-adapter
description: >-
  Writes or reviews a CAS.AI mediation adapter for one ad network, inside the
  CAS-Kotlin (Android) or CAS-Swift (iOS) SDK repository: registering the adapter,
  initialization, bidding signals, the interstitial, rewarded, app open, banner,
  MREC and native loaders, error mapping and the lifecycle callbacks. Use when the
  task is to add, port or fix an adapter in those repositories, or to check that
  the Android and iOS adapters for one network agree. Don't use for integrating the
  CAS SDK into a publisher's app, which is the cas-android and cas-ios skills.
license: Apache-2.0
metadata:
  version: "0.1.0"
  category: CAS.AI
---
# CAS.AI mediation adapters

This is internal work in the SDK repositories, not an app integration. Each repository
carries a reference adapter that compiles and a document that is the source of truth:

| Platform | Reference adapter | Instructions |
|---|---|---|
| Android | `adapters/custom/` in **CAS-Kotlin** | `adapters/custom/AGENTS.md` |
| iOS | `Adapters/Custom/` in **CAS-Swift** | `Adapters/Custom/AGENTS.md` |
| Both | — | `CustomAdapter-Kotlin-Swift.md`, the same file in both repositories |

**Read the `AGENTS.md` for the platform first, in full, and then the reference adapter's
source.** It is kept next to the code and it wins over anything here. This page exists so
an agent that has not opened the repository yet knows what it is walking into.

## What an adapter is

CAS decides which network to ask, in what order and at what price. An adapter is the
translation layer for exactly one network, and it owns three things: initialising the
network SDK once, turning a CAS ad request into a network ad request per format, and
turning the network's callbacks back into CAS callbacks.

It owns no policy. It does not retry, does not cache ads across requests, does not decide
a format is not worth asking for, and never calls another network. CAS does all of that,
and an adapter that second-guesses the mediation layer produces bugs that look like SDK
bugs.

## Four things to know before writing code

1. **One answer per request.** Exactly one success or failure, exactly once. Several
   network SDKs call their own listener twice on timeout, so the reference adapters clear
   the held request after the first answer — that is what makes the second call a no-op.
2. **The error code is the only lever.** There is no API to switch retries off. The CAS
   error an adapter maps a network failure onto decides whether CAS asks that network
   again for the rest of the session. Mapping everything to "no fill" hides broken
   placements; mapping everything to "configuration error" drops a network that was only
   having a bad minute.
3. **Decline, do not return nothing.** For a format or a size the network does not sell,
   call the base implementation. It reports "not supported" and the waterfall moves on. A
   bare `null`/`nil` reports nothing and the waterfall waits.
4. **Both platforms, always.** Android and iOS adapters for the same network must agree
   on what they send and what they report. Before calling a difference a bug, read the
   base implementation as well as the network's override on both platforms — a value set
   in the shared base is easy to mistake for a per-network choice.

## Order of work

1. Register the adapter — `build-logic/src/main/kotlin/AdaptersList.kt` on Android,
   `tools/adapterList.py` on iOS. Nothing builds before this.
2. Create the module or Xcode target, copying the reference adapter's scaffolding.
3. Write the adapter class, then a loader and an ad class per format family. The
   loader exists only until the request is answered and builds the ad once the network
   has one; keeping the two in one object is where the awkward bugs live.
4. Map the network's errors onto CAS errors.
5. Build the **release** configuration: it is the minified one, and it catches what a
   debug build hides.
6. Check the declared formats against the implemented ones, and the two platforms against
   each other.

The reference adapter is built alongside the real ones on both platforms, so it cannot
drift away from the mediation API. If a change to that API breaks the example, the example
is what gets fixed — and its `AGENTS.md` with it.
