# CAS Publishing Hub integration guide for AI agents (Unity)

You are an AI coding agent working for a game studio in the **CAS Publishing** program. The CAS Publishing Hub (an Editor installer package) puts the CAS SDK, its adapters and the publishing settings into a Unity project. This guide drives the Hub **without opening the Unity Editor window and without anyone clicking through the Hub's wizard**. The result is the same as the wizard's: the packages installed and configured, an ads manager in the code, purchases reported, ads placed where the game needs them.

Everything runs Unity in **batch mode** and drives the Hub through three `-executeMethod` entry points. The Hub deliberately does **nothing** on its own in batch mode (a safety rule, not an omission): only the commands in this guide make it act. You start Unity either directly (Option 1, default) or through the optional Unity CLI (Option 2); see step 1.

(Studios that came only for ad mediation, without the publishing program, use the separate CAS SDK guide.)

Rules for the whole guide:
- Never invent values. The CAS ID, the ad formats and the file paths come from the human.
- Show the human what you are about to change (files, packages, settings) before changing it, and wait for a clear yes. The reply to the step 0 block (`ok` or the chosen letters) is that yes for everything the block named; ask again only for a change it did not cover.
- Never run Unity in batch mode on a project that is open in an Editor.
- Do not delete files you did not create, with one exception: the files of another ad SDK that the human approved for removal in step 1b. The Hub removes its own legacy files itself; do not do that by hand.
- Do not initialize any mediated ad network SDK yourself. CAS does it.

Work in this order. Stop and ask the human whenever this guide says so.

---

## 0. Research the project, then ask

**Research first, ask second.** Everything that can be read from the project is read, not asked. Do the read-only checks below (no changes, no questions yet), then put what is left into **rounds of at most 3 questions** in the question format below.

This guide runs on its own: it researches the project itself and asks for everything it needs. Nothing is handed over from another guide. (The Firebase side of the studio is a separate guide, `firebase-ga4-setup.md`; do not run it from here.)

### Question format

This format is used for **every** question in this guide, not only in step 0.

- One question = one short bold line, then `Found:` (the evidence and where it came from, or "nothing found"), then a table of 2-4 ready options with a letter (two columns only: the letter and the answer; **no Recommended column**). Right after the table, one line: `**Recommended:** Option A - the reason in a few words`. Add a last option "Other" only when a free value is possible. When there is nothing to recommend (a value that cannot be guessed), write `**Recommended:** none - <why>`.
- The Recommended option is what the research found. If nothing was found, the safest default.
- Do not ask what the research settled. List it under "Found" so the human can object.
- **Rounds of at most 3 questions.** Numbering runs through all rounds (Q1-Q3, then Q4-Q6, and so on); title each round "Round 1 of 4". Send one round, wait for the reply, then send the next. Order the questions so that one an earlier answer decides comes after it (the Hub install method after "only the Hub or both", for example). Skip a question that an earlier answer made pointless.
- **Separate questions with empty lines** so the round is easy to read: an empty line between the bold question line, `Found:`, the table and the `Recommended:` line, and at least one empty line (better a line with `---`) between one question and the next. Never glue two questions together.
- End each round with: "Reply `ok` to accept every Recommended answer of this round, or list the changes, for example `2B`."
- `ok` accepts every Recommended answer of the round. A question with `Recommended: none` (a value only the human has, for example a Firebase project id that is not in the project, or a CAS ID that is still a `com.DefaultCompany.*` template) stays open after `ok`: ask only that question again, in a short follow-up, before the next round. Do not re-ask anything that `ok` settled.
- After the reply, say in one line what was chosen and start at once. No second confirmation of the answers.
- A question that appears later (an error, a result) uses the same format, one question per message.

Example:

> **Q1. Which platform should this pass configure?**
> Found: Android is the active build target (`Library/EditorUserBuildSettings.asset`).
>
> | Option | Answer |
> |---|---|
> | A | Android |
> | B | iOS |
> | C | Both, as two passes |
>
> **Recommended:** Option A - the active build target.

### Research (read-only)

| What | Where to look |
|---|---|
| Project root and Unity version | The current folder if it has `ProjectSettings/ProjectVersion.txt`, else the nearest parent that has it. Never ask for the path when you find it. |
| Active platform | `Library/EditorUserBuildSettings.asset`, field `m_ActiveBuildTarget`: `13` = Android, `9` = iOS. No `Library/` yet: use the platform that has an identifier or a Firebase config file below. |
| Package name / bundle id | `ProjectSettings/ProjectSettings.asset`, `applicationIdentifier`, keys `Android` and `iPhone`. A value like `com.DefaultCompany.*` is a template: it is not the real id. |
| CAS already set | `**/CASSettings*.asset` (`managerIds`, `testAdMode`); `com.cleversolutions.ads.unity` in the manifest. |
| Firebase config | `**/google-services.json` (`project_info.project_id`, `client[].client_info.android_client_info.package_name`) and `**/GoogleService-Info.plist` (`PROJECT_ID`, `BUNDLE_ID`). Skip `Library/`, `Temp/`, `Packages/`. Check that the package name matches `applicationIdentifier`. |
| Hub already installed | `ai.cas.installer` in the manifest; another installer (for example `com.psvgamestudio.installer`) = step 1, item 5. |
| Purchases | `com.unity.purchasing` and its version in the manifest; other payment packages in the manifest. |
| Ad formats the game uses now | The calls of an existing ad SDK in the game's C# (`ShowRewarded`, `ShowInterstitial`, banner, app open), if there is one. |
| Other ad SDKs | The read-only search of step 1b. |
| Git and tools | `git --version`, `git status`; `unity --version` (and `%LOCALAPPDATA%\Unity\bin\unity.exe` on Windows); `Temp/UnityLockfile` (Editor open). |
| Newest Hub version, bootstrap file | `GET https://npm.cas.ai/ai.cas.installer`; a file `cas-publishing-bootstrap.unitypackage` in the project or the Downloads folder. |

First show the human a short **Found** list (project, Unity version, platform, package name, Firebase project id, what is installed). Then the questions.

### Questions

Ask only those that still apply. The last column is a rule for you, not part of what the human sees: it says how to pick the Recommended option, which you show as a line under the table.

| # | Question | Answers | Recommended is |
|---|---|---|---|
| 1 | Which platform should this pass configure? | A. the active platform; B. the other platform; C. both, as two passes | A, the platform found. Nothing found: the platform that has an identifier or a Firebase file, else Android. |
| 2 | Which CAS ID? | Android: A. the package name found; B. another value. iOS: A. `demo` (not on the App Store yet); B. the numeric App Store id | The package name from the project. A `com.DefaultCompany.*` name or none: ask for the real one. iOS: the id already in `CASSettingsIOS.asset`, else `demo`. |
| 3 | Is the app already registered in CAS? | A. No, or do not know: Test ad mode ON until it is; B. Yes: Test ad mode OFF | A. Until CAS registers the app, the game builds only with Test ad mode on (step 3b); CAS contact registers it. |
| 4 | How to install the Hub? | A. Package registry `https://npm.cas.ai/` (no `git` needed); B. Git URL of `CAS-Publishing-Hub` (only if `git` is found); C. bootstrap `.unitypackage` (only if the file is found) | A. Not asked when the Hub is already installed. |
| 5 | Which ad formats? | A. banner, interstitial, rewarded; B. the same plus app open; C. another combination | The formats the old ad code uses, if any; else A. |
| 6 | The Firebase config file | Asked only when none is found. A. skip now, add it later by running step 3 again; B. I give you the path | A. |
| 7 | Place ads in the game code? | A. No; B. Yes, propose placements first (step 6) | A. Step 6 runs only on an explicit B. |
| 8 | Report purchases to CAS? | Asked only when Unity IAP is found. A. Yes, add `CASHub.ReportPurchase` to the purchase callback (step 5); B. No | A. No purchases or another payment system: not asked, say so in the Found list. |
| 9 | How to start Unity? | A. plain Unity (nothing to install); B. Unity CLI (beta); if it is not installed, B means the human installs it (command in step 1, item 6) | A. |
| 10 | Version control | Not a git repo: A. backup copy of `Assets/` and `Packages/` outside the project; B. stop, I commit first. Uncommitted changes: A. go on, with a backup copy; B. stop, I commit first | A. Clean tree: not asked. |
| 11 | Another ad SDK found: replace it with CAS? | Show a table (SDK, where, how many call sites). A. Yes, remove it and move its calls to CAS (step 5b); B. No, stop | A. CAS cannot run next to another ad SDK. Nothing found: not asked. |

---

## 1. Preflight

The read-only checks of this step were already done in step 0 and their questions asked there (version control = Q10, how to start Unity = Q9). Here you act on the answers.

1. **The Editor must be closed** on this project. If `Temp/UnityLockfile` exists, the Editor is open: ask the human to save and close it (one line, they reply "done"), and wait. Two Unity processes on one project corrupt `Library/`.
2. **Version control.** The Hub refuses to delete legacy files that git could not restore: a clean tree is what makes migrations safe. Act on the answer to Q10: for a backup copy, copy `Assets/` and `Packages/` to a folder outside the project and tell the human where it is; if the human chose to stop, stop.
3. **Back up** `Packages/manifest.json`.
4. **Find the Unity binary** named in `ProjectSettings/ProjectVersion.txt`: Windows `C:\Program Files\Unity\Hub\Editor\<version>\Editor\Unity.exe`, macOS `/Applications/Unity/Hub/Editor/<version>/Unity.app/Contents/MacOS/Unity`, Linux `~/Unity/Hub/Editor/<version>/Editor/Unity`. Below it is called `$UNITY`.
   - **Check the version.** The Hub needs Unity **2022.3 or newer**. On Unity 6.3 it needs **6000.3.5f2 or newer**: earlier 6.3 builds report valid package signatures as invalid. If the project is older, stop and tell the human that Unity must be upgraded first; do not install on an unsupported version.
   - The machine needs internet access: the Hub talks to the package registry.
5. **Is another Hub already in the project?** If `Packages/manifest.json` has an installer from another registry (for example the older `com.psvgamestudio.installer`), **stop and ask the human**: do not mix two installers.

6. **How Unity is started: the answer to Q9. There are two options; the Hub works with both, and the Editor is closed in both.**
   - **Option 1, plain Unity (default, nothing to install):** you call the editor binary from step 4 directly.
   - **Option 2, Unity CLI:** `unity run` starts the same batch run. It finds the editor by itself (no binary path to look up), works the same on Windows, macOS and Linux, and with `--allow-install` it installs the project's editor version if it is missing. It is a **beta** tool from Unity.

   Taking Option 2 is never required for anything in this guide. If the human chose to install the CLI (Q9, B), the official commands (Unity's own, shown by `unity skill show`): Windows PowerShell `$env:UNITY_CLI_CHANNEL='beta'; irm https://unity.com/install.ps1 | iex`; macOS or Linux `curl -fsSL https://unity.com/install.sh | UNITY_CLI_CHANNEL=beta bash`. These download and run a script: let the human run them in their own terminal, do not run them yourself unasked. Then open a new terminal so `unity` is on the PATH and check `unity --version`. If anything in Option 2 fails or behaves oddly, say so and go back to Option 1.

   Do not use the CLI's commands for a **running** Editor (`unity command ...`) with this guide: the Hub only acts in batch mode.

Every Unity command in this guide is written for Option 1 and has this shape; keep all five flags:

```
$UNITY -batchmode -nographics -quit -projectPath "<project root>" -buildTarget <Android|iOS> \
       -logFile - -executeMethod <method> [extra args]
```

**Option 2 form of the same command**:

```
unity run "<project root>" --no-tail -l <log file> -- -buildTarget <Android|iOS> -executeMethod <method> [extra args]
```

The arguments after `--` go to the editor; read the log file for the `[CAS Hub] REPORT` line, and read `Library/CASHub/report.json` as before.

`-logFile -` prints the Editor log to stdout. Each Hub command also writes `Library/CASHub/report.json` and echoes it on a log line starting with `[CAS Hub] REPORT `; the exit code is `0` on success and `1` on failure. **Read the report after every run.** A run takes 1 to 5 minutes (package resolution, script compilation); wait for it.

If a run fails with C# compile errors that are not in `Packages/`, the game's own code does not compile: fix that first (or ask the human). The Hub cannot run on a project that does not compile.

---

## 1b. Detect other ad SDKs (read-only; done in step 0, before installing anything)

CAS cannot live next to another mediation or ad network SDK. Two ad SDKs in one game duplicate the native libraries (the Android build fails), initialize twice and ask for consent twice. CAS already includes adapters for the common networks, so the other SDK has to be **replaced**. The Hub removes its own legacy files and the old Unity Ads package (`com.unity.ads`); everything else is yours to find. Change nothing yet.

**Where to look** (use the file search tools, not your memory of the project):
- `Packages/manifest.json`: dependencies and scoped registries that contain `applovin`, `ironsource`, `levelplay`, `google.ads` / `admob`, `appodeal`, `unity.ads`, `yandex`, `chartboost`, `vungle`, `liftoff`, `pangle`, `mintegral`, `inmobi`, `fyber`, `digitalturbine`, `ogury`, `bidmachine`, `adcolony`, `startapp`, `tapjoy`, `smaato`.
- `Assets/` folders and files with the same names (for example `MaxSdk`, `GoogleMobileAds`, `IronSource`, `LevelPlay`, `Appodeal`), their `*Dependencies.xml` for the External Dependency Manager, and their native libraries in `Assets/Plugins/Android` and `Assets/Plugins/iOS`.
- Code (the game's C#, not `Packages/` or `Library/`): `MaxSdk.`, `IronSource.`, `LevelPlay`, `GoogleMobileAds`, `Advertisement.Initialize`, `Appodeal.`, and any wrapper class of the game around them.
- App IDs and keys these SDKs put in `Assets/Plugins/Android/AndroidManifest.xml` or `Info.plist` (for example `com.google.android.gms.ads.APPLICATION_ID`, `GADApplicationIdentifier`, `AppLovinSdkKey`).

**What is not an ad SDK, do not touch it:** the External Dependency Manager (EDM4U), Firebase, attribution and analytics (AppsFlyer, Adjust, Tenjin and similar), in-app purchases. If you are not sure whether something is an ad SDK, or it is a consent SDK (for example Google UMP), **ask the human**.

Also look for an existing CAS in the project (`com.cleversolutions.ads.unity` or `Assets/CleverAdsSolutions`). Do not ask about it: in the publishing program the project always gets the **Publishing** solution (`PublishingAds`), and the Hub switches to it itself. Just mention in the report that a CAS was already there.

**If nothing is found:** say so and go to step 2.

**If another ad SDK is found**, it goes into the step 0 block as Q11, with a table (SDK, where it is: package or folder, how many places in the code call it). If the answer is "no, stop", **stop** and tell the human that CAS cannot run next to it. The replacement itself is step 5b.

---

## 2. Bootstrap: put the Hub into the project

Do the option the human chose in step 0. Keep every existing entry in the manifest; merge, do not overwrite. Skip this step when the installer and the metadata package are already in `dependencies`.

**A. Package registry.** Add the scoped registry and the two packages to `Packages/manifest.json`:

```jsonc
{
  "scopedRegistries": [
    { "name": "CAS Publishing", "url": "https://npm.cas.ai/", "scopes": [ "ai.cas" ] }
  ],
  "dependencies": {
    "ai.cas.installer": "<latest>",
    "ai.cas.installer.metadata": "<same version>"
  }
}
```

The latest version is the `latest` tag of `GET https://npm.cas.ai/ai.cas.installer`. Use the **same** version for both packages.

**B. Git URL.** Add the installer from the repository `cleveradssolutions/CAS-Publishing-Hub`, path `/installer`, in the form `https://github.com/cleveradssolutions/CAS-Publishing-Hub.git?path=/installer#<version>` (confirmed by the repository README, which uses the `#<version>` form; a release is a commit on `main` tagged with its version; git installs do not update themselves, so to upgrade change the `#<version>` suffix). The metadata package still comes from the registry, so also add the registry and `ai.cas.installer.metadata` as in option A.

**C. Bootstrap `.unitypackage`** (`cas-publishing-bootstrap.unitypackage`, named in the Hub's user guide). The human imports it once in the Editor (Assets > Import Package > Custom Package); it writes the registry and the installer dependency into `manifest.json` and deletes itself. Then the human saves and **closes the Editor**, and you check that `manifest.json` now has both packages (add `ai.cas.installer.metadata` yourself if only the installer is there).

The metadata package must be in the manifest next to the installer: in batch mode the Hub does not fetch its catalog by itself. The installer adds the scopes `com.cleversolutions`, `com.tenjin` and `com.google` itself when it installs those components.

Then run Unity once with no `-executeMethod` so it resolves the packages:

```
$UNITY -batchmode -nographics -quit -projectPath "<project root>" -logFile -
```

`Library/PackageCache/ai.cas.installer@…` exists afterwards: continue.

The installed package ships the Hub's own agent guide, `Library/PackageCache/ai.cas.installer@*/AGENTS.md`. This guide is the main one and wins; the package file is only a reference. Open it only when a command from this guide fails (for example "method not found" for a `HeadlessHub` entry point) to see the current method name or argument, and tell the human about the difference instead of silently switching to the package guide's flow.

---

## 3. Install: the SDK components

```
$UNITY -batchmode -nographics -quit -projectPath "<project root>" -buildTarget <platform> \
       -logFile - -executeMethod CAS.Publishing.Installer.Wizard.HeadlessHub.Install
```

One run is one Scan, Plan, Apply pass over the default component set (CAS SDK, Firebase Analytics, EDM4U, the adapters the catalog lists). It writes `Packages/manifest.json`; Unity resolves the new packages on its **next** launch. So loop:

- `state: "resolving"`: run the same command again.
- `state: "installed"`: done; go to step 3a.
- `state: "failed"`: read `messages`. Typical causes:
  - the metadata catalog is missing: step 2 was incomplete;
  - `refusing to delete` on a path: a legacy install (loose SDK files under `Assets/`) is untracked or modified, so the Hub will not delete it. **Do not delete those files yourself.** Show the human the paths from the report and ask them to commit or restore the files, then run again;
  - a registry or network error: report it to the human word for word.

Cap the loop at 6 runs; if it still says `resolving`, stop and show the human the last report. The report also lists `actions` (what changed in the manifest); keep them for your final summary.

### 3a. Configure: CAS ID, ad formats, Firebase

Write a config file (anywhere; for example `Library/CASHub/config.json`) from the answers of step 0:

```json
{
  "platform": "Android",
  "casId": "com.company.game",
  "adFormats": ["banner", "interstitial", "rewarded"],
  "firebaseConfig": "path/to/google-services.json"
}
```

| Key | Required | Notes |
|---|---|---|
| `platform` | no | `Android` / `iOS`; default: the `-buildTarget`. Pass the same value to `-buildTarget`. |
| `casId` | first pass | Validated per platform. Omit on later passes to keep the stored one. |
| `adFormats` | no | Any of `banner`, `interstitial`, `rewarded`, `appOpen`. Default: the first three. Enable only what the game uses. |
| `firebaseConfig` | no | Absolute or project-relative path; validated and copied into `Assets/`. Omit it if the human has no file yet. |

Do not write `audience` and `keepExistingSolution`: the publishing program takes no children's games, and the solution is always Publishing. Do not ask the human about Families or about the mediation solution.

```
$UNITY -batchmode -nographics -quit -projectPath "<project root>" -buildTarget <platform> \
       -logFile - -executeMethod CAS.Publishing.Installer.Wizard.HeadlessHub.Configure \
       -casHubConfig Library/CASHub/config.json
```

This goes through the same writers as the wizard's Placements and Firebase steps: the CAS settings asset, the Publishing and Tenjin MMP mediation solutions (their EDM4U dependency files), the Firebase config in `Assets/`. `state: "configured"`: continue. `failed`: the message names the bad value; fix the config and run again. Run once per platform the game ships on.

### 3b. Test ad mode (if the app is not registered in CAS yet)

Until CAS registers the app, the game can only be built with **Test ad mode on**. The Hub config has no key for it, so edit the CAS settings asset the Hub just wrote, **with Unity closed**: `Assets/CleverAdsSolutions/Resources/CASSettingsAndroid.asset` (or `CASSettingsiOS.asset`). Change only this line, and only if it is present in the file:

```
  testAdMode: 1
```

Leave every other line alone. After CAS registers the app, the human sets it back to `testAdMode: 0` (or unticks **Test ad mode** in `Assets > CleverAdsSolutions > Android Settings...` / `iOS Settings...`) and builds again. Never ship a build with test ads. If the app is already registered, leave `testAdMode: 0`.

---

## 4. Verify

```
$UNITY -batchmode -nographics -quit -projectPath "<project root>" -buildTarget <platform> \
       -logFile - -executeMethod CAS.Publishing.Installer.Wizard.HeadlessHub.Status
```

`ready: true` is the same green state the wizard's Configuration screen shows. If not: `pendingInstall` lists what step 3 would still do, `configuration[].cells[]` shows which value is `Missing` / `NotConfigured`, `androidTemplatesMissing` names build templates step 3 creates. Go back to the step that owns it. A `settingsAssetField` cell is the CAS ID; `assetFile` cells are the Firebase config. A missing Firebase config that the human has not provided yet is expected: say it in the report.

---

## 5. Ads manager in the code

First look for one. Search the game's C# (not `Packages/`, not `Library/`) for: `MobileAds.BuildManager`, `IMediationManager`, `CAS.`, `AdsManager`, `AdManager`, `AdService`, `ShowInterstitial`, `ShowRewarded`, and the other ad SDKs of step 1b.

- **A CAS manager exists:** leave it. Make sure its builder chain calls `.WithPublishingSettings()` (namespace `CAS.Publishing.Installer.Runtime`) before `.Initialize()`; that is what hands a stored Tenjin key to CAS (without one, CAS takes it from the CAS dashboard). Add the call if it is missing; nothing else.
- **Another ad SDK's manager exists:** step 1b already got the human's decision. The replacement is step 5b; do not create a second manager in parallel.
- **Nothing:** copy the template from the installed package, `Samples~/AdsManager/AdsManager.cs` (under `Library/PackageCache/ai.cas.installer@*/`), into the game: `Assets/Scripts/Ads/AdsManager.cs`, or next to the game's other managers if it has a folder for them. It needs no scene setup: it bootstraps itself before the first scene loads and initializes CAS with the Hub's settings. Edit only the constants at the top of the file (banner on start, size, position). Its API:

```csharp
AdsManager.ShowInterstitial("level_complete");            // bool: shown or not
AdsManager.ShowRewarded("double_coins", earned => …);     // callback fires exactly once
AdsManager.ShowBanner();  AdsManager.HideBanner();
AdsManager.IsRewardedReady                                // enable/disable the button
```

If the project uses Assembly Definitions and the compiler says `error CS0103: The name 'MobileAds' does not exist in the current context`, add an Assembly Definition Reference to the assembly `CleverAdsSolutions` for the assembly that holds the code.

### Purchases

Act on the human's answer from step 0, Q8:
- **No purchases in the game:** do nothing here; say "no purchases, nothing added" in the report.- **Purchases through Unity IAP** (the manifest has `com.unity.purchasing`; if the human said Unity IAP and it is not there, ask): report every purchase to CAS with the one-liner instead of hand-written receipt parsing; CAS forwards it to Tenjin, nothing else to call. Show the human the file and the line before adding it.
- **Another payment system:** leave it alone, see the end of this section.

The one-liner:

```csharp
using CAS.Publishing.Installer.Runtime;
// Unity IAP 5, in the purchase-pending callback, before confirming the order:
CASHub.ReportPurchase(product, pendingOrder);
// Legacy IStoreListener.ProcessPurchase / Unity IAP 4:
CASHub.ReportPurchase(args.purchasedProduct);
```

Find the existing purchase callback and add the call there; do not restructure the store code. Another payment system: leave it alone (the human can wire `CAS.PurchaseInfo` by hand).

---

## 5b. Replace the old ad SDK (only if step 1b found one and the human agreed)

Do this after the Hub is installed and configured and the ads manager of step 5 exists. Do the removal and the code changes in one pass, then compile once and fix the errors in batches; every compile is a batch run of 1 to 5 minutes.

First, **before you delete anything**, collect what you will need later and keep it in your notes:
- the `guid` of every script and prefab of the old SDK (from its `.meta` files): you will need them to find "Missing script" references;
- the library names the old SDK pulled in for Android (from its `*Dependencies.xml`, for example `com.applovin:applovin-sdk`): you will need them to find leftover `.aar` and `.srcaar` files.

1. **Remove the SDK itself**, only the items you listed in step 1b and the human approved: its entries in `Packages/manifest.json` (and its scoped registry if nothing else uses it); its folders and files under `Assets/` together with their `.meta` files, its `*Dependencies.xml`, its native libraries in `Assets/Plugins/Android` and `Assets/Plugins/iOS`. Never delete anything outside the approved list. Keep EDM4U, Firebase, attribution, analytics and purchase SDKs.
2. **Remove the keys it left** in `Assets/Plugins/Android/AndroidManifest.xml` and `Info.plist` (for example the AdMob or AppLovin app ID) only if they are clearly that SDK's, and tell the human each one you removed. If unsure, leave it and report it.
3. **Move the game's ad calls to CAS.** The calls the game already has are the list of placements: every old interstitial, rewarded, banner and app open call becomes the matching call of the ads manager (step 5), in the same place, with the same reward and callback logic. If the game has its own ad class, keep it and change only what is inside. Old initialization and old ad unit IDs go away: CAS does not use them. Do not copy IDs into CAS.
4. **Leftover Android libraries.** The External Dependency Manager downloaded the old SDK's libraries into `Assets/Plugins/Android` as `.aar` or `.srcaar` files. Deleting the manifest entry does not remove them, and they duplicate the CAS libraries. Delete exactly the files whose names match the libraries you noted. Also remove the old SDK's lines from `mainTemplate.gradle`, `baseProjectTemplate.gradle` and `launcherTemplate.gradle` if they are clearly its own. Then run the Hub's `Install` and `Configure` again (step 3): they recalculate the dependencies and the Hub resolves the Android ones through CAS.
5. **"Missing script" in scenes and prefabs.** Components of the removed SDK now point to GUIDs that no longer exist. Search `Assets/**/*.unity` and `Assets/**/*.prefab` for the GUIDs you noted and list each file to the human. Do not edit scene YAML by hand unless the human agrees; the safe way is a small Editor script run in batch mode that opens the scene or prefab and calls `GameObjectUtility.RemoveMonoBehavioursWithMissingScript` on its objects, then saves it.
6. **Compile** (step 7). Fix only errors from the removed SDK. If a feature of the old SDK has no CAS counterpart (for example custom banner placement, server-side reward callbacks, an unusual consent setup), **stop**, list these places, and ask the human what to do.
7. **Check that nothing is left:** search again for the markers from step 1b in the code, `Assets/` and `manifest.json`.

---

## 6. Placing ads in the game (only on an explicit yes from step 0)

Do this only on an explicit request such as "add ads to the game". Then:

1. **Map the game loop first.** Find the natural breaks: level / round / match complete, game over, return to the main menu, pause then resume, a "continue?" prompt, shop, reward screens. Search for method and event names like `LevelComplete`, `OnWin`, `OnLose`, `GameOver`, `Restart`, `LoadNextLevel`, `ShowResults`, `OnContinue`, `Revive`, `DoubleReward`, `ClaimReward`, `FreeCoins`, scene loads and UI button handlers.
2. **Propose before editing.** Present the human a short table (file, method, ad type, placement name) and get a yes. Placement rules:
   - **Interstitial:** after a natural break is over (level complete, results shown, *then* the ad, before the next level loads). Never on the very first launch, never in the middle of gameplay, never in a tutorial, never twice in a row with a rewarded ad. One call per break; CAS itself enforces the interval between interstitials.
   - **Rewarded:** only behind a button the player presses for an optional benefit (revive, double reward, free currency, skip a wait). Grant the benefit in the callback, only when `earned` is true. Disable or hide the button when `AdsManager.IsRewardedReady` is false.
   - **Banner:** on menu / idle screens; hide it when gameplay starts and on every full-screen UI that would overlap it.
   - Placement names are short snake_case words that describe the spot (`level_complete`, `revive`, `main_menu`); they show up in the CAS dashboard.
3. **Edit minimally.** One line per placement, next to the existing logic; no refactoring of the game's flow, no new managers besides `AdsManager`.
4. **Compile** (step 7). Fix what you introduced.

---

## 7. Build check

- Run Unity once more in batch mode with no `-executeMethod` and search the log for `error CS`. Fix only what you introduced.
- Test ads: if the app is not registered in CAS yet, `testAdMode: 1` is set (step 3b). A development build also gets test ads automatically. Remind the human: **until CAS registers the app, the game can only be built with Test ad mode on.** After registration: `testAdMode: 0` and build again.
- If you used test devices in code (`CAS.MobileAds.settings.SetTestDeviceIds`), remove that line before release.
- Read the CAS pages **User Consent Flow** and **Privacy Regulations** (`https://docs.page/cleveradssolutions/docs/Unity/`) and tell the human what they need to provide (privacy policy link, the App Tracking Transparency text on iOS). Do not change the consent defaults without the human's decision.

---

## 8. Report back

Tell the human, in this order: whether another ad SDK was found and what you removed and rewrote (steps 1b and 5b), the packages added or updated (from the step 3 reports), the configuration applied per platform (CAS ID, formats, test ad mode), the files you added or changed in the game, whether `CASHub.ReportPurchase` was added and in which file (or "no purchases" / "another payment system, not touched"), the placements made (if any), and what still needs a human: after every new build the studio makes a test purchase again, otherwise Tenjin does not see purchases; CAS registering the app and turning Test ad mode off, store console setup, the other platform's pass, the Firebase config file if it is still missing, the App Tracking Transparency text on iOS. Suggest `git diff` for review.
