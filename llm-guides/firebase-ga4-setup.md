# CAS Publishing: Firebase, GA4 and BigQuery setup guide for AI agents

You are an AI coding agent working for a game studio that is integrating with CAS Publishing. This guide sets up the studio's own Firebase project so that CAS can check it and receive analytics. Do exactly the steps below, in order. **Stop and ask the human whenever this guide says so.**

You work **on the human's machine, under the human's own Google account**. Rules for the whole guide:
- Never ask for, print, store or send passwords, tokens or keys. Never create service account keys.
- Show the human the full list of changes (who, which role, where) **before** applying them and wait for a clear yes.
- Grant access only to the accounts in the table in step 0. Never grant `roles/owner`. Never remove existing members.
- The temporary service account from step 3 **must be deleted** in step 11. If you cannot finish, say so first in your report.
- **Your own permission system may block commands that grant roles** (steps 3, 5 and 11), even after the human said yes in chat. Warn the human about this before step 3. If a command is denied, do not retry it in disguise and do not use another tool to get around it. Tell the human what was blocked and why you need it, and let them choose: switch the agent from auto mode to manual mode and confirm each command in its prompt (the first choice), confirm again explicitly in chat (naming the project and the roles), add a permission rule for these `gcloud` IAM commands in the agent settings, run the commands themselves in their own terminal, or use the manual alternative at the end of this guide.

---

## 0. Before you start

1. **Research first, then ask in the question format below.** This guide runs on its own: it researches the machine and the project itself and asks for everything it needs. Nothing is handed over from another guide.

   **Question format** (the same in every CAS guide). One question = a bold line, `Found:` (evidence, or "nothing found"), a two-column table of 2-4 lettered options (no Recommended column), then a line `**Recommended:** Option A - reason`. Do not ask what the research found: list it. Separate the parts of a question with empty lines and the questions from each other with a `---` line, so a round is easy to read. Questions go in rounds of at most 3, numbered through all rounds ("Round 1 of 2"); send one round, wait for the reply, then the next. End each round with "Reply `ok` to accept every Recommended answer of this round, or list the changes, for example `2B`". `ok` accepts every Recommended answer of the round; a question with `Recommended: none` (a value only the human has) stays open: ask only that question again, in a short follow-up, before the next round. After the last round say in one line what was chosen and start at once. A question raised later by a result uses the same format.

   **Research (read-only):**
   - **This guide needs no Unity project and no project files.** It also works for a manager who has access to the Firebase, Google Analytics and Google Cloud accounts but is not the developer and has no game folder. If the current folder is not a Unity project, or has no config file, just skip the file lookups below; nothing else changes.
   - the Firebase project id, if a game folder happens to be at hand: `project_info.project_id` in `google-services.json`, or `PROJECT_ID` in `GoogleService-Info.plist`, in the current folder or the Unity project (skip `Library/`, `Temp/`, `Packages/`; two different ids: list both and ask which). No file: the id is found after the sign-in of step 1b (see F1);
   - `gcloud --version` (or the full path from 1a) to see if the CLI is installed.

   **Questions** (ask only those that apply; the last column is a rule for you, shown to the human as the `**Recommended:**` line under the options):

   | # | Question | Answers | Recommended is |
   |---|---|---|---|
   | F1 | Which Firebase project? | A file was found: A. the id found; B. another id (the human types it). No file: **ask F1 after the sign-in of step 1b**, with the projects the human's account can see (`gcloud projects list --format="value(projectId,name)"`) as options A to C, up to three, and "Other" for an id that is not listed | The id from the config file. No file and exactly one project in the list: that project. Otherwise `none` (the human knows which project is the game's). |
   | F2 | Is the Android app already published on Google Play? (decides whether step 9 applies) | A. Not yet: skip the Google Play link, add it after the release; B. Yes: walk me through the link | A. |
   | F3 | `gcloud` is not installed. Install it? | A. Yes, install it with the platform's package manager (Windows `winget`, macOS `brew`); B. No, I install it myself; C. Stop | A. |

   If F1 cannot be answered before the sign-in (no file), leave it out of the first round and ask it as soon as the sign-in of step 1b is done, in the same format. The ID you got from F1 is the only project you touch.
2. **The human must be Owner** of the Firebase / Google Cloud project and **Administrator** of its Google Analytics property. If they are not, stop and tell them which role is missing.
3. **Run the preflight in step 1 before anything else.** Nothing below may start until the CLI is installed and signed in and the Blaze plan is on.
4. **Accounts that get access.** Exactly this list:

| Account | Firebase / Cloud role | GA4 role |
|---|---|---|
| `publishing_admin@cas.ai` | Editor (`roles/editor`) | Editor |
| `Serhii.Liakh@cas.ai` | Editor (`roles/editor`) | Editor |
| `cas_firebase_user@cas.ai` | Editor (`roles/editor`) | Administrator |
| `core-automation@data-warehouse-2025-0424.iam.gserviceaccount.com` | BigQuery Data Editor (`roles/bigquery.dataEditor`) | - |
| `viktor-trukhnov@data-warehouse-2025-0424.iam.gserviceaccount.com` | BigQuery Data Viewer (`roles/bigquery.dataViewer`) | - |

`cas_firebase_user@cas.ai` runs CAS's automatic integration checks. It needs Administrator in GA4 because Google Analytics shows the access list through its API to administrators only.

Other values: the BigQuery dataset region **`europe-central2`**, the GA4 reporting time zone **`Etc/GMT-3`** (GMT+03:00).

---

## 1. Preflight: tools, sign-in and Blaze (before any other step)

Check each item. If it fails, fix it with the human as described, then check again. Do not continue until all three pass.

**1a. The Google Cloud CLI is installed.** This guide uses only `gcloud` and plain HTTPS calls; the Firebase CLI is not needed.

```
gcloud --version
```

If it is missing, the human already answered F3 in step 0 (if you skipped it, ask F3 now, in the question format). Examples: Windows `winget install --id Google.CloudSDK`, macOS `brew install --cask google-cloud-sdk`, Linux Google's installer for the Cloud SDK. After installing, the PATH changes only in a new terminal: ask the human to restart the agent or the terminal if the command is still not found.

On Windows the SDK may be installed but missing from the PATH of the agent session. Check `%LOCALAPPDATA%\Google\Cloud SDK\google-cloud-sdk\bin\gcloud.cmd`; if the file exists, call `gcloud` by that full path in every command instead of asking for a restart or a reinstall. The examples below write plain `gcloud`.

**1b. The human is signed in on this machine.**

```
gcloud auth list
```

If no account is active, the human signs in. The command opens a browser and waits for the human, so do not run it inside the agent's own shell. **Open a separate terminal window for the human that already runs it**, then tell the human to sign in in the browser and reply "done". Do not ask the human to type the command or find the path. Use the full `gcloud` path from 1a (the agent session may not have `gcloud` on its PATH).

On Windows, with `$g` set to the full path of `gcloud.cmd`:

```
Start-Process powershell -ArgumentList '-NoExit','-Command',"& '$g' auth login"
```

macOS / Linux: open a terminal window running `gcloud auth login` (for example `osascript -e 'tell application "Terminal" to do script "gcloud auth login"'` on macOS), or, if no window can be opened, tell the human to run `gcloud auth login` in their own terminal.

The human picks the Google account that is Owner of the project. Check again with `gcloud auth list`. If F1 was not answered yet (no config file, for example a manager running this guide), ask it now with the list from `gcloud projects list`. Then set the project: `gcloud config set project <project-id>`. If the project ID is wrong or missing in `gcloud projects list`, stop.

Do **not** try `gcloud auth application-default login` with Analytics scopes: Google blocks those scopes for the default `gcloud` client. Step 3 solves this.

**1c. The Blaze plan is on.** BigQuery export needs it.

```
gcloud billing projects describe <project-id>
```

If the call says the Cloud Billing API is disabled, ask the human and run `gcloud services enable cloudbilling.googleapis.com --project <project-id>`, then repeat. `billingEnabled: true` means Blaze: go on. If it is `false`, upgrading needs the studio's billing account and payment method, so you cannot do it yourself. Ask one question in the question format: **"The project is on the Spark plan. Upgrade to Blaze now?"** with the options A. Yes, show me the whole path; B. Stop, I will do it myself and run the guide again. Then the line `**Recommended:** Option A - BigQuery export needs Blaze, and the free tier keeps the cost at zero until the limits are exceeded`. On A, **show the human the whole path at once**, as one numbered list in a single message (not one button per message), then ask for "done" once at the end:

1. Open the project in the Firebase console: `https://console.firebase.google.com/project/<project-id>/overview`.
2. Click the gear icon next to **Project Overview**, then **Usage and billing**.
3. Open **Details & settings** and click **Modify plan**.
4. Choose **Blaze**. Pick or create the studio's billing account, enter the payment details and confirm.
5. Come back and reply "done".

After "done" you repeat the `gcloud billing projects describe` check until `billingEnabled: true` (the human does not do that). If the human gets stuck on a screen, ask which step and what they see, and help with that step only. The human enters the payment details; you never do. Never pick a billing account for the human.

---

## 2. Project and GA4 property

This guide does not register apps and does not download config files. It only sets up access and integrations of an existing project.

- The project must already exist. Its ID is the answer to F1; check that it is in `gcloud projects list`, do not ask again. If there is no project yet, stop and tell the human to create it in the Firebase console with **Google Analytics enabled**; creating projects is not part of this guide.
- Send every call below with `Authorization: Bearer $(gcloud auth print-access-token)` and `x-goog-user-project: <project-id>`. Never print the token.
- Find the GA4 property of the project and keep its numeric ID as `<property-id>`:

```
GET https://firebase.googleapis.com/v1beta1/projects/<project-id>/analyticsDetails
```

Read `analyticsProperty.id`. If the call returns nothing, Google Analytics is not enabled for the project: stop and tell the human.

**Windows PowerShell tips**:
- Put the `gcloud` path in a variable and call it with `&`.
- Put every value in its own variable and quote option arguments: `"--member=$m" "--role=$role"`. Writing `--member=$row[0]` is not expanded as an array element; `gcloud` then gets garbage and answers `The role name must be in the form "roles/{role}"...`.
- For REST calls use `Invoke-RestMethod`, with the token in a variable that is never echoed. Send JSON as a single-quoted string with `-ContentType "application/json"`.
- On an HTTP error PowerShell hides the response body. Read it from `$_.Exception.Response.GetResponseStream()` to see the real reason.

---

## 3. Temporary setup service account (under the human's login)

Why: the Analytics Admin API needs a token with Analytics scopes, which a normal `gcloud` login cannot get. A temporary service account in the **studio's own project** can. It is deleted in step 11.

Show the human what you are about to create and wait for a yes, then run:

```
gcloud services enable iamcredentials.googleapis.com analyticsadmin.googleapis.com cloudbilling.googleapis.com iam.googleapis.com --project <project-id>
gcloud iam service-accounts create cas-setup --display-name="CAS setup (temporary)" --project <project-id>
```

Give it only these project roles (not Owner):

```
gcloud projects add-iam-policy-binding <project-id> --member="serviceAccount:cas-setup@<project-id>.iam.gserviceaccount.com" --role="roles/resourcemanager.projectIamAdmin" --condition=None
gcloud projects add-iam-policy-binding <project-id> --member="serviceAccount:cas-setup@<project-id>.iam.gserviceaccount.com" --role="roles/firebase.admin" --condition=None
gcloud projects add-iam-policy-binding <project-id> --member="serviceAccount:cas-setup@<project-id>.iam.gserviceaccount.com" --role="roles/bigquery.admin" --condition=None
gcloud projects add-iam-policy-binding <project-id> --member="serviceAccount:cas-setup@<project-id>.iam.gserviceaccount.com" --role="roles/serviceusage.serviceUsageAdmin" --condition=None
```

Let the human act as the service account (no key needed):

```
gcloud iam service-accounts add-iam-policy-binding cas-setup@<project-id>.iam.gserviceaccount.com --member="user:<human-email>" --role="roles/iam.serviceAccountTokenCreator" --project <project-id>
```

A new service account needs a few seconds to propagate. If the first role binding fails with `Service account ... does not exist`, wait 10 to 15 seconds and repeat that one binding. Afterwards list the roles and check that all four are there:

```
gcloud projects get-iam-policy <project-id> --flatten="bindings[].members" --filter="bindings.members:cas-setup@" --format="table(bindings.role)"
```

If creating the account or granting a role is refused (for example by an organization policy), **stop**, do not look for workarounds, and use the manual alternative at the end of this guide. If the refusal comes from your own permission system, see the rule in step 0.

---

## 4. The human adds the service account to Google Analytics (manual, one action)

Give the human a link that already points at this project's property, built from the IDs, then the clicks. The account ID is `analyticsProperty.analyticsAccountId` in the same `analyticsDetails` answer as step 2 (if the field is not there, leave the account out and use the clicks):

```
https://analytics.google.com/analytics/web/#/a<account-id>p<property-id>/admin
```

It should open the **Admin** page of exactly that property. A link straight to the access-management section is not reliable: Google redirected it to the main page, so link to **Admin** only. If the link lands on the main page or on another property, the human follows the clicks below and checks the property name `<project-id>` (ID `<property-id>`) at the top left.

1. Open the link above (or https://analytics.google.com and select the account and the property).
2. If you are not on the Admin page, click **Admin** (the gear icon, bottom left).
3. In the **Property** column choose **Property access management**.
4. Click the blue **+** button, then **Add users**.
5. Email: `cas-setup@<project-id>.iam.gserviceaccount.com`. Role: **Administrator**. Untick "Notify new users by email". Click **Add**.

Wait for the human to confirm.

Then check it before step 5: generate the Analytics token (see step 5) and call `GET https://analyticsadmin.googleapis.com/v1alpha/properties/<property-id>/accessBindings`. It must return the list, with `cas-setup@...` as `predefinedRoles/admin`. If it returns 403 `The caller does not have permission`, either Google has not propagated the grant yet (retry once after about 20 seconds) or it was added wrongly. Ask the human to check four things: the email is exact, the role is **Administrator** and not Editor, it was added in **Property** access management and not **Account** access management, and the right property is selected.

---

## 5. Access

> **Before this step, tell the human to switch the agent from auto mode to manual mode** (the mode that asks before each action). In auto mode the agent's classifier refuses `add-iam-policy-binding` for another person's account with the reason "Permission Grant", before Google is even called. In manual mode the same command goes through after the human confirms it in the agent's own prompt. Say it in one line, wait for "done", then go on. If the agent is already in manual mode, skip this note.

**Show the full change list from step 0 and wait for a yes first.**

Firebase and Google Cloud IAM, acting as the service account (the `--impersonate-service-account` flag):

```
gcloud projects add-iam-policy-binding <project-id> --member="user:publishing_admin@cas.ai" --role="roles/editor" --condition=None --quiet --impersonate-service-account=cas-setup@<project-id>.iam.gserviceaccount.com
```

Repeat for every row of the table in step 0 (`user:` for people, `serviceAccount:` for service accounts, and the role from the table).

Analytics token with the Analytics scopes (put the JSON in a file; shells often strip the quotes):

```
{"scope":["https://www.googleapis.com/auth/analytics.edit","https://www.googleapis.com/auth/analytics.manage.users","https://www.googleapis.com/auth/cloud-platform"]}
```

```
POST https://iamcredentials.googleapis.com/v1/projects/-/serviceAccounts/cas-setup@<project-id>.iam.gserviceaccount.com:generateAccessToken
```

Send it with the human's `gcloud auth print-access-token` and the header `x-goog-user-project: <project-id>`; the answer holds `accessToken`. Use that token only for the calls below and never print it.

GA4 access (one request per account; `predefinedRoles/editor` for `publishing_admin@cas.ai` and `Serhii.Liakh@cas.ai`, `predefinedRoles/admin` for `cas_firebase_user@cas.ai`):

```
POST https://analyticsadmin.googleapis.com/v1alpha/properties/<property-id>/accessBindings
{ "user": "publishing_admin@cas.ai", "roles": ["predefinedRoles/editor"] }
```

Google Analytics stores emails in lower case: send `serhii.liakh@cas.ai`, not `Serhii.Liakh@cas.ai`.

If a request fails with HTTP 400 `FAILED_PRECONDITION` and `accounts-per-user-limit-exceeded`, that user is already in the maximum number of Analytics accounts and Google will not add them. Do not work around it. Carry on with the other accounts and with steps 7 and 8, do not keep the service account for this (finish step 11), and put it in the report right after the first line: the account must first be removed from Analytics accounts it does not need (this is done on the CAS side), then the human adds it by hand: Google Analytics > **Admin** > **Property access management** > **+** > **Add users**, with the missing role.

**When the access is granted** (every account of the step 0 table is in Cloud IAM and GA4, or the failure of one is reported), check the result with the read-only calls and then tell the human in one line: **"Access is done. You can switch the agent back from manual to auto mode now."** Steps 6 to 10 only read data or change the BigQuery link and the time zone, which the classifier does not block. If step 11 (removing the service account's roles and deleting it) is refused for the same reason, ask the human to switch to manual again for that step only, and to go back to auto when step 11 is verified.

---

## 6. Blaze plan (checked in step 1c)

Already confirmed in the preflight. Only repeat the check (`gcloud billing projects describe <project-id>`) if the human changed the billing during the session.

---

## 7. BigQuery export

A property can have only one BigQuery link. Read first, with the Analytics token from step 5:

```
GET https://analyticsadmin.googleapis.com/v1alpha/properties/<property-id>/bigQueryLinks
```

**The link must have apps selected.** A link made without `exportStreams` showed **0 of 1 apps** in the Firebase console (Project settings > Integrations > BigQuery): it exists, but exports nothing. So before creating or updating the link, list the app data streams of the property:

```
GET https://analyticsadmin.googleapis.com/v1beta/properties/<property-id>/dataStreams
```

Keep the streams whose `type` is `ANDROID_APP_DATA_STREAM` or `IOS_APP_DATA_STREAM` (their `name` is `properties/<property-id>/dataStreams/<stream-id>`), and put all of those names in `exportStreams` below (field name `exportStreams` of the v1alpha BigQuery link).

- **No app streams at all:** the project has no registered app, so there is nothing to export yet. This guide does not register apps. Still create or update the link (so the region and the options are right), and write in the report: "BigQuery is linked, but 0 apps are selected because no app is registered in the project; after the studio registers its app (Firebase console > Project settings > Your apps) check Integrations > BigQuery > Manage that the app is ticked".

- **No link:** create it.

```
POST https://analyticsadmin.googleapis.com/v1alpha/properties/<property-id>/bigQueryLinks
{
  "project": "projects/<project-id>",
  "datasetLocation": "europe-central2",
  "dailyExportEnabled": false,
  "streamingExportEnabled": true,
  "includeAdvertisingId": true,
  "exportStreams": [ "properties/<property-id>/dataStreams/<stream-id>" ]
}
```

Daily export is **not needed** and must stay off: only Streaming and Include advertising identifiers are enabled.

- **A link exists and `datasetLocation` is `europe-central2`:** update it. A second `POST` fails with HTTP 429.

```
PATCH https://analyticsadmin.googleapis.com/v1alpha/<link name>?updateMask=dailyExportEnabled,streamingExportEnabled,includeAdvertisingId,exportStreams
{ "dailyExportEnabled": false, "streamingExportEnabled": true, "includeAdvertisingId": true, "exportStreams": [ "properties/<property-id>/dataStreams/<stream-id>" ] }
```

  Add every app stream from the list above to `exportStreams`; the PATCH replaces the whole list, so do not drop streams that were already there. If the call refuses it, ask the human to turn Daily off by hand: Firebase console > Project settings > **Integrations** > **BigQuery** > **Manage**.

- **A link exists in another region:** **stop** and ask the human. The region cannot be changed; a wrong region means the analytics timestamps have to be corrected by hand.

---

## 8. Reporting time zone

```
PATCH https://analyticsadmin.googleapis.com/v1beta/properties/<property-id>?updateMask=timeZone
{ "timeZone": "Etc/GMT-3" }
```

---

## 9. Google Play link (Android only, published apps only)

No API; the human does it. Tell them: Firebase console > Project settings > **Integrations** > **Google Play** > **Link**, signed in with the email that belongs to the app's Google Play Console account. Skip it for iOS or an app that is not on Google Play yet.

---

## 10. Verify (read-only, before cleanup)

| Check | How | Expected |
|---|---|---|
| Firebase / Cloud roles | `gcloud projects get-iam-policy <project-id> --format=json` | the five rows of step 0 with the right roles |
| GA4 access | `GET .../v1alpha/properties/<property-id>/accessBindings` | the three people with Editor, Editor, Administrator (compare emails ignoring case) |
| Blaze | `gcloud billing projects describe <project-id>` | `billingEnabled: true` |
| BigQuery link | `GET .../v1alpha/properties/<property-id>/bigQueryLinks` | region `europe-central2`, `streamingExportEnabled` and `includeAdvertisingId` true, `dailyExportEnabled` false, and `exportStreams` holds **every** app stream of the property (the console must not say "0 of N apps"); no app streams in the property = report it, not an error |
| Time zone | `GET .../v1beta/properties/<property-id>` | `timeZone` is `Etc/GMT-3` |
| Google Play link | ask the human | linked (Android, published) or not applicable |

---

## 11. Clean up (mandatory)

In this order, then verify:

1. Remove the service account's own GA4 access: list `accessBindings`, find the one whose `user` is `cas-setup@<project-id>.iam.gserviceaccount.com` (ignoring case) and `DELETE https://analyticsadmin.googleapis.com/v1alpha/<binding name>`.
2. Remove its four project roles (`gcloud projects remove-iam-policy-binding ... --condition=None`).
3. Delete it: `gcloud iam service-accounts delete cas-setup@<project-id>.iam.gserviceaccount.com --project <project-id>`.
4. Check: `gcloud iam service-accounts list --project <project-id>` shows no `cas-setup`, and the project IAM policy contains no `cas-setup`.
5. Ask the human to open **Admin** > **Property access management** in Google Analytics and confirm `cas-setup` is gone.

---

## 12. Report back

Tell the human, in this order: whether `cas-setup` is deleted (yes or no, first line), what you did (access granted, integrations set up), the result of each check in step 10, anything that could not be done (with the error text), what is still manual (Blaze if needed, Google Play link), and the values to send to CAS: the Firebase project ID and the GA4 property ID. Suggest that the human reviews the change list once more.

---

## Manual alternative (when step 3 is not possible)

Do these in the consoles and let the agent only verify (step 10, with the human's own read-only `gcloud` commands):

1. **Firebase / Cloud access:** Firebase console > Project settings > **Users and permissions** > **Add member** for the three CAS people (Editor); Google Cloud console > **IAM & Admin** > **IAM** > **Grant access** for the two service accounts (BigQuery Data Editor, BigQuery Data Viewer).
2. **GA4 access:** Google Analytics > **Admin** > **Property access management** > **+** > **Add users** for the three people (Editor, Editor, Administrator).
3. **BigQuery:** Firebase console > Project settings > **Integrations** > **BigQuery** > **Link**; region `europe-central2`; tick **Streaming** and **Include advertising identifiers in export**, leave **Daily** off.
4. **Time zone:** Google Analytics > **Admin** > **Property settings** > **Reporting time zone**: the option with offset GMT+03:00 that has no daylight saving, then **Save**.
