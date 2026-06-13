# AutoBook — Project Context for Claude Code

## What is AutoBook?
AutoBook is an Android app for tracking personal vehicle maintenance.
Users can log service history, fuel usage, and save favorite workshops.
Primary market: Indonesia. All monetary values in Rupiah (IDR).

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Database:** Room (SQLite, offline-first, no login required)
- **Architecture:** MVVM (ViewModel → Repository → DAO)
- **Async:** Kotlin Coroutines + StateFlow
- **Navigation:** Jetpack Navigation Compose
- **Location:** Google Maps / FusedLocationProvider (for GPS tagging workshops)
- **Min SDK:** API 26 (Android 8.0 Oreo)
- **Target SDK:** API 35
- **Build system:** Gradle with Kotlin DSL

## Project Structure
```
app/
├── data/
│   ├── local/
│   │   ├── entity/         # Room entity classes
│   │   ├── dao/            # Room DAO interfaces
│   │   └── AppDatabase.kt  # RoomDatabase class
│   └── repository/         # Repository classes
├── ui/
│   ├── screen/             # Composable screen files
│   ├── component/          # Reusable UI components
│   ├── viewmodel/          # ViewModel classes
│   └── theme/              # Colors, typography, shapes
├── util/                   # Helper/utility functions
└── MainActivity.kt
```

## Database Schema (5 Tables)

### Vehicle
| Column | Type | Notes |
|---|---|---|
| id | Int (PK, autoGenerate) | |
| nickname | String | User-given name, e.g. "Motor Harian" |
| type | String | "motor" or "mobil" |
| brand | String | e.g. "Honda", "Toyota" |
| model | String | e.g. "Vario 125", "Avanza" |
| year | Int | manufacture year |
| currentOdometer | Int | in km, updated manually |
| photoPath | String? | nullable, local file path |
| createdAt | Long | epoch milliseconds |

### ServiceRecord
| Column | Type | Notes |
|---|---|---|
| id | Int (PK, autoGenerate) | |
| vehicleId | Int (FK → Vehicle.id) | cascade delete |
| serviceDate | Long | epoch milliseconds |
| odometerAtService | Int | km at time of service |
| serviceTypes | String | comma-separated, e.g. "oli,tune_up,rem" |
| cost | Int | Rupiah, integer only |
| notes | String? | nullable |

### ServiceReminder
| Column | Type | Notes |
|---|---|---|
| id | Int (PK, autoGenerate) | |
| serviceRecordId | Int (FK → ServiceRecord.id) | cascade delete |
| vehicleId | Int (FK → Vehicle.id) | for quick query |
| remindBy | String | "km", "date", or "both" |
| nextKm | Int? | nullable, target odometer |
| nextDate | Long? | nullable, epoch milliseconds |
| isDone | Int | 0 = active, 1 = done |

### FuelLog
| Column | Type | Notes |
|---|---|---|
| id | Int (PK, autoGenerate) | |
| vehicleId | Int (FK → Vehicle.id) | cascade delete |
| fillDate | Long | epoch milliseconds |
| liters | Float | amount filled |
| pricePerLiter | Int | Rupiah |
| totalCost | Int | Rupiah = liters × pricePerLiter, rounded |
| odometerAtFill | Int | km at time of fill |
| fuelType | String | "Pertalite", "Pertamax", "Pertamax Turbo", "Solar", "Dexlite" |
| kmPerLiter | Float | auto-calculated: (odometerAtFill - previous odometerAtFill) / liters; 0.0 if first entry |

### Workshop
| Column | Type | Notes |
|---|---|---|
| id | Int (PK, autoGenerate) | |
| name | String | workshop name |
| address | String? | nullable, free text |
| latitude | Double? | nullable, GPS coordinate |
| longitude | Double? | nullable, GPS coordinate |
| rating | Int | 1–5 stars |
| specialization | String | "oli", "ban", "listrik", "body", "umum" (comma-separated for multi) |
| notes | String? | nullable, personal notes |
| savedAt | Long | epoch milliseconds |

## Key Business Rules
- Deleting a Vehicle must cascade-delete all its ServiceRecords, FuelLogs, and ServiceReminders
- Workshop has NO foreign key to Vehicle — it is standalone
- `kmPerLiter` in FuelLog is calculated at insert time, not on-the-fly
- `ServiceReminder.vehicleId` is denormalized (duplicated from ServiceRecord) for faster dashboard queries
- Dashboard shows: km since last service, next service alert (red if overdue, yellow if close), and total fuel cost this month
- Reminder alert logic: trigger yellow if within 500km or 14 days of target; trigger red if past target

## Coding Conventions
- All UI strings must go in `res/values/strings.xml`
- Use `@StringRes` annotations where applicable
- Date display format: `dd MMM yyyy` (e.g. "05 Jun 2026"), use Indonesian locale
- Currency display: prefix `Rp` with dot-separated thousands (e.g. `Rp 45.000`)
- Comments in English for non-trivial logic
- No hardcoded colors — use MaterialTheme.colorScheme tokens
- ViewModel should never import anything from `android.view` (Compose only)
- Each screen has its own ViewModel

## Current Build Phase
**Phase 1 MVP** — see `AUTOBOOK_BUILD_PLAN.md` for full step-by-step instructions.
