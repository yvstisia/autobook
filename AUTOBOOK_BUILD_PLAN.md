# AutoBook — Build Plan for Claude Code
> Read `CLAUDE.md` first before executing any step in this file.
> Execute one step at a time. Confirm each step compiles and runs before moving to the next.

---

## How to Use This File

Paste the prompt from each step directly into Claude Code.
Each step is self-contained and builds on the previous one.
Do not skip steps — each one is a dependency for the next.

---

## STEP 1 — Project Dependencies

**Prompt to give Claude Code:**
```
Add all required dependencies to build.gradle.kts (app level) for this project.
We need: Jetpack Compose (BOM, Material3, UI tooling), Room (runtime, KTX, compiler),
Lifecycle (ViewModel Compose, runtime KTX), Navigation Compose, Kotlin Coroutines Android,
and Google Play Services Location (for GPS tagging).
Also add the Room KSP plugin. Use the latest stable versions.
After updating gradle, sync the project.
```

**What to verify:**
- Gradle sync completes with no errors
- No red underlines in build.gradle.kts

---

## STEP 2 — Room Database Layer

**Prompt to give Claude Code:**
```
Create the full Room database layer for AutoBook based on the schema in CLAUDE.md.

Create these files:
1. data/local/entity/Vehicle.kt — @Entity data class
2. data/local/entity/ServiceRecord.kt — @Entity data class
3. data/local/entity/ServiceReminder.kt — @Entity data class
4. data/local/entity/FuelLog.kt — @Entity data class
5. data/local/entity/Workshop.kt — @Entity data class
6. data/local/dao/VehicleDao.kt — @Dao interface with: insertVehicle, updateVehicle, deleteVehicle, getAllVehicles (Flow), getVehicleById
7. data/local/dao/ServiceRecordDao.kt — @Dao with: insertServiceRecord, deleteServiceRecord, getServiceRecordsByVehicle (Flow), getLatestServiceRecord
8. data/local/dao/ServiceReminderDao.kt — @Dao with: insertReminder, updateReminder, getActiveReminderByVehicle (Flow), markReminderDone
9. data/local/dao/FuelLogDao.kt — @Dao with: insertFuelLog, deleteFuelLog, getFuelLogsByVehicle (Flow), getLastFuelLog, getTotalFuelCostThisMonth
10. data/local/dao/WorkshopDao.kt — @Dao with: insertWorkshop, updateWorkshop, deleteWorkshop, getAllWorkshops (Flow), getWorkshopById
11. data/local/AppDatabase.kt — @Database class linking all 5 entities and DAOs, singleton pattern with application context

Use ForeignKey with CASCADE on delete where specified in CLAUDE.md.
All list queries must return Flow<List<T>> for reactive updates.
```

**What to verify:**
- All files created with no compile errors
- Build succeeds (`./gradlew assembleDebug`)

---

## STEP 3 — Repository Layer

**Prompt to give Claude Code:**
```
Create the Repository layer for AutoBook. One repository per domain area.

Create these files:
1. data/repository/VehicleRepository.kt
   - Wraps VehicleDao
   - Exposes: getAllVehicles(): Flow<List<Vehicle>>, getVehicleById(), insertVehicle(), updateVehicle(), deleteVehicle()

2. data/repository/ServiceRepository.kt
   - Wraps ServiceRecordDao + ServiceReminderDao
   - Exposes: getServiceRecordsByVehicle(), insertServiceRecord(), deleteServiceRecord(), getLatestServiceRecord(), insertReminder(), markReminderDone(), getActiveReminderByVehicle()

3. data/repository/FuelRepository.kt
   - Wraps FuelLogDao
   - Exposes: getFuelLogsByVehicle(), insertFuelLog(), deleteFuelLog(), getLastFuelLog(), getTotalFuelCostThisMonth()
   - insertFuelLog() must auto-calculate kmPerLiter before inserting: fetch last FuelLog for that vehicle, compute (currentOdometer - lastOdometer) / liters; use 0.0f if no previous log exists

4. data/repository/WorkshopRepository.kt
   - Wraps WorkshopDao
   - Exposes: getAllWorkshops(), getWorkshopById(), insertWorkshop(), updateWorkshop(), deleteWorkshop()

All repository functions that write data should be suspend functions.
All repositories take their DAO as constructor parameter.
```

**What to verify:**
- All repository files compile with no errors

---

## STEP 4 — Dependency Injection (Manual DI)

**Prompt to give Claude Code:**
```
Set up manual dependency injection for AutoBook using an AppContainer pattern (no Hilt/Dagger).

Create:
1. AppContainer.kt — holds singleton instances of AppDatabase and all 4 repositories
2. AutoBookApplication.kt — extends Application, initializes AppContainer as a lazy property

Update AndroidManifest.xml to register AutoBookApplication as the application class.

Each ViewModel will receive its repository via a ViewModelFactory.
Create a base ViewModelFactory helper or use the factory pattern directly in each ViewModel file (we will create these per screen in later steps).
```

**What to verify:**
- App launches on emulator without crashing
- Logcat shows no initialization errors

---

## STEP 5 — App Navigation Setup

**Prompt to give Claude Code:**
```
Set up Navigation Compose for AutoBook with a bottom navigation bar.

Create:
1. ui/navigation/Screen.kt — sealed class defining all routes:
   Dashboard, VehicleList, ServiceList, FuelList, WorkshopList,
   AddVehicle, EditVehicle(vehicleId), AddService(vehicleId),
   AddFuel(vehicleId), AddWorkshop, EditWorkshop(workshopId)

2. ui/navigation/BottomNavItem.kt — data class for bottom nav items with route, label (@StringRes), and icon (ImageVector)

3. ui/navigation/AutoBookNavGraph.kt — NavHost composable with all routes registered.
   Bottom nav shows 5 tabs: Dashboard, Kendaraan, Servis, Bensin, Bengkel
   Use icons from Icons.Outlined (Home, DirectionsCar, Build, LocalGasStation, Store)

4. Update MainActivity.kt to host AutoBookNavGraph inside a Scaffold with the bottom nav bar.

Use rememberNavController(). Active tab should highlight based on current route.
Add strings for all tab labels in strings.xml.
```

**What to verify:**
- App launches and shows bottom navigation bar with 5 tabs
- Tapping each tab navigates without crashing (screens can be empty placeholders for now)

---

## STEP 6 — Vehicle Screen (List + Add)

**Prompt to give Claude Code:**
```
Build the Vehicle feature screens for AutoBook.

Create:
1. ui/viewmodel/VehicleViewModel.kt
   - Takes VehicleRepository as constructor param
   - Exposes: vehicles: StateFlow<List<Vehicle>>, insert/update/delete functions
   - Use viewModelScope + Dispatchers.IO for database operations

2. ui/viewmodel/VehicleViewModelFactory.kt — ViewModelProvider.Factory for VehicleViewModel

3. ui/screen/VehicleListScreen.kt — Composable showing:
   - LazyColumn of vehicle cards (nickname, brand+model, type badge "Motor"/"Mobil", year, current odometer)
   - FloatingActionButton to navigate to AddVehicleScreen
   - Empty state message if no vehicles exist
   - Tap a card → navigate to EditVehicle screen (placeholder for now)

4. ui/screen/AddVehicleScreen.kt — Composable form with:
   - Fields: nickname (TextField), type (SegmentedButton or RadioButton: Motor/Mobil),
     brand (TextField), model (TextField), year (TextField, numeric),
     current odometer in km (TextField, numeric)
   - Photo field: placeholder button (no camera logic yet, leave as TODO)
   - Save button: validates required fields (nickname, type, brand, model, year, odometer cannot be empty), calls ViewModel insert, navigates back
   - Top app bar with back button

Use Material3 components throughout. Show input validation errors inline below each field.
Add all user-facing strings to strings.xml.
```

**What to verify:**
- Vehicle list screen shows empty state correctly
- Add vehicle form validates and saves to Room
- Saved vehicle appears in the list immediately (reactive via Flow)

---

## STEP 7 — Edit & Delete Vehicle

**Prompt to give Claude Code:**
```
Add Edit and Delete functionality to the Vehicle feature.

Create:
1. ui/screen/EditVehicleScreen.kt — same form as AddVehicleScreen but:
   - Pre-populated with existing vehicle data loaded by vehicleId from nav argument
   - Save button calls ViewModel update
   - Add a Delete button at the bottom (with confirmation dialog before deleting)
   - Deleting a vehicle navigates back to VehicleListScreen

Update VehicleViewModel.kt to add:
- getVehicleById(id): StateFlow<Vehicle?> or a function that loads a single vehicle

Remind: deleting a vehicle triggers cascade delete of all its ServiceRecords,
FuelLogs, and ServiceReminders — this is handled by Room via the ForeignKey
cascade we set in Step 2, no extra logic needed.
```

**What to verify:**
- Tapping a vehicle card opens EditVehicleScreen with pre-filled data
- Editing saves correctly
- Delete confirmation dialog appears, and confirming delete removes the vehicle and navigates back

---

## STEP 8 — Service Record Screen (List + Add)

**Prompt to give Claude Code:**
```
Build the Service feature screens for AutoBook.

Create:
1. ui/viewmodel/ServiceViewModel.kt
   - Takes ServiceRepository as constructor param
   - Exposes:
     - serviceRecords: StateFlow<List<ServiceRecord>> (filtered by selected vehicleId)
     - activeReminder: StateFlow<ServiceReminder?> (for selected vehicle)
     - selectedVehicleId: StateFlow<Int?>
     - setSelectedVehicle(id: Int)
     - insertServiceRecord(record: ServiceRecord)
     - insertReminder(reminder: ServiceReminder)
     - markReminderDone(reminderId: Int)

2. ui/viewmodel/ServiceViewModelFactory.kt

3. ui/screen/ServiceListScreen.kt — Composable showing:
   - Vehicle filter chips at top (list of all vehicles, tap to filter)
   - Active reminder card (if exists): shows next service target (km or date), status badge (On Track / Due Soon / Overdue)
   - LazyColumn of service history cards: date, odometer, service types as chips, cost in Rp format
   - FloatingActionButton → navigate to AddServiceScreen(vehicleId)
   - Empty state if no records

4. ui/screen/AddServiceScreen.kt — form with:
   - Vehicle pre-selected from nav argument (show vehicle name, non-editable)
   - Date picker (DatePickerDialog, default today)
   - Odometer at service (TextField, numeric)
   - Service types: multi-select chips (Ganti Oli, Tune-Up, Ganti Ban, Servis Rem, Ganti Aki, Lainnya)
   - Cost in Rp (TextField, numeric, optional)
   - Notes (TextField, multiline, optional)
   - Reminder section:
     - Toggle switch "Set reminder"
     - If enabled: RemindBy selector (Jarak / Tanggal / Keduanya)
     - If "Jarak" or "Keduanya": input next km target
     - If "Tanggal" or "Keduanya": date picker for next service date
   - Save button: validates required fields, saves ServiceRecord + ServiceReminder (if set), navigates back

Add reminder status badge logic in a util/ReminderStatusUtil.kt:
- Input: currentOdometer (Int), nextKm (Int?), nextDate (Long?), today (Long)
- Output: enum ReminderStatus { ON_TRACK, DUE_SOON, OVERDUE }
- DUE_SOON if within 500 km or within 14 days
- OVERDUE if past target km or past target date
```

**What to verify:**
- Service list shows filtered by vehicle
- Reminder card shows correct status badge
- Add service form saves record and reminder correctly

---

## STEP 9 — Fuel Log Screen (List + Add)

**Prompt to give Claude Code:**
```
Build the Fuel Log feature screens for AutoBook.

Create:
1. ui/viewmodel/FuelViewModel.kt
   - Takes FuelRepository as constructor param
   - Exposes:
     - fuelLogs: StateFlow<List<FuelLog>> (filtered by vehicleId)
     - selectedVehicleId: StateFlow<Int?>
     - monthlyTotal: StateFlow<Int> (total fuel cost this month for selected vehicle)
     - setSelectedVehicle(id: Int)
     - insertFuelLog(vehicleId, fillDate, liters, pricePerLiter, odometerAtFill, fuelType)
       Note: totalCost and kmPerLiter are calculated inside FuelRepository, not here

2. ui/viewmodel/FuelViewModelFactory.kt

3. ui/screen/FuelListScreen.kt — Composable showing:
   - Vehicle filter chips at top
   - Summary card: "Total bulan ini: Rp X.XXX.XXX" and average km/liter this month
   - LazyColumn of fuel log cards:
     - Date, fuel type badge, liters, total cost in Rp
     - km/liter shown as "X.X km/L" (show "—" if 0.0, meaning first entry)
   - FloatingActionButton → AddFuelScreen(vehicleId)
   - Empty state if no logs

4. ui/screen/AddFuelScreen.kt — form with:
   - Vehicle pre-selected (show vehicle name, non-editable)
   - Date (DatePickerDialog, default today)
   - Liters (TextField, decimal, e.g. "10.5")
   - Price per liter in Rp (TextField, numeric)
   - Auto-display total cost as user types (liters × pricePerLiter, live update)
   - Odometer at fill (TextField, numeric)
   - Fuel type (dropdown: Pertalite, Pertamax, Pertamax Turbo, Solar, Dexlite)
   - Save button validates required fields, calls ViewModel insert, navigates back

Display km/liter with 1 decimal place. Display Rp values with dot-separated thousands.
```

**What to verify:**
- Fuel list shows per-vehicle history with km/liter calculated correctly
- First fuel log entry shows "—" for km/liter
- Monthly total updates reactively

---

## STEP 10 — Workshop Screen (List + Add + Edit)

**Prompt to give Claude Code:**
```
Build the Workshop bookmark screens for AutoBook.

Create:
1. ui/viewmodel/WorkshopViewModel.kt
   - Takes WorkshopRepository as constructor param
   - Exposes: workshops: StateFlow<List<Workshop>>, CRUD functions, searchQuery: StateFlow<String>
   - Filter workshops list by searchQuery (name contains query, case-insensitive)

2. ui/viewmodel/WorkshopViewModelFactory.kt

3. ui/screen/WorkshopListScreen.kt — Composable showing:
   - Search bar at top (filters list in real time)
   - LazyColumn of workshop cards:
     - Name, address (if set), star rating (show filled/empty stars), specialization chips
     - "Buka di Maps" button: if lat/lng set, opens Google Maps via Intent
       Uri: "geo:lat,lng?q=lat,lng(WorkshopName)"
     - Tap card → EditWorkshopScreen(workshopId)
   - FloatingActionButton → AddWorkshopScreen

4. ui/screen/AddWorkshopScreen.kt — form with:
   - Name (TextField, required)
   - Address (TextField, optional, multiline)
   - GPS tag section:
     - Button "Gunakan Lokasi Sekarang" — requests ACCESS_FINE_LOCATION permission,
       then fetches last known location via FusedLocationProviderClient,
       shows "Lokasi tersimpan: lat, lng" when successful
     - Show "Izin lokasi diperlukan" message if permission denied
   - Star rating (1–5, use clickable star icons)
   - Specialization: multi-select chips (Oli & Filter, Ban, Kelistrikan, Body & Cat, Umum)
   - Notes (TextField, optional, multiline)
   - Save button validates name is not empty, calls ViewModel insert, navigates back

5. ui/screen/EditWorkshopScreen.kt — same as Add but pre-populated, includes Delete button with confirmation dialog

Add ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions to AndroidManifest.xml.
```

**What to verify:**
- Workshop list shows with search filter working
- "Buka di Maps" opens Google Maps correctly
- GPS tag button requests permission and stores coordinates
- Add/Edit/Delete all work

---

## STEP 11 — Dashboard Screen

**Prompt to give Claude Code:**
```
Build the Dashboard screen for AutoBook — the home screen users see first.

Create:
1. ui/viewmodel/DashboardViewModel.kt
   - Takes VehicleRepository, ServiceRepository, FuelRepository as constructor params
   - For each vehicle, compute a DashboardVehicleCard data class containing:
     - vehicle: Vehicle
     - kmSinceLastService: Int (currentOdometer - odometerAtService from latest ServiceRecord; null if no service record)
     - reminderStatus: ReminderStatus (from ReminderStatusUtil, null if no active reminder)
     - fuelCostThisMonth: Int (from FuelRepository.getTotalFuelCostThisMonth)
   - Expose: dashboardCards: StateFlow<List<DashboardVehicleCard>>

2. ui/viewmodel/DashboardViewModelFactory.kt

3. ui/screen/DashboardScreen.kt — Composable showing:
   - App name "AutoBook" in top app bar
   - If no vehicles: empty state with illustration text and button "Tambah Kendaraan Pertama" → navigates to AddVehicleScreen
   - If vehicles exist: LazyColumn of vehicle summary cards, each showing:
     - Vehicle nickname + type badge (Motor/Mobil)
     - Brand + Model + Year
     - "Servis terakhir: X km yang lalu" or "Belum ada data servis"
     - Reminder status badge (On Track 🟢 / Due Soon 🟡 / Overdue 🔴) — only if reminder exists
     - "Bensin bulan ini: Rp X.XXX" — only if fuel log exists this month
   - Quick action buttons at bottom: "+ Catat Servis", "+ Isi Bensin"
     (these should show a vehicle picker bottom sheet if multiple vehicles exist)

Make the dashboard the start destination in the NavGraph.
```

**What to verify:**
- Dashboard shows empty state when no vehicles
- After adding a vehicle and service record, dashboard card updates correctly
- Status badge color is correct (green/yellow/red)
- Quick action buttons navigate correctly

---

## STEP 12 — Service Reminder Notifications

**Prompt to give Claude Code:**
```
Add background service reminder notifications to AutoBook.

Create:
1. util/NotificationHelper.kt
   - Creates notification channel "autobook_reminders" on app first launch
   - Function: showReminderNotification(context, vehicleNickname, message)
     Shows a notification with title "AutoBook — Reminder Servis" and the message

2. worker/ReminderCheckWorker.kt — WorkManager PeriodicWorkRequest:
   - Runs once per day
   - Queries all active ServiceReminders via AppDatabase directly (WorkManager context)
   - For each reminder, check ReminderStatusUtil
   - If DUE_SOON: notify "Servis [vehicle] sudah dekat — segera jadwalkan servis"
   - If OVERDUE: notify "Servis [vehicle] sudah lewat jadwal!"
   - If ON_TRACK: no notification

3. Schedule the worker in AutoBookApplication.kt on app start using:
   WorkManager.getInstance(this).enqueueUniquePeriodicWork(
     "reminder_check",
     ExistingPeriodicWorkPolicy.KEEP,
     PeriodicWorkRequestBuilder<ReminderCheckWorker>(1, TimeUnit.DAYS).build()
   )

Add WorkManager dependency to build.gradle.kts.
Add POST_NOTIFICATIONS permission to AndroidManifest.xml.
Handle Android 13+ runtime notification permission request in MainActivity.kt.
```

**What to verify:**
- Notification channel is created on first launch (check in phone Settings → Apps → AutoBook → Notifications)
- Worker is scheduled (check via `adb shell dumpsys jobscheduler`)
- Test notification appears by temporarily setting worker interval to 15 minutes

---

## STEP 13 — UI Polish

**Prompt to give Claude Code:**
```
Polish the UI for AutoBook to make it feel complete and consistent.

Do all of the following:

1. Theme setup — update ui/theme/Color.kt, Theme.kt, Type.kt:
   - Primary color: #1D6FE8 (blue, automotive feel)
   - Use Material3 dynamic color as fallback on Android 12+
   - Support both light and dark theme

2. App icon — create a simple launcher icon:
   - Foreground: a stylized car/speedometer outline in white
   - Background: primary blue color
   - Generate all required mipmap sizes using Android Studio Image Asset tool instructions
     (add a comment in code telling developer to use Image Asset tool with the provided description)

3. Consistent empty states — every list screen (Vehicle, Service, Fuel, Workshop) must have:
   - A centered icon (use a relevant Material icon, size 64dp, tinted with outline color)
   - A headline text e.g. "Belum ada kendaraan"
   - A subtext e.g. "Tap tombol + untuk menambahkan kendaraan pertama"

4. Loading states — wrap all StateFlow collections in a UiState<T> sealed class:
   sealed class UiState<out T> { object Loading; data class Success<T>(val data: T); data class Error(val message: String) }
   Show a CircularProgressIndicator while Loading.

5. Confirm all strings are in strings.xml — scan all screen files for hardcoded Indonesian or English UI strings and move them to strings.xml if any remain.

6. Snackbar feedback — after every successful insert/update/delete operation, show a Snackbar:
   - "Kendaraan berhasil disimpan", "Servis berhasil dicatat", "Bengkel berhasil dihapus", etc.
```

**What to verify:**
- App looks consistent across all screens
- Dark mode works without any hardcoded colors showing
- All list screens have proper empty states
- Snackbar appears after each action

---

## STEP 14 — Final Testing & Play Store Prep

**Prompt to give Claude Code:**
```
Prepare AutoBook for Google Play Store submission.

Do all of the following:

1. Update AndroidManifest.xml:
   - Add android:label="AutoBook"
   - Add android:description (short app description string in strings.xml)
   - Verify all required permissions are declared
   - Add android:exported="false" on all activities except MainActivity

2. Update build.gradle.kts (app level):
   - Set versionCode = 1
   - Set versionName = "1.0.0"
   - Enable minification for release build:
     buildTypes {
       release {
         isMinifyEnabled = true
         isShrinkResources = true
         proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
       }
     }

3. Add ProGuard rules in proguard-rules.pro to keep Room entities and DAOs from being obfuscated:
   -keep class com.autobook.app.data.local.entity.** { *; }
   -keep class com.autobook.app.data.local.dao.** { *; }

4. Create a release build:
   Run: ./gradlew bundleRelease
   (This produces an .aab file at app/build/outputs/bundle/release/app-release.aab)

5. Generate a signed keystore:
   Provide step-by-step terminal commands to generate a keystore file using keytool.
   Do NOT hardcode any passwords — show placeholders like YOUR_KEYSTORE_PASSWORD.

6. Write a short Play Store listing text and save it as PLAY_STORE_LISTING.md:
   - App name: AutoBook
   - Short description (max 80 chars)
   - Full description (max 4000 chars) — highlight: multi-vehicle support, offline/no login, service reminders, fuel efficiency tracking, workshop bookmarks with GPS
   - Suggested tags/categories: Auto & Vehicles

7. Add a README.md to the project root with:
   - Project overview
   - How to build locally
   - Tech stack summary
   - Folder structure
```

**What to verify:**
- Release build (`bundleRelease`) completes without errors
- ProGuard does not strip any Room entities (check build output for warnings)
- PLAY_STORE_LISTING.md and README.md are created

---

## Done! 🎉

At this point AutoBook Phase 1 is complete and ready for Play Store submission.

### Summary of what was built:
- 5-table Room database (Vehicle, ServiceRecord, ServiceReminder, FuelLog, Workshop)
- MVVM architecture with Repository pattern and manual DI
- 5 main screens + 7 sub-screens via Navigation Compose
- Smart service reminders with daily background check via WorkManager
- Fuel efficiency auto-calculation (km/liter)
- Workshop bookmark with GPS tagging + open in Google Maps
- Full light/dark theme support
- Play Store ready release build

### Phase 2 ideas (future):
- Community workshop ratings (requires backend)
- Export service history to PDF
- Odometer photo OCR (auto-read km from photo)
- Vehicle insurance & STNK expiry reminders
- Multi-language support (EN + ID)
