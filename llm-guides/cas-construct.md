# CAS SDK integration guide for AI agents (Construct 3, ad mediation)

You are an AI coding agent helping a studio integrate the CAS SDK (CAS.AI ad mediation) into a **Construct 3** project. Do exactly the steps below, in order. **Stop and ask the human whenever this guide says so.**

**Read this before anything else: you cannot do most of this work yourself.** A Construct project lives in the Construct 3 editor. The addon is installed through a dialog, the configuration is a Properties bar, and the ad calls are blocks in an event sheet. None of that is a file you edit. **Your job is to decide what is correct, say exactly which property and which event block, and verify the result in the one place that is text — the exported Cordova project.** An agent that starts editing a `.c3p` is doing the wrong thing; say what to click instead.

Rules for the whole guide:
- Never invent values. A value comes from the project (step 0 research) or from the human, never from a guess.
- Do not initialize any mediated ad network SDK yourself. CAS does it. Doing it breaks the integration.
- Do not edit `.c3p` files, the files inside an exported `project.c3proj`, or an event sheet's JSON. Construct owns them, and a hand-edit is unsupported.
- Show the human what you are about to change before changing it, and wait for a clear yes. The reply to the step 0 block (`ok` or the chosen letters) is that yes for everything the block named.
- Never test on live ads, and never release with test ads on. Both are rules, not preferences, and neither is a question.

---

## 0. Research the project, then ask

**Research first, ask second.** Everything that can be read is read, not asked.

### Question format

This format is used for **every** question in this guide, not only in step 0.

- One question = one short bold line, then `Found:` (the evidence and where it came from, or "nothing found"), then a table of 2-4 ready options with a letter (two columns only: the letter and the answer; **no Recommended column**). Right after the table, one line: `**Recommended:** Option A - the reason in a few words`. Add a last option "Other" only when a free value is possible. When there is nothing to recommend (a value that cannot be guessed), write `**Recommended:** none - <why>`.
- The Recommended option is what the research found. If nothing was found, the safest default.
- Do not ask what the research settled. List it under "Found" so the human can object.
- **Rounds of at most 3 questions.** Numbering runs through all rounds (Q1-Q3, then Q4-Q6, and so on); title each round "Round 1 of 3". Send one round, wait for the reply, then send the next. Order the questions so that one an earlier answer decides comes after it. Skip a question that an earlier answer made pointless.
- **Separate questions with empty lines**, and put `---` between one question and the next. Never glue two questions together.
- End each round with: "Reply `ok` to accept every Recommended answer of this round, or list the changes, for example `2B`."
- A question with `Recommended: none` stays open after `ok`: ask only that one again, in a short follow-up.
- After the reply, say in one line what was chosen and start at once.

### Research (read-only)

A Construct project is usually a single `.c3p`, or a **project folder** saved from Construct with `project.c3proj` and JSON files beside it. Read what you can; never write.

| What | Where to look |
|---|---|
| Is this a Construct project at all | A `.c3p` file, or a `project.c3proj`. If instead you find `config.xml` with a Cordova plugin list and a `www/` folder, that is an **exported** project — see the note below. Neither present: this is not this guide's project. |
| An already exported Cordova project | `config.xml`, `www/`, and a `package.json`. This is the readable half of a Construct project, and the only place you can verify anything. The CAS plugin appears here as `cordova-plugin-casai`. |
| Bundle id and app name | `project.c3proj`, or the `<widget id>` of an exported `config.xml`. The bundle id is the Android CAS ID — the addon reads it automatically. |
| CAS already present | A `CASMobileAds` object in the project, or `cordova-plugin-casai` in an exported `config.xml`. |
| Other ad addons | Any AdMob, AppLovin or IronSource addon in the project, or their Cordova plugins in an exported `config.xml`. Two mediation SDKs cannot run side by side. |
| Construct version and build route | Ask: the Construct Build Service, or the Cordova CLI. It changes the whole of step 5, and nothing in the files answers it. |
| Toolchain, only for the CLI route | `node --version`, `cordova --version`, and on macOS `xcodebuild -version`. iOS needs a Mac. |
| Version control | `git status`, if the project is in a repository. A `.c3p` is a zip, so a diff shows nothing useful — say so rather than offering one. |

Show the human a short **Found** list first: the project file, the bundle id, whether an export exists, what is already installed, which build route. Then the questions.

### Questions

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Which platforms does this game ship? | A. Android and iOS; B. Android only; C. iOS only | What the export settings or the human say. Everything below is set **twice**, once per platform, in the same object. |
| 2 | Which CAS ID on iOS? | A. the numeric App Store id, digits only; B. leave it empty, the game is not on the App Store yet | **Not the bundle identifier.** B unless the human gives the App Store id. A real id only the human has: `none`. Android needs no answer — the addon reads the bundle package name itself. |
| 3 | Is the app already registered in CAS? | A. No, or do not know; B. Yes | A. Until CAS registers the app nothing serves. The integration is still done, on test ads, and the human's CAS contact registers it. |
| 4 | Which build route? | A. the Construct Build Service; B. the Cordova CLI | What the human already uses. **If the answer is A, warn about the allowlist trap in step 5 before going further** — it is the single most expensive thing on this platform. |
| 5 | New integration, or replacing another ad addon? | A. New; B. Replacing the addon found in the research | B when one was found, else A. CAS cannot run next to another mediation, so B means removing it — addon, object, event blocks and all. |
| 6 | Which ad formats, and at which moments? | A. banner, interstitial, rewarded; B. the same plus app open; C. another combination | The formats the old ad blocks use, if any; else A. Construct has banner **and** MREC as separate formats, and no native format. |
| 7 | Where does the banner go, and what gives up the room? | A. bottom, with the layout leaving space; B. top; C. no banner | A when a banner is wanted. The banner is drawn by the **native layer over the canvas** — nothing in the Construct layout knows it is there. See step 6. |
| 8 | What is the audience? | A. Mixed (Unknown); B. Children; C. Not Children | A unless the project says otherwise. These are the three values of the **Target Audience** property. |
| 9 | Only when 8 is Children: is the iOS app in the Apple Kids Category? | A. No, Google Play Families only; B. Yes | A. On the Kids Category, stop and point at the Apple Kids Category page: the Families solution is **not** intended for it, and the choice has to be agreed with the account manager. |
| 10 | Which ads solution? | A. `Optimal`; B. `Families`; C. a hand-picked adapter list | Follows from questions 8 and 9, and is set **twice**, once in the iOS Integration group and once in the Android Integration group. |
| 11 | Consent: the built-in CAS flow, or your own? | A. the built-in flow, `Consent Flow Auto show if required` on; B. a consent platform the game already has | A unless one was found. On B the automation is turned off and their decision applies before startup. |
| 12 | Where will this be verified? | A. the human runs a build on a device per platform; B. nobody can build right now | A. **There is no other way.** The addon does nothing in the Construct preview and nothing in a browser — there is no SDK there. |

---

## 1. Install the addon

Tell the human, in these words:

1. Download the latest `CASMobileAds.c3addon` from the releases of `https://github.com/cleveradssolutions/CAS-Construct`. The addon is also on the Construct addon page, *CAS.AI Mobile Ads*.
2. In Construct 3: **Menu > View > Addon Manager**, then **Install new addon...**, and pick the `.c3addon` file.
3. Reload Construct when it asks.

Then: **double-click a layout, find the `CAS.AI` object, insert it.** One object, on one layout — it is a single-global plugin, not something to place per layout.

You cannot do any of this. Say it as four numbered instructions and wait for confirmation that the object exists.

---

## 2. The object properties, which are the whole configuration

Select the CAS.AI object and set the properties in the Properties bar. **There is no initialization action and no CAS ID in any event sheet** — the addon initializes itself at startup from these properties. So a misconfigured game looks exactly like a correct one until it runs on a device.

Give the human a table of **property group, property, value**, and nothing else. These are the groups:

| Group | What to set |
|---|---|
| **iOS Integration** | `CAS Id (iTunes Id)` — the App Store id from question 2. `Solutions` — `Optimal` or `Families`. `Adapters` — only for a hand-picked list. `Tracking Usage Description` — the App Tracking Transparency text. |
| **Android Integration** | `Solutions` and `Adapters`, the same decision made separately. `Use Ad ID` — `true`, or `false` for a children-only game and for builds for devices without Google Play Services. No CAS ID: the bundle package name is read automatically. |
| **App Information** | `Target Audience` — `Children`, `Not Children` or `Unknown`. `App Keywords`, `App Content URL` — optional targeting, only with values the human has. `Mediation Extras` — JSON, used only when a network's documentation asks for it. |
| **Development Options** | `Force Test Ads`, `Test Device IDs`, `Debug Logging`, `Debug Geography`. See step 4. |
| **Automation Options** | Per format: auto load, auto show, refresh intervals, the interstitial minimum interval, `Consent Flow Auto show if required`, `Collect Location`, the trial ad-free interval. See step 3. |

Three things to get right, and they are the ones that go wrong:

- **Without `CAS Id (iTunes Id)` the iOS build serves test ads for ever**, silently, with no error and no log line. It is the most expensive empty field on this platform. Confirm the value is the App Store id — the number from `apps.apple.com/.../id123456789` — and not the bundle identifier.
- **The solution is two properties, not one.** Setting it in the Android group only means the game earns on Android and shows nothing on iOS. Say both, every time.
- **The adapter names are spelled differently per platform.** iOS writes `GoogleAds`, Android writes `googleAds`, separated by a comma, a semicolon or a space. A list copied from one group into the other is wrong while looking right. Take each from its own table: the Android and iOS `Adapters/README.md` in the CAS repositories.

Without a solution or an adapter list, nothing serves beyond test ads. CAS has no inventory of its own.

---

## 3. Automation: decide it once, here

The Automation Options group replaces most of the event blocks another platform would need. Recommend this, and explain each line:

- **Auto load on** for every format the game uses. A format that autoloads keeps an ad ready and retries failures by itself. A format the game never shows must be **off** — it costs requests for nothing.
- **Banner auto load has a catch:** unlike the other formats it only starts working after the game has performed a **Load Banner Ad** action once, with a size. Put that action in the startup event sheet, or the banner silently never loads.
- **Interstitial / App Open auto show** shows a loaded ad when the user returns to the app. Useful for app open; for the interstitial it takes the choice of moment away from the game, so leave it off unless the human asks.
- **Interstitials Ad Min Interval** paces interstitials. An ad refused inside that window fires *On Ad Failed to Show* — correct behaviour, not a defect.
- **Trial ad-free interval** suppresses ads for a number of seconds after install, keeping rewarded ads working. Say it in the hand-over, or a tester files the silence as a bug.

---

## 4. Test ads

- **`Force Test Ads` on** for every build the studio tests, and **off** before release. There is no build flag here that could do it automatically, so this is a human step and it belongs at the top of the hand-over checklist.
- Test full screen ads announce themselves and show CAS integration details, including a **Mediation Adapters** section listing every network that initialized correctly. That screen is the fastest proof the solution properties are right — and it is per platform, so it has to be read on both.
- `Test Device IDs` makes a release build serve test ads on named devices. The ids are printed in the device log on first run — read them from there, never invent one. Comma separated. Remove them before release.
- `Debug Logging` on for the verification build: the log tag is `CAS.AI`.
- `Debug Geography` makes the consent flow behave as if the device were in the EEA or a regulated US state. It applies to test sessions only.

---

## 5. Build, and the trap that eats a week

Construct exports a Cordova project; the CAS addon depends on the Cordova plugin `cordova-plugin-casai`.

> **The Construct Build Service uses an allowlist.** If the CAS plugin version the addon asks for has not been whitelisted yet, **the plugin is silently left out of the build. No error, no warning.** The game builds, installs and runs, and simply has no ads — and the only symptom is on the device. Each CAS update is submitted for whitelisting and the review can take up to a week.
>
> So: on the Build Service route, **check first** whether the version is available — the CAS-Construct repository tracks it in an open issue — and if it is not, build with the Cordova CLI instead. Tell the human this **before** they spend a day debugging an integration that was never in the binary.

The Cordova CLI route, which is also the one you can verify:

1. In Construct, export as **Android (Cordova)** or **iOS (Cordova)**, and in the Cordova options dialog choose **Cordova project**. Construct produces a zip; extract it.
2. Set **Minimum Android API level** and **Minimum target iOS version** to at least the values on the CAS build page. Below them the native SDKs will not build.
3. In the folder that holds `config.xml`:
   ```sh
   cordova prepare
   cordova requirements   # first time only
   cordova build
   ```
4. Android needs the JDK and Android Studio; iOS needs Xcode and `npm install -g ios-deploy`, **on a Mac** — a Windows or Linux machine can generate the Xcode project and no further.

**This exported folder is the one place you can verify anything.** Read `config.xml` and confirm `cordova-plugin-casai` is listed with the expected variables. If it is missing, the addon did not make it into the export, and nothing else in the integration matters.

---

## 6. The ad blocks in the event sheet

You cannot author event blocks. Give the human a per-format list of **actions** and **conditions** by their exact names, in order, with the events they belong under.

Every format has the same shape, with `Interstitial` replaced by `Rewarded`, `AppOpen`, `Banner` or `MREC`:

- Actions: **Load Interstitial Ad**, **Show Interstitial Ad**, **Destroy Interstitial Ad**.
- Conditions: **On Interstitial Ad Loaded**, **On Interstitial Ad Failed to Load**, **On Interstitial Ad Showed**, **On Interstitial Ad Failed to Show**, **On Interstitial Ad Clicked**, **On Interstitial Ad Dismissed**, and **Is Interstitial Ad Loaded**.
- Banner and MREC add **Show** with a position, **Hide**, and have no dismissed event; they do have Loaded, Failed to Load and Clicked.

Four things that are specific to Construct, and worth saying every time:

- **Load and Show are asynchronous actions.** To continue after one, put **Wait for previous actions to complete** beneath it. Without that, the next action in the block runs immediately, and a "show then change layout" pair swaps the layout out from under the ad.
- **There is no need to check whether an ad is loaded before showing it.** If it is not ready, *On Ad Failed to Show* fires. Branching on *Is Ad Loaded* is for the UI — hiding a reward button — not for guarding the show.
- **The rewarded ad has two separate conditions:** *On Rewarded ad user earned reward* and *On Rewarded ad canceled by user*. **Grant the reward under the first and only the first.** *On Rewarded Ad Dismissed* fires in both cases and is the wrong place.
- **When a show fails, Dismissed never fires.** Any "continue the game" logic under *On Ad Dismissed* needs the same blocks under *On Ad Failed to Show*, or the player is stuck on a screen that never advances.

### The banner is drawn over the canvas

**Show Banner Ad** takes an anchor position and `X offset` / `Y offset` in density-independent pixels. The banner is a native view above the game canvas — the Construct layout cannot see it, and no layer, Z order or viewport setting moves it.

- **The game's own layout has to leave the room.** A bottom banner covers whatever is at the bottom of the screen. Decide with the human what moves, as agreed in question 7.
- Changing the position is another **Show** action with new parameters, not a destroy and reload. **Show** may run before the ad is ready; it appears by itself when it arrives.
- Sizes: `Adaptive` (full width unless `Max Width` limits it), `Inline` (`Max Width` and `Max Height`, can be tall), `Smart` (320x50 on phones, 728x90 on tablets), `Leaderboard` (728x90, tablets only), `Standard` (320x50). `0` in a max field means no limit.
- **One size at a time.** Loading with a different size destroys the current banner and loads a new one.
- Adaptive and inline sizes are re-clamped on rotation, so the room the layout gives up has to hold in both orientations.

### Where each format belongs

- **Interstitial** — at a break the player is already taking: level complete, run over, a menu closed. Never mid-play, never in the tutorial, never right after a rewarded ad.
- **Rewarded** — only behind a button the player pressed, with the reward stated before the press. Hide or disable the button while *Is Rewarded Ad Loaded* is false.
- **Banner** — menus and idle screens; **Hide Banner Ad** when play starts.
- **MREC** — a pause screen, a results panel. It is large, and it does not belong over gameplay.
- **App Open** — on a return to the app, or on a cold start while the loading layout is still up. `AppOpen Ad Auto Show` handles the moment. Never on the very first launch before the game is playable, and never over another full screen ad.

**Pause the game while a full screen ad is up** — audio, physics, the game loop — and resume it under *On Ad Dismissed* **and** *On Ad Failed to Show*. Construct will keep running the layout behind the ad otherwise.

---

## 7. Consent and privacy

The built-in flow runs at startup when `Consent Flow Auto show if required` is on, which is the default and the right answer for most games.

- Conditions: **On Consent Form Dismissed**, **On Consent Required**, **On Consent Not Required**, **Is Consent Required**. Action: **Show Consent Form**, with an `If required only` property.
- **Privacy options button:** some forms require the user to be able to change their decision later. Check *Is Consent Required*, show a button in the settings layout, and under it perform **Show Consent Form** with `If required only` **false**. This is a compliance requirement, not a nicety.
- **Start attribution and analytics addons after the consent form is dismissed**, not before. Started earlier they cannot read the relevant identifiers, and the studio loses measurement and revenue. This is the one ordering rule with a money consequence.
- Never cache the consent status in the game. An expired consent then produces a TCF 3.3 error, and the SDK is the one that knows when to ask again.
- `Tracking Usage Description` is the iOS App Tracking Transparency text. Without it the IDFA is zeroed out and revenue drops, with nothing in the log. **The human writes it** — offer the examples from the CAS pages and do not word it for them.
- A game that targets children and adults needs a **neutral age screen**, an age gate that does not nudge the player to misrepresent their age. Say so; do not decide it.

The store questions have no Construct copy. Name them in the hand-over: the Google Play data disclosure, the App Store privacy questions, Apple's ad attribution, and the Apple Kids Category if it applies. Pages at `https://cleveradssolutions.docs.page/docs/general/`.

---

## 8. Verify

Nothing here can be verified from the editor. The checks, in order:

1. The human runs a **device build** per platform. Not the Construct preview, not a browser — there is no SDK in either.
2. `Debug Logging` on, and read the device log under the tag `CAS.AI` — `adb logcat` on Android, the Xcode console on iOS.
3. The test ad screen appears, and its **Mediation Adapters** section lists the networks that were chosen. Read it **on both platforms**; it is the proof the two solution properties are right, and on the Build Service route it is also the proof the plugin was not silently dropped.
4. Every format that was added loads **and** shows. A format that only loads is not done.
5. The rewarded reward arrives under *On Rewarded ad user earned reward* and nowhere else — watch an ad to the end, then dismiss one early and confirm nothing is granted.
6. The banner does not cover the game's own UI, in **both** orientations.
7. On the CLI route, you read the exported `config.xml` yourself and confirm `cordova-plugin-casai` is there.

Report the exact log lines. "It works" is not a result.

---

## 9. Before release

A checklist the human ticks, because every line is a property or a store answer, not code:

| What | Why |
|---|---|
| `Force Test Ads` **off** | A release with test ads earns nothing. No build flag does this for you. |
| `Test Device IDs` cleared | They turn a release build into test ads on those devices. |
| `Debug Logging` off | It costs performance. |
| `CAS Id (iTunes Id)` is the real App Store id | Empty means test ads for ever, with no error. |
| A solution in **both** Integration groups | One side without it earns nothing, silently. |
| Adapter lists spelled per platform | iOS `GoogleAds`, Android `googleAds`. |
| `Use Ad ID` matching the audience | `false` for a children-only game, and for builds without Google Play Services. |
| `Tracking Usage Description` filled | Without it the IDFA is zeroed and revenue drops. |
| Build Service: the plugin version is whitelisted | Otherwise the shipped binary has no CAS in it at all. |
| The app registered in CAS | Until then nothing serves. |

---

## 10. Report back

Tell the human: which properties you told them to set and their values, per platform; which event blocks were added and where; whether another ad addon was found and what was removed; which build route was used and whether the plugin was confirmed present in the export; what the device log showed; what is still manual — CAS registration, turning `Force Test Ads` off, the store privacy answers, the App Store id if it is still empty; and any step you could not complete.

Say plainly which parts you did **not** do, because on this platform that is most of them: you did not install the addon, you did not set the properties, and you did not author the event blocks. The human did. Your part was deciding what is correct and checking the result.
