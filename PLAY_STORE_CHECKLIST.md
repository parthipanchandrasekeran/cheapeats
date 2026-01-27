# Google Play Store Publishing Checklist

## Pre-requisites
- [ ] Google Play Developer Account ($25 one-time fee)
- [ ] Privacy Policy URL (required for apps that use location)

---

## Step 1: Create Release Keystore

Run this command in the project root to create your release keystore:

```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias cheapeats
```

You'll be prompted for:
- Keystore password (remember this!)
- Your name, organization, city, state, country
- Key password (can be same as keystore password)

**IMPORTANT:**
- Back up `release-keystore.jks` securely - you need it for ALL future updates
- Never commit it to git (already in .gitignore)

---

## Step 2: Configure Signing

1. Copy `keystore.properties.template` to `keystore.properties`
2. Fill in your values:
   ```properties
   storeFile=release-keystore.jks
   storePassword=your_store_password
   keyAlias=cheapeats
   keyPassword=your_key_password
   ```

---

## Step 3: Build Release APK/Bundle

```bash
# Build signed release bundle (recommended for Play Store)
./gradlew bundleRelease

# Or build signed APK
./gradlew assembleRelease
```

Output locations:
- Bundle: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`

---

## Step 4: Play Store Listing Content

### App Details
- **App name:** Cheap Eats
- **Short description (80 chars max):**
  Find affordable restaurants under $15 near TTC stations in Toronto

- **Full description (4000 chars max):**
  ```
  Discover budget-friendly dining options in Toronto with Cheap Eats!

  Features:
  • Find restaurants with meals under $15
  • Filter by "Open Now" to see what's available
  • Locate eateries near TTC subway stations
  • Get AI-powered restaurant recommendations
  • View ratings, prices, and walking distances
  • Student discount filter for extra savings

  Perfect for students, budget-conscious foodies, and anyone looking
  for great food without breaking the bank. Uses your location to find
  nearby restaurants and shows real-time opening hours.

  Built for Toronto - optimized for TTC transit users!
  ```

### Category
- **Category:** Food & Drink
- **Tags:** restaurants, food, budget, cheap eats, Toronto, TTC

### Contact Details
- Developer email (required)
- Website (optional)
- Privacy policy URL (required)

---

## Step 5: Graphics Assets Required

### App Icon
- 512x512 PNG (already have ic_launcher)

### Feature Graphic
- 1024x500 PNG/JPG
- Displayed at top of store listing

### Screenshots (required)
- Phone: 2-8 screenshots
- Recommended: 1080x1920 or 1920x1080
- Capture key features:
  1. Home screen with restaurant list
  2. Map view with markers
  3. Filter chips in action
  4. Restaurant details
  5. AI recommendations

### Promotional Video (optional)
- YouTube URL
- 30 seconds to 2 minutes

---

## Step 6: Privacy Policy

Since the app uses location, you MUST have a privacy policy. Here's a template:

**Privacy Policy for Cheap Eats**

```
Last updated: [DATE]

Cheap Eats ("we", "our", or "us") respects your privacy. This policy
explains how we handle your information.

INFORMATION WE COLLECT
• Location data: Used only to find nearby restaurants. Not stored on
  our servers.

THIRD-PARTY SERVICES
• Google Maps API: Subject to Google's privacy policy
• Google Places API: For restaurant data

DATA STORAGE
We do not collect, store, or share your personal data. All location
processing happens on your device.

CONTACT
[Your email]
```

Host this on a website (GitHub Pages works) and provide the URL.

---

## Step 7: Content Rating

Complete the content rating questionnaire in Play Console:
- No violence
- No sexual content
- No controlled substances
- No user-generated content

Expected rating: **Everyone (E)**

---

## Step 8: App Access

If your app requires:
- API keys: These are embedded in the app
- No login required: Select "All functionality available"

---

## Step 9: Ads Declaration

- [ ] Contains no ads (check this if true)

---

## Step 10: Data Safety Form

Fill out the data safety section:

**Location**
- Collected: Yes
- Shared: No
- Required: Yes (for core functionality)
- Purpose: App functionality

**No other data collected**

---

## Version History

| Version | Code | Changes |
|---------|------|---------|
| 1.0.0   | 1    | Initial release |

---

## Final Checklist

- [ ] Keystore created and backed up
- [ ] keystore.properties configured
- [ ] Release build successful
- [ ] App tested on multiple devices
- [ ] Privacy policy hosted online
- [ ] All graphics prepared
- [ ] Store listing text ready
- [ ] Content rating completed
- [ ] Data safety form filled
- [ ] Internal testing track upload
- [ ] Production release submitted

---

## Useful Commands

```bash
# Check signing info of APK
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk

# List keystore contents
keytool -list -v -keystore release-keystore.jks
```
