# Unreal: consent and privacy

Canonical, in this repository:
[docs/Unreal/User-Consent-Flow.mdx](../../../../../docs/Unreal/User-Consent-Flow.mdx) and
[Privacy-Regulations.mdx](../../../../../docs/Unreal/Privacy-Regulations.mdx)
Published: `https://docs.page/cleveradssolutions/docs/Unreal/User-Consent-Flow`

Two mutually exclusive paths. Pick one in the interview and never mix them.

## Path A, the built-in flow

`Auto Consent Flow` in `CAS.AI Config - [Platform]`, **on by default**. The form appears
when the app initializes ads, and nothing else has to be written. The publisher also fills
in the **Privacy Policy URL** there; without it the dialog links the CAS privacy policy
instead of the game's.

Two events, and the second is usually enough on its own:

```cpp
UCASMobileAds::OnConsentFlowDismissed.AddLambda([=](){ /* form closed */ });

UCASMobileAds::OnAdsInitialized.AddLambda([=](const FCASInitialConfig& Config){
    // fires only after the consent dialog is dismissed
    ECASConsentFlowStatus Status = Config.ConsentFlowStatus;
});
```

Because initialization completes only after the dialog closes, `OnAdsInitialized` is the
single correct place to start attribution and analytics SDKs.

### Showing the form yourself

`ShowAdConsentFlowIfRequired()` loads the form and presents it only when consent is
required, and `OnConsentFlowResult` is invoked afterwards — **or immediately when consent is
not required**. So it is safe to call without checking first, but a UI that blocks until it
returns will flash.

```cpp
UCASMobileAds::OnConsentFlowResult.AddUObject(this, &UHandlerClass::HandleResult);
UCASMobileAds::ShowAdConsentFlowIfRequired();
```

The status values are `Undefined` (unused), `Obtained`, `NotRequired`, `Unavailable`,
`InternalError`, `NetworkError`, `NotForegroundApp` and `FlowStillPresenting`. The first
three are dismissals; the rest are failures. In Blueprint the same thing arrives as the
**OnCompleted** and **OnFailed** delegates.

### Privacy options entry

Some forms require the user to be able to change their decision later.
`IsUserAdConsentRequired()` says whether to show that entry in the game's settings; wire it
to `ShowAdConsentFlow()`, which presents the form regardless of whether consent is required.
Keep it reachable — this is a compliance requirement, not a nicety.

## Path B, the publisher's own CMP or consent UI

Uncheck `Auto Consent Flow` on both platforms, and apply the decision **before**
`InitializeMobileAds()`, so the ad networks start with the user's actual choice:

```cpp
UCASMobileAds::SetUserConsentForAds(ECASUserConsentStatus::Accepted);  // or ::Denied
UCASMobileAds::SetUserOptOutSaleForAds(ECASUserCCPAStatus::OptOutSale); // or ::OptInSale
```

The first is GDPR and the other opt-in regions; the second is the US multi-state consumer
privacy laws. Applying either **after** initialization means the networks were already
initialized under the wrong assumption, and the first session is wrong in a way nothing
reports.

If Google demand is used, the Google CMP requirements apply and have to be reviewed before
the integration, not after.

## Debug geography is a settings-window property

`User Debug Geography` in `CAS.AI Config - [Platform]`, not a code call. It works **only**
when test ads mode is on and an active test device is defined — so it cannot leak into a
release session the way it can on the other CAS plugins, but it also will not do anything
in a build the agent forgot to put in test mode. If the EEA form does not appear, check
those two switches before anything else.

## Audience is a settings-window field

`Target app audience`: **Mixed**, **Children** or **NotChildren**, per platform. It has to
match what the publisher declared in the CAS dashboard and in both stores. `Children` brings
the Google Play Families Policy on Android — the Families solution is built for it — and on
iOS the Families solution is **not** intended for the Apple Kids Category.

A game that targets children and adults needs a **neutral age screen**, an age gate that does
not nudge the player to misrepresent their age. That is a product decision: the agent asks,
links [Privacy-Regulations.mdx](../../../../../docs/Unreal/Privacy-Regulations.mdx) and
[Google-Plays-Families-Policy.mdx](../../../../../docs/Unreal/Google-Plays-Families-Policy.mdx),
and does not decide.

## App Tracking Transparency, on the iOS side

The usage description is the **User Tracking Usage Description** field of the iOS config
panel, and it is only meaningful when `Use Advertising Id` is on. Without the text the IDFA
is zeroed out and revenue drops, with nothing in the log to say why.

Never add the request on the agent's own initiative, and never word the usage description for
the publisher beyond offering the documented examples. Consent under GDPR and the ATT prompt
are two different decisions, and neither replaces the other.

On Android the same `Use Advertising Id` checkbox must be **off** for a children-only game,
and for a build for devices without Google Play Services.

## The store side has no Unreal copy

Both stores ask the publisher questions that an integration changes the answers to, and those
pages are the general ones:
[Google-Play-data-disclosure.mdx](../../../../../docs/general/Google-Play-data-disclosure.mdx)
and
[AppStore-data-disclosure.mdx](../../../../../docs/general/AppStore-data-disclosure.mdx), plus
[Apple-Ad-Attribution.mdx](../../../../../docs/general/Apple-Ad-Attribution.mdx) and
[Apple-Kids-Category.mdx](../../../../../docs/general/Apple-Kids-Category.mdx). Name them in
the hand-over; the publisher answers them.

## Rules that hold on both paths

- Third-party SDKs — attribution, analytics — start **from inside** `OnAdsInitialized`, after
  consent. And remember that event can fire again after an error, so guard against starting
  them twice.
- Never cache the consent status or a saved consent string in the game. An expired consent
  then produces a TCF 3.3 error, and the SDK is the one that knows when to ask again.
- Meta Audience Network's CCPA "Limited Data Use" flag is the publisher's own obligation, not
  something CAS handles. The page links the extra steps.
- The agent does not decide whether the audience is child-directed. It asks, and it stops when
  the answer is the Apple Kids Category.
