# CAS SDK integration guide for AI agents (Unity, ad mediation)

You are an AI coding agent working for a game studio that is integrating the CAS SDK (CAS.AI ad mediation) into a Unity project. Do exactly the steps below, in order. **Stop and ask the human whenever this guide says so.**

Rules for the whole guide:
- Never invent values. A value comes from the project (step 0 research) or from the human, never from a guess.
- Do not initialize any mediated ad network SDK yourself. CAS does it. Doing it breaks the integration.
- Show the human what you are about to change (files, packages, settings) before changing it, and wait for a clear yes. The reply to the step 0 block (`ok` or the chosen letters) is that yes for everything the block named; ask again only for a change it did not cover.
- Do not delete files you did not create, with one exception: the files of another ad SDK that the human approved for removal in step 1b.
- Never run Unity in batch mode on a project that is open in an Editor.

---

## 0. Research the project, then ask

**Research first, ask second.** Everything that can be read from the project is read, not asked. Do the read-only checks below (no changes, no questions yet), then put what is left into **rounds of at most 3 questions** in the question format below.

### Question format

This format is used for **every** question in this guide, not only in step 0.

- One question = one short bold line, then `Found:` (the evidence and where it came from, or "nothing found"), then a table of 2-4 ready options with a letter (two columns only: the letter and the answer; **no Recommended column**). Right after the table, one line: `**Recommended:** Option A - the reason in a few words`. Add a last option "Other" only when a free value is possible. When there is nothing to recommend (a value that cannot be guessed), write `**Recommended:** none - <why>`.
- The Recommended option is what the research found. If nothing was found, the safest default.
- Do not ask what the research settled. List it under "Found" so the human can object.
- **Rounds of at most 3 questions.** Numbering runs through all rounds (Q1-Q3, then Q4-Q6, and so on); title each round "Round 1 of 4". Send one round, wait for the reply, then send the next. Order the questions so that one an earlier answer decides comes after it. Skip a question that an earlier answer made pointless.
- **Separate questions with empty lines** so the round is easy to read: an empty line between the bold question line, `Found:`, the table and the `Recommended:` line, and at least one empty line (better a line with `---`) between one question and the next. Never glue two questions together.
- End each round with: "Reply `ok` to accept every Recommended answer of this round, or list the changes, for example `2B`."
- `ok` accepts every Recommended answer of the round. A question with `Recommended: none` (a value only the human has, for example a CAS ID that is still a `com.DefaultCompany.*` template) stays open after `ok`: ask only that question again, in a short follow-up, before the next round. Do not re-ask anything that `ok` settled.
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
| Active platform | `Library/EditorUserBuildSettings.asset`, field `m_ActiveBuildTarget`: `13` = Android, `9` = iOS. No `Library/` yet: use the platform that has an identifier below. |
| Package name / bundle id | `ProjectSettings/ProjectSettings.asset`, `applicationIdentifier`, keys `Android` and `iPhone`. A value like `com.DefaultCompany.*` is a template: it is not the real id. |
| CAS already in the project | `com.cleversolutions.ads.unity` in the manifest or `Assets/CleverAdsSolutions`; `**/CASSettings*.asset` (`managerIds`, `testAdMode`, `audienceTagged`); which mediation solution is active (step 4). |
| Latest stable CAS release | OpenUPM page of `com.cleversolutions.ads.unity`, or the latest release tag from `https://github.com/cleveradssolutions/CAS-Unity/releases`. |
| Ad formats the game uses now | The calls of an existing ad SDK in the game's C# (`ShowRewarded`, `ShowInterstitial`, banner, app open), if there is one. |
| Other ad SDKs | The read-only search of step 1b. |
| Git and tools | `git --version`, `git status`; `unity --version` (and `%LOCALAPPDATA%\Unity\bin\unity.exe` on Windows); if the CLI is there, `unity status` (is an Editor connected, is the Pipeline package present); `Temp/UnityLockfile` (Editor open). |

First show the human a short **Found** list (project, Unity version, platform, package name, what is already installed). Then the questions.

### Questions

Ask only those that still apply. The last column is a rule for you, not part of what the human sees: it says how to pick the Recommended option, which you show as a line under the table.

| # | Question | Options | Recommended is |
|---|---|---|---|
| 1 | Which platform should this pass configure? | A. the active platform; B. the other platform; C. both, as two passes | A, the platform found. Nothing found: the platform that has an identifier, else Android. |
| 2 | Which CAS ID? | Android: A. the package name found; B. another value. iOS: A. `demo` (not on the App Store yet); B. the numeric App Store id (digits only, from `apps.apple.com/.../id123456789`) | The package name from the project. A `com.DefaultCompany.*` name or none: `none`, ask for the real one. iOS: the id already in `CASSettingsiOS.asset`, else `demo`. |
| 3 | Is the app already registered in CAS? | A. No, or do not know: Test ad mode ON; B. Yes: Test ad mode OFF | A. Until CAS registers the app, the game builds only with Test ad mode on (step 7); the human's CAS contact registers it. |
| 4 | How to install the SDK, and which release? | A. OpenUPM, latest stable release (a scoped registry, no `git`, pins the version in `packages-lock.json`); B. Git URL with a pinned tag (needs `git`; the only way to get a beta); C. the Package Manager window, done by the human (same as B); D. a downloaded `.unitypackage` (use it only if the human already has one from CAS) | A with the latest stable version found. B only if the human asks for a beta. Not asked when the SDK is already installed. |
| 5 | Is the game in the Google Play Families category? | A. No; B. Yes | A. Most apps are not Families. If nothing in the project says otherwise, or the human does not know, count it as no. |
| 6 | Which ad formats? | A. banner, interstitial, rewarded; B. the same plus app open; C. another combination | The formats the old ad code uses, if any; else A. |
| 7 | Write the ad code? | A. No, write no ad code at all: the human adds it; B. Yes, initialize CAS and propose the placements first (steps 5 and 8) | A. Steps 5 and 8 run only on an explicit B (or when step 5b needs them). |
| 8 | How to work with Unity? | A. Mode A: Editor closed, plain batch mode; B. Mode B: Editor open, Unity CLI (only when the CLI is installed, an Editor is connected and the Pipeline package is there; if the CLI is missing, B means the human installs it, commands in step 1); C. Mode B, and add the Pipeline package (only when the CLI is installed but the project has no Pipeline package) | A. The CLI is optional and everything works without it. |
| 9 | Version control | Not a git repo: A. backup copy of `Assets/` and `Packages/` outside the project; B. stop, I commit first. Uncommitted changes: A. go on, with a backup copy; B. stop, I commit first | A. Clean tree: not asked. |
| 10 | Another ad SDK found: replace it with CAS? | Show a table (SDK, where, how many call sites). A. Yes, remove it and move its calls to CAS (step 5b); B. No, stop | A. CAS cannot run next to another ad SDK. Nothing found: not asked. |

---

## 1. Preflight: Editor and Unity CLI

The read-only checks (Editor open, CLI installed, git state) were done in step 0 and their questions asked there (how to work with Unity = Q8, version control = Q9). Here you act on the answers.

1. **Is the project open in the Unity Editor?** Known from step 0 (`Temp/UnityLockfile` exists = open).
2. **Is the Unity CLI installed?** Known from step 0.
3. **The mode is the answer to Q8. The Unity CLI is optional. Mode A (Editor closed, no CLI) is the default and everything in this guide works with it.** Mode B (Editor open, Unity CLI) only makes iterations faster.
   - **Mode B:** use it only when the project has the Unity Pipeline package and the Editor is connected (`unity status` lists it with `"state": "ready"`). If the CLI is installed but no Editor is connected, ask the human (one line, they reply "done") to open the project, or to close Unity and use mode A.
   - **The human chose to install the CLI (Q8, B):** it is a **beta** Unity tool and needs Unity's `com.unity.pipeline` package in the project, an experimental version (`0.9.1-exp.1`). Official install commands (Unity's own, shown in `unity skill show`): Windows PowerShell `$env:UNITY_CLI_CHANNEL='beta'; irm https://unity.com/install.ps1 | iex`; macOS or Linux `curl -fsSL https://unity.com/install.sh | UNITY_CLI_CHANNEL=beta bash`. These download and run a script: let the human run them in their own terminal, do not run them yourself unasked. Open a new terminal afterwards so `unity` is on the PATH, check `unity --version`. If the human declines, use **mode A**.
   - **Add the Pipeline package to the project:** `unity pipeline install --project-path "<project root>"` (it adds `com.unity.pipeline` to `Packages/manifest.json`). A **running Editor picks the change up slowly when it is not the focused window**: poll `unity status` every 10 seconds for up to 3 minutes; if it still shows no instance, ask the human to click the Unity window once. Then continue in mode B.
   - If the project has C# compile errors, the Editor starts in **Safe Mode** and the Pipeline does not load: `unity status` shows no instance. Tell the human; either fix the errors or use mode A.
   - If anything in mode B fails or behaves oddly, say so and fall back to mode A.
4. **Mode A (closed Unity):** if `Temp/UnityLockfile` exists, ask the human to save and close the Editor, and wait. Find the editor binary named in `ProjectSettings/ProjectVersion.txt` (Unity Hub installs it under `Hub/Editor/<version>/Editor/`; on Windows `Unity.exe`).
5. **Version control:** act on the answer to Q9 (a backup copy of `Assets/` and `Packages/` outside the project, with its location told to the human; or stop). Back up `Packages/manifest.json` before step 2.

**What differs between the modes:**

| Action | Mode A (Editor closed) | Mode B (Editor open, Unity CLI) |
|---|---|---|
| Add the package | edit `Packages/manifest.json`, then run Unity once in batch mode | edit `Packages/manifest.json` (or `unity command package add --identifier <package> --confirm true`), then `unity command invoke_menu --path "Assets/Refresh"` and wait for `unity recompile` to finish |
| Edit the settings asset | edit the file; import happens at the next batch run | `unity command set_field --target "Assets/CleverAdsSolutions/Resources/CASSettingsAndroid.asset" --field <name> --value <v>`, then **save** with `unity command eval 'UnityEditor.AssetDatabase.SaveAssets();'`; or edit the file on disk and run `unity command invoke_menu --path "Assets/Refresh"` |
| Check that it compiles | run Unity in batch mode and look for `error CS` in the log | `unity recompile` (exit code `0` = clean; exit code `6` and lines like `error CS0029 <file> <line> <col> <message>` = errors) |
| Read the console | the batch log (`-logFile -`) | `unity command console get --level error --tail 20` |

**How mode B behaves** (Unity 6000.3, Pipeline `0.9.1-exp.1`):
- `set_field` changes the value in memory only; the file on disk changes after `AssetDatabase.SaveAssets()` (`eval`). After setting a field back, the file is byte-identical to the original.
- **An Editor that is not in the foreground does not notice edits of files on disk by itself** (nothing happened within 8 seconds); `Assets/Refresh` through the CLI fixes this at once.
- `unity recompile` reports compile errors, and exits `0` when there are none.
- `unity command save_all` can hang on an unsaved scene (it waits for a dialog in the Editor); do not use it.
- Commands with the same name in several groups need the group: `unity command console get`, not `unity command get`. List them with `unity command --query <word>`; arguments are passed as `--name value`.
- `unity command eval 'C# code'` runs C# in the Editor. In PowerShell write the code without inner double quotes, because they are stripped.

Batch-mode command (mode A): `<Unity> -batchmode -nographics -quit -projectPath "<project root>" -buildTarget <Android|iOS> -logFile -`.

---

## 1b. Detect other ad SDKs (read-only; done in step 0, before installing anything)

CAS cannot live next to another mediation or ad network SDK. Two ad SDKs in one game duplicate the native libraries (the Android build fails), initialize twice and ask for consent twice. CAS already includes adapters for the common networks, so the other SDK has to be **replaced**, not kept. Look for it now, change nothing yet.

**Where to look** (use the file search tools, not your memory of the project):
- `Packages/manifest.json`: dependencies and scoped registries that contain `applovin`, `ironsource`, `levelplay`, `google.ads` / `admob`, `appodeal`, `unity.ads`, `yandex`, `chartboost`, `vungle`, `liftoff`, `pangle`, `mintegral`, `inmobi`, `fyber`, `digitalturbine`, `ogury`, `bidmachine`, `adcolony`, `startapp`, `tapjoy`, `smaato`.
- `Assets/` folders and files with the same names (for example `MaxSdk`, `GoogleMobileAds`, `IronSource`, `LevelPlay`, `Appodeal`), plus their `*Dependencies.xml` files for the External Dependency Manager and their native libraries in `Assets/Plugins/Android` and `Assets/Plugins/iOS`.
- Code (the game's C#, not `Packages/` or `Library/`): `MaxSdk.`, `IronSource.`, `LevelPlay`, `GoogleMobileAds`, `Advertisement.Initialize`, `Appodeal.`, and any wrapper class of the game around them.
- App IDs and keys that these SDKs put in `Assets/Plugins/Android/AndroidManifest.xml` or `Info.plist` (for example `com.google.android.gms.ads.APPLICATION_ID`, `GADApplicationIdentifier`, `AppLovinSdkKey`).

**What is not an ad SDK, do not touch it:** the External Dependency Manager (EDM4U), Firebase, attribution and analytics (AppsFlyer, Adjust, Tenjin and similar), in-app purchases. If you are not sure whether something is an ad SDK, or it is a consent SDK (for example Google UMP), **ask the human**.

**If nothing is found:** say so and go to step 2.

**If something is found**, it goes into the step 0 block as Q10, with a table (SDK, where it is: package or folder, how many places in the code call it). If the answer is "no, stop", **stop** and tell the human that CAS cannot run next to it. Do not continue with a half-integration.

**Before changing anything:** the project is in git and committed, or the backup of `Assets/` and `Packages/` exists (Q9). The replacement itself is step 5b.

---

## 2. Install the CAS SDK package

Package name: `com.cleversolutions.ads.unity`. The method and the release are the answer to Q4. By default it is the latest stable release (on OpenUPM, or the latest release tag from `https://github.com/cleveradssolutions/CAS-Unity/releases`). Never pick a pre-release unless the human asked for it. **Default method: A (OpenUPM)**, unless the human chose another one or asked for a pre-release (betas are only available through B, Git URL).

- **A. OpenUPM (Recommended).** Add the scoped registry and the dependency:

```
"scopedRegistries": [
  { "name": "OpenUPM", "url": "https://package.openupm.com", "scopes": ["com.cleversolutions", "com.google.external-dependency-manager"] }
],
"dependencies": { "com.cleversolutions.ads.unity": "<version>" }
```

  Note: the OpenUPM release may be older than the GitHub one. Tell the human which version it installed. If the project already has an `OpenUPM` entry in `scopedRegistries`, add the scopes to it, do not create a second entry.
- **B. Git URL.** In `Packages/manifest.json`, under `dependencies`:

```
"com.cleversolutions.ads.unity": "https://github.com/cleveradssolutions/CAS-Unity.git#<release-tag>"
```

- **C. Unity Package Manager window.** Tell the human: Window > Package Manager > **+** > **Add package from git URL** > paste `https://github.com/cleveradssolutions/CAS-Unity.git#<release-tag>`.
- **D. `.unitypackage`.** The human imports it by Assets > Import Package > Custom Package. Do not guess its source.

Keep every existing entry in `manifest.json`; merge, do not overwrite. Then let Unity resolve the package (mode A: run Unity once in batch mode; mode B: wait). Check that `Library/PackageCache/com.cleversolutions.ads.unity@*` exists.

---

## 3. Settings asset (the agent edits the file)

CAS keeps one settings asset per platform in `Assets/CleverAdsSolutions/Resources/`:

- `CASSettingsAndroid.asset` for Android,
- `CASSettingsiOS.asset` for iOS.

It is a Unity YAML ScriptableObject. Fields you set (CAS 4.8.1-beta2):

| Field | Meaning | Value |
|---|---|---|
| `testAdMode` | Test ad mode | `1` while the app is not registered by CAS, otherwise `0` |
| `managerIds` | CAS ID list | one entry: the Android package name or the iOS numeric ID |
| `allowedAdFlags` | enabled ad formats, a bit mask | Banner `1`, Interstitial `2`, Rewarded `4`, App Open `8`; add the numbers of the chosen formats |
| `audienceTagged` | audience | `0` Mixed (the default), `1` Children |

Do not touch the other fields (`bannerSize`, `bannerRefresh`, `interstitialInterval`, `loadingMode`, `debugMode`, `trackLocationEnabled`, `interWhenNoRewardedAd`): keep the defaults.

Examples of `allowedAdFlags`: banner + interstitial = `3`; interstitial + rewarded = `6`; all four = `15`.

**Audience.** If the game is **not** in the Families category (or the human does not know): leave `audienceTagged` at `0` (Mixed). If the human says it is Families: `1`. (`NotChildren` is a third value in the CAS docs, for apps whose audience is 13+ only; the file value is not confirmed, so do not set it yourself.)

**If the file does not exist yet** (a clean install): create it. Use this template, take the `guid` for `m_Script` from the installed package, never from this guide (file `Library/PackageCache/com.cleversolutions.ads.unity@*/Runtime/Internal/CASInitSettings.cs.meta`, line `guid:`), and create a `.meta` file next to it with a new random 32-character hex `guid`:

```
%YAML 1.1
%TAG !u! tag:unity3d.com,2011:
--- !u!114 &11400000
MonoBehaviour:
  m_ObjectHideFlags: 0
  m_CorrespondingSourceObject: {fileID: 0}
  m_PrefabInstance: {fileID: 0}
  m_PrefabAsset: {fileID: 0}
  m_GameObject: {fileID: 0}
  m_Enabled: 1
  m_EditorHideFlags: 0
  m_Script: {fileID: 11500000, guid: <guid from the installed package>, type: 3}
  m_Name: CASSettingsAndroid
  m_EditorClassIdentifier: 
  testAdMode: 1
  managerIds:
  - <CAS ID>
  allowedAdFlags: <mask>
  audienceTagged: 0
  bannerSize: 0
  bannerRefresh: 30
  interstitialInterval: 30
  loadingMode: 2
  debugMode: 0
  trackLocationEnabled: 0
  interWhenNoRewardedAd: 1
```

For iOS use the file name `CASSettingsiOS.asset` and `m_Name: CASSettingsiOS`. In mode A edit the file while Unity is closed. In mode B use `set_field` + `SaveAssets`, or edit the file and run `Assets/Refresh` (see step 1); a **new** file also needs `Assets/Refresh` before Unity knows it.

---

## 4. Mediation solution and Android dependencies

CAS has no ad inventory of its own. The game needs a mediation solution (a set of ad network adapters). The CAS docs choose it with a button in the Settings window (`Assets > CleverAdsSolutions > Android Settings...` and `iOS Settings...`).

**The choice is fixed by the answers in step 0, do not ask the human:**
- **Optimal Ads Solutions** by default, for every game that is not in the Families category (including when the human does not know).
- **Families Ads Solutions** only if the human said the game is in the Families category.

The Settings window does this with CAS's own editor class `CAS.UEditor.DependencyManager`, which writes the dependency files (`Assets/CleverAdsSolutions/Editor/*Dependencies.xml`). The CAS source has no public headless entry point, but the same class can be called by **reflection** from a small Editor script. The CAS Publishing installer does exactly this in production for its own solutions (`PublishingAds`, `TenjinMMP`) and for `OptimalAds` and `FamiliesAds`, on 4.8.1-beta2.

**4a. Write a temporary Editor script** in the game (for example `Assets/Editor/CasSolutionSetup.cs`, with the folder `Assets/Editor` if it does not exist). It is yours: you delete it in 4d.

```csharp
using System;
using System.Reflection;
using UnityEditor;
using UnityEngine;

public static class CasSolutionSetup
{
    // Run from the command line:
    //   -executeMethod CasSolutionSetup.ApplyOptimal    (not Families)
    //   -executeMethod CasSolutionSetup.ApplyFamilies   (Families category)
    public static void ApplyOptimal() => Run("OptimalAds", 0);
    public static void ApplyFamilies() => Run("FamiliesAds", 1);

    private static void Run(string solutionName, int audience)
    {
        bool ok = false;
        try
        {
            var target = EditorUserBuildSettings.activeBuildTarget;
            var dm = FindType("DependencyManager");
            var aud = FindType("Audience");
            var create = dm.GetMethod("Create", BindingFlags.Public | BindingFlags.Static, null,
                new[] { typeof(BuildTarget), aud, typeof(bool) }, null);
            var manager = create.Invoke(null, new object[] { target, Enum.ToObject(aud, audience), false });
            var solutions = (Array)(dm.GetField("solutions")?.GetValue(manager)
                                    ?? dm.GetProperty("solutions")?.GetValue(manager));
            foreach (var s in solutions)
            {
                var name = (string)(s.GetType().GetField("name")?.GetValue(s)
                                    ?? s.GetType().GetProperty("name")?.GetValue(s));
                if (name != solutionName) continue;
                s.GetType().GetMethod("ActivateDependencies").Invoke(s, new object[] { target, manager });
                ok = true;
            }
            AssetDatabase.Refresh();
        }
        catch (Exception e) { Debug.LogError("[CasSolutionSetup] " + e); }
        Debug.Log("[CasSolutionSetup] " + (ok ? "applied" : "FAILED"));
        if (Application.isBatchMode) EditorApplication.Exit(ok ? 0 : 1);
    }

    private static Type FindType(string simpleName)
    {
        foreach (var asm in AppDomain.CurrentDomain.GetAssemblies())
        {
            Type[] types; try { types = asm.GetTypes(); } catch { continue; }
            foreach (var t in types)
                if (t.Name == simpleName && t.Namespace != null && t.Namespace.StartsWith("CAS")) return t;
        }
        return null;
    }
}
```

**4b. Run it once per platform** the game ships on, with the Editor closed (mode A):

```
<Unity> -batchmode -nographics -quit -projectPath "<project root>" -buildTarget <Android|iOS> -logFile - -executeMethod CasSolutionSetup.ApplyOptimal
```

(`ApplyFamilies` for a Families game.) The exit code is `0` on success. Solutions are independent: enabling one does not switch another off, so if the project already has another solution active and the human wants to replace it, **stop and ask**.

**4c. Check:**
1. The log line `[CasSolutionSetup] applied` and exit code `0`.
2. The dependency files exist in `Assets/CleverAdsSolutions/Editor/` (one `*Dependencies.xml` for the solution, per platform).
3. Android: the External Dependency Manager (EDM4U) resolves the libraries. If the Resolver menu (`Assets > External Dependency Manager > Android Resolver`) is missing, EDM4U is not installed: ask the human before installing it. The CAS Settings window offers it with an **Install** button (red warning "Mediation requires External Dependency Manager"), which downloads `https://github.com/googlesamples/unity-jar-resolver/raw/master/external-dependency-manager-latest.unitypackage` and imports it. The same can be done headless: download the file into its own empty folder outside the project, then run Unity once in batch mode with `-importPackage "<path to the file>"`. Some agent environments block this import as code from an external source; then stop and let the human press **Install** or import the package by hand (Assets > Import Package > Custom Package).
4. Android: after EDM4U is in, the resolver needs `Assets/Plugins/Android/mainTemplate.gradle` and `gradleTemplate.properties` (Player Settings > Android > Publishing Settings > **Custom Main Gradle Template** and **Custom Gradle Properties Template**; without the second one EDM4U refuses to resolve with "could not enable Jetifier"). Tick them in the Editor, or in batch mode use a temporary Editor script that calls CAS's internal `CASPreprocessGradle.TryEnableGradleTemplate("Assets/Plugins/Android/mainTemplate.gradle")` and the same with `gradleTemplate.properties` by reflection, then `CASEditorUtils.ConfigureProjectForTargetPlatform()` (public, it runs the resolver). The check: `mainTemplate.gradle` now has `implementation 'com.cleveradssolutions:...'` lines. Delete the temporary script afterwards.

**4d. Delete the temporary script** (`Assets/Editor/CasSolutionSetup.cs` and its `.meta`) and, if you created the `Assets/Editor` folder only for it, that folder too.

**If the script fails** (a CAS type or method is missing in the installed version, for example after a CAS update), do not retry in disguise. Fall back to the manual way: tell the human to open the Settings window for each platform (`Assets > CleverAdsSolutions > Android Settings...`, `iOS Settings...`), click **Optimal Ads Solutions** (or **Families Ads Solutions** for a Families app) and, on Android, click **Android Resolver**, and wait for "done". Then do 4c.

The menu item `Assets > CleverAdsSolutions > Configure project` (`CAS.UEditor.CASEditorUtils.ConfigureProjectForTargetPlatform`) applies the required project configuration; it can also run in batch mode. Check whether you still need it after 4b.

In mode B (Editor open) the Pipeline can run C# inside the Editor (`unity command eval` and `script run`), so the reflection code above can be run without closing Unity, instead of `-executeMethod` (put the script file in the project, `Assets/Refresh`, wait for `unity recompile`, then call `CasSolutionSetup.ApplyOptimal()` through `eval`). If it does not work, use mode A for this step.

Some networks need extra steps (for example Meta Audience Network): do them only if the human uses that network. Do not add networks the human did not ask for.

For a Families app also remove the advertising ID permission: Settings window > Android Settings > Other settings > **Advertiser ID permission** > **Removed**.

---

## 5. Initialization in code (only on an explicit B for question 7, or when step 5b needs it)

Skip this step when the answer to question 7 is A and step 5b does not apply: write no ad code, and tell the human in step 9 that initialization is theirs (page **Initialize CAS** at `https://docs.page/cleveradssolutions/docs/Unity/`).

Initialize the manager **once**, at app start, before any ad call, with an ads manager class written after the `CASAdsManager` reference in step 8. Write the class and set up its scene object here, in step 5; step 8 then only adds the calls. The settings asset already holds the CAS ID and formats.

Look first for an ad manager the game already has (search the game's C#, not `Packages/` or `Library/`, for `MobileAds.BuildManager`, `IMediationManager`, other ad SDKs like `MaxSdk`, `IronSource`, `LevelPlay`, `GoogleMobileAds`). If the game already has a CAS manager, leave it and do not add `CASAdsManager`. If it uses another ad SDK, step 1b already handled the decision; the replacement is step 5b.

If the project uses Assembly Definitions and the compiler says `error CS0103: The name 'MobileAds' does not exist in the current context`, add an Assembly Definition Reference to the assembly `CleverAdsSolutions` for the assembly that holds the code.

---

## 5b. Replace the old ad SDK (only if step 1b found one and the human agreed)

Mode A (Unity closed): do the removal and the code changes in one pass, then compile once and fix the errors in batches; every compile is a batch run of 1 to 5 minutes. Mode B (Editor open): work in small stages and run `unity recompile` after each of them; it takes seconds.

First, **before you delete anything**, collect what you will need later and keep it in your notes:
- the `guid` of every script and prefab of the old SDK (from its `.meta` files): you will need them to find "Missing script" references;
- the library names the old SDK pulled in for Android (from its `*Dependencies.xml`, for example `com.applovin:applovin-sdk`): you will need them to find leftover `.aar` and `.srcaar` files.

1. **Remove the SDK itself**, only the items you listed in step 1b and the human approved:
   - delete its entries from `Packages/manifest.json` (and its scoped registry if nothing else uses it);
   - delete its folders and files under `Assets/` together with their `.meta` files, and its `*Dependencies.xml`, and its native libraries in `Assets/Plugins/Android` and `Assets/Plugins/iOS`;
   - the legacy Unity Ads package `com.unity.ads` is an error for CAS: remove it too.
   Never delete anything that was not in the approved list and never delete outside `Assets/` and `Packages/manifest.json`. Keep EDM4U, Firebase, attribution, analytics and purchase SDKs.
2. **Remove the keys it left** in `Assets/Plugins/Android/AndroidManifest.xml` and `Info.plist` (for example the AdMob or AppLovin app ID) only if they are clearly that SDK's, and tell the human each one you removed. If unsure, leave it and report it.
3. **Move the game's ad calls to CAS.** Use the calls the game already has as the list of placements: every old interstitial, rewarded, banner and app open call becomes the matching CAS call, in the same place, with the same reward and callback logic. Read the CAS pages for exact calls: **Interstitial Ads**, **Rewarded Ads**, **Banner Ads**, **App Open Ads** (`https://docs.page/cleveradssolutions/docs/Unity/`).
   - If the game has its own ad class, keep it and change only what is inside; the rest of the game then does not change.
   - If the game calls the old SDK directly from many places, create one small ad class once (written after the `CASAdsManager` reference in step 8, in step 5) and point the calls at it.
   - Old SDK initialization and its ad unit IDs go away: CAS does not use them. Do not copy IDs into CAS.
4. **Compile** (step 7). Fix only errors from the removed SDK. If a feature of the old SDK has no CAS counterpart (for example custom banner placement, server-side reward callbacks, an unusual consent setup), **stop**, list these places, and ask the human what to do.
5. **Check that nothing is left:** search again for the markers from step 1b in the code, `Assets/` and `manifest.json`.
6. **Leftover Android libraries.** The External Dependency Manager downloaded the old SDK's libraries into `Assets/Plugins/Android` as `.aar` or `.srcaar` files. Deleting the SDK's entry from `manifest.json` does not remove them, and they duplicate the CAS libraries. Delete exactly the files whose names match the libraries you noted, then recalculate the dependencies: in the Editor it is `Assets > External Dependency Manager > Android Resolver > Resolve`; in batch mode call CAS's resolver by reflection, `CASEditorUtils.TryResolveAndroidDependencies(true)` (public static in `CAS.UEditor`; the CAS Publishing installer does this). Also remove the old SDK's lines from `mainTemplate.gradle`, `baseProjectTemplate.gradle` and `launcherTemplate.gradle` if they are clearly its own.
7. **"Missing script" in scenes and prefabs.** If a scene or prefab had components of the removed SDK, they now point to GUIDs that no longer exist. Search `Assets/**/*.unity` and `Assets/**/*.prefab` for the GUIDs you noted. List each file to the human. Do not edit the YAML of scenes by hand unless the human agrees; the safe way is a small Editor script run in batch mode that opens the scene or prefab and calls `GameObjectUtility.RemoveMonoBehavioursWithMissingScript` on its objects, then saves it.
8. Report the full list of removed packages, files and edited code places in step 9.

---

## 6. Consent and privacy

CAS shows its own consent flow where the law requires it (the initialization result has `isConsentRequired` and `consentFlowStatus`). Read the CAS pages **User Consent Flow** and **Privacy Regulations** at `https://docs.page/cleveradssolutions/docs/Unity/` and tell the human what they need to provide (privacy policy link, Info.plist text on iOS). Do not change the defaults without the human's decision. For a Families app set the audience as in step 3.

---

## 7. Test mode and build check

- Test ads: the settings asset has `testAdMode: 1`, set in step 3 if the app is not registered by CAS yet. A development build also gets test ads automatically.
- Remind the human: **until CAS registers the app, the game can only be built with Test ad mode on.** After registration: set `testAdMode: 0` (or ask the human to untick **Test ad mode** in the Settings window) and build again. Never ship a build with test ads.
- Check that the project compiles: mode A run Unity in batch mode and search the log for `error CS`; mode B `unity recompile`. Fix only what you introduced.
- If you used test devices in code (`MobileAds.settings.SetTestDeviceIds`), remove that line before release.
- **If you added the Unity CLI's `com.unity.pipeline` package in mode B, ask one question in the question format: "Remove the Pipeline package?"** Found: you added it in this run; it is an experimental Unity package and the project does not need it for CAS. Options: A. Yes, remove it; B. No, keep it. **Recommended:** Option A - the project does not need it. On A: `unity command package remove --identifier com.unity.pipeline --confirm true`, or delete its line from `Packages/manifest.json` with the Editor closed. Do not remove it if it was in the project before you started.

---

## 8. Placing ads in the game (only on an explicit yes from step 0)

Read the CAS pages **Banner Ads**, **Interstitial Ads**, **Rewarded Ads**, **App Open Ads** at `https://docs.page/cleveradssolutions/docs/Unity/`, then:

1. Map the game loop: level complete, game over, return to the menu, pause, continue prompts, shops, reward buttons.
2. **Propose before editing**: a short table (file, method, ad format, placement name) and get a yes.
   - **Interstitial**: after a natural break, never in the middle of play, never in a tutorial, never right after a rewarded ad.
   - **Rewarded**: only behind a button the player presses; give the reward only when the ad was completed; hide the button when no ad is ready.
   - **Banner**: on menu or idle screens; hide it when play starts.
   - **App Open**: at app start, before the first screen. **It is not loaded automatically** (unlike banner, interstitial and rewarded in the default `Auto` loading mode): call `manager.LoadAd(AdType.AppOpen)` yourself once the manager is ready, and call `ShowAd(AdType.AppOpen)` from the `OnAppOpenAdLoaded` callback. Waiting for `IsReadyAd(AdType.AppOpen)` without calling `LoadAd` never succeeds.
3. Edit minimally: one call per placement next to the existing logic; no refactoring.
4. Compile and check (step 7).

**Reference: `CASAdsManager`, one ads manager for the whole game.** This is a working script written by hand for CAS 4.8.0 on Unity 6000.3. It is a reference for the code you write, not a file to paste. Adapt it to the project: its naming and code style, the formats the human chose (question 6), the ad class and call sites the game already has, the scene setup and the installed Unity and CAS versions. Keep what the reference does right: one manager for the whole game, one initialization at start, App Open loaded by hand, every ad called through the manager, one banner on screen at a time. Do not add features the human did not ask for.

What it does: a singleton (`CASAdsManager.instance`) on one object of the first scene that survives scene loads (`DontDestroyOnLoad`). `InitCAS()` builds the manager (on `Start`, unless `_initOnStart` is switched off in the Inspector), and in the initialization callback it runs App Open: **App Open is loaded by hand** (`LoadAd(AdType.AppOpen)`) and shown from the `OnAppOpenAdLoaded` callback. The other formats are called by the game code through the manager:

```csharp
CASAdsManager.instance.ShowBanner(AdSize.Banner, AdPosition.BottomCenter);
CASAdsManager.instance.HideBanner();
CASAdsManager.instance.ShowInterstitial();
CASAdsManager.instance.ShowRewarded(() => GiveReward());
```

```csharp
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using CAS;
using UnityEngine;

public class CASAdsManager : MonoBehaviour
{
    public static CASAdsManager instance = null;

    private static IMediationManager _manager = null;
    private static IAdView _lastAdView = null;
    private static Action _lastAction = null;

    [SerializeField] bool _initOnStart = true;

    private void Awake()
    {
        if (instance == null)
            instance = this;

        DontDestroyOnLoad(this);
    }

    public void Start()
    {
        if(_initOnStart)
            InitCAS();
    }

    public void InitCAS()
    {
        _manager = MobileAds.BuildManager()

          .WithCompletionListener((config) => {
              string initErrorOrNull = config.error;
              string userCountryISO2OrNull = config.countryCode;
              bool protectionApplied = config.isConsentRequired;
              IMediationManager manager = config.manager;

              ShowAppOpen();
          })
          .Build();

        _manager.OnRewardedAdCompleted += _lastAction;

    }

    private void ShowAppOpen()
    {
        _manager.OnAppOpenAdLoaded += () => _manager.ShowAd(AdType.AppOpen);
        _manager.LoadAd(AdType.AppOpen);
    }

    public void ShowBanner(AdSize size, AdPosition position)
    {
        HideBanner();

        if (_manager != null)
        {
            _lastAdView = _manager.GetAdView(size);

            _lastAdView.position = position;
            _lastAdView.SetActive(true);
        }
    }

    public void HideBanner()
    {
        if (_lastAdView != null)
        {
            _lastAdView.SetActive(false);
            _lastAdView.Dispose();
            _lastAdView = null;
        }
    }

    public void ShowInterstitial()
    {
        _manager?.ShowAd(AdType.Interstitial);

    }

    public void ShowRewarded(Action complete)
    {
        if (_manager == null)
            return;

        if (_lastAction != null)
        {
            _manager.OnRewardedAdCompleted -= _lastAction;
        }

        _lastAction = complete;
        _manager.OnRewardedAdCompleted += _lastAction;
        _manager?.ShowAd(AdType.Rewarded);
    }
}

```

Notes for the reference:
- **Put the object into the first scene** after the script compiles: one object named `CASAdsManager` with this component, at the root of the scene (`DontDestroyOnLoad` works only on root objects). Editor closed: add three blocks to the scene YAML before the `SceneRoots` block (a `GameObject`, a `MonoBehaviour` whose `m_Script` is `{fileID: 11500000, guid: <guid from CASAdsManager.cs.meta>, type: 3}`, a `Transform` with `m_Father: {fileID: 0}`) with new unused fileIDs, and add the Transform's fileID to `m_Roots`; copy an existing object of the same scene as the format sample and keep a backup of the scene file. Fields missing in the YAML take the defaults from the script. Editor open: ask the human to drag the script onto an empty object (editing the scene file under an open Editor makes it ask to reload and can lose unsaved changes).
- It initializes CAS itself: one manager per game, so never add a second initializer next to it. Existing ad calls of the game (step 5b) point at `CASAdsManager.instance`.
- There is no guard against a second copy: keep the object only in the first scene, or a later scene with the same object creates a second manager.
- `HideBanner()` disposes the current banner view; `ShowBanner` hides the previous one first, so only one banner is on screen. `AdSize` is an enum (`Banner`, `AdaptiveBanner`, `Leaderboard`, `MediumRectangle`, ...); `AdPosition` has `BottomCenter` and the others. Both are in `CAS`.
- The reference does not check that an ad is ready: `ShowInterstitial` and `ShowRewarded` call `ShowAd` directly, and a failed show only fires the CAS failed-to-show event. If the human wants a ready check or a "hide the reward button" logic, say so and ask whether to add it. Do not add it on your own.
- Include the App Open part only when the human chose app open (question 6). App Open is requested once, at initialization. In the reference project the banner and the interstitial 10 seconds later are not part of the manager: place those calls where the human asked (step 8.2).
- Rewarded and the other formats work only if they are enabled in the settings asset (`allowedAdFlags`, step 3).
- The settings asset keeps `interstitialInterval: 30` (minimum seconds between interstitials). If an interstitial is requested sooner, it fails to show. Tell the human instead of changing the interval yourself.


---

## 9. Report back

Tell the human: whether another ad SDK was found and what you removed and rewrote (step 1b and 5b), the install method and version, the settings per platform (CAS ID, formats, audience, test mode), the files you added or changed, whether the mediation solution was chosen (and by whom), what is still manual (CAS registration of the app, turning Test ad mode off, store setup, and, when the answer to question 7 was A, the initialization and the ad calls in code, which you did not write), and any step you could not complete. Suggest `git diff` for review if the project is under git.
