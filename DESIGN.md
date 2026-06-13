# AutoBook Design System v1.0
> UI redesign specification. Read this together with CLAUDE.md.
> Scope: visual layer only. Do NOT modify ViewModels, Repositories, DAOs,
> entities, navigation routes, or any business logic. Only files in
> `ui/screen/`, `ui/component/`, and `ui/theme/` may be changed or created.

## Design Direction

Style: modern clean (fintech-grade polish) with automotive character.
References: modern Indonesian fintech apps (clean white surfaces, one
confident accent color) combined with bold display typography and a
floating pill bottom navigation. Numbers are heroes: odometer and
fuel-efficiency figures use tabular/monospace styling like a digital
speedometer.

Key principles:
1. White surfaces on a near-white background. Cards rise via subtle
   border + minimal elevation, never heavy shadows.
2. One accent color used decisively. Everything else is neutral.
3. Strong visual hierarchy: key numbers are large and bold, labels are
   small and muted.
4. Status is color-coded and instantly glanceable (green/amber/red).
5. Generous whitespace. Never cram.

---

## 1. Color Tokens

Create these in `ui/theme/Color.kt`. Implement BOTH light and dark scheme
in `ui/theme/Theme.kt` using Material 3 `lightColorScheme()` /
`darkColorScheme()` plus a custom `AutoBookColors` object (via
CompositionLocal) for tokens that Material slots do not cover.

### Light theme
| Token | Hex | Usage |
|---|---|---|
| `primary` | #1D6FE8 | CTA buttons, active nav item, links, focus |
| `onPrimary` | #FFFFFF | text/icon on primary |
| `primaryContainer` | #E6EEFC | icon chips, tonal buttons, selected chips |
| `onPrimaryContainer` | #0C447C | text/icon on primaryContainer |
| `background` | #FAFBFE | screen background (near-white, cool tint) |
| `surface` | #FFFFFF | cards, sheets, nav |
| `surfaceVariant` | #F1F4FA | input backgrounds, inactive chips |
| `outline` | #E3E8F2 | card borders, dividers (use at 1dp) |
| `textPrimary` | #101522 | headings, key numbers |
| `textSecondary` | #5F6B85 | labels, captions, metadata |
| `textTertiary` | #98A2B8 | hints, disabled, inactive nav |
| `statusSuccess` | #0F6E56 on container #E1F5EE | reminder ON_TRACK |
| `statusWarning` | #854F0B on container #FAEEDA | reminder DUE_SOON, "no service data" |
| `statusDanger` | #A32D2D on container #FCEBEB | reminder OVERDUE |
| `accentMotor` | #0F6E56 on container #E1F5EE | motorcycle icon chip |
| `accentMobil` | #1D6FE8 on container #E6EEFC | car icon chip |
| `navBackground` | #14181F | floating pill bottom nav |
| `navActive` | #FFFFFF | active nav icon/label |
| `navInactive` | #8A94A8 | inactive nav icon/label |

### Dark theme
| Token | Hex |
|---|---|
| `primary` | #5B97F0 |
| `onPrimary` | #04203F |
| `primaryContainer` | #163358 |
| `onPrimaryContainer` | #B5D4F4 |
| `background` | #0E1116 |
| `surface` | #181D26 |
| `surfaceVariant` | #1F2735 |
| `outline` | #2A3140 |
| `textPrimary` | #ECF1F8 |
| `textSecondary` | #8A94A8 |
| `textTertiary` | #5F6B85 |
| `statusSuccess` | #4DD0A1 on container #16291F |
| `statusWarning` | #EF9F27 on container #2E2414 |
| `statusDanger` | #F09595 on container #2E1717 |
| `navBackground` | #1F2735 |

Rules:
- Never hardcode hex values inside screen composables. Always reference
  theme tokens.
- Status colors must always pair text-on-container from the same family.

---

## 2. Typography

Define in `ui/theme/Type.kt`. Font: keep default system font (Roboto) for
body, but ALL numeric data (odometer, km/L, Rupiah amounts, liters) must
use `FontFamily.Monospace` OR Roboto with
`fontFeatureSettings = "tnum"` (tabular numbers). Prefer the tabular
approach; fall back to Monospace if simpler.

| Style name | Size / Weight | Usage |
|---|---|---|
| `displayNumber` | 28sp / SemiBold / tabular | hero numbers (monthly fuel total) |
| `headlineLarge` | 24sp / Bold | screen titles ("Kendaraan", "Servis") |
| `titleMedium` | 16sp / SemiBold | card titles (vehicle nickname) |
| `numberMedium` | 16sp / SemiBold / tabular | odometer on cards, km/L values |
| `bodyMedium` | 14sp / Regular | general body text |
| `labelMedium` | 12sp / Medium | metadata (brand · year), section labels |
| `labelSmall` | 11sp / Medium | badges, nav labels, units ("km", "/L") |

Number formatting (create `util/FormatUtil.kt` if not present):
- Odometer: dot thousands separator, unit in labelSmall textSecondary:
  `54.854 km`
- Currency: `Rp 45.000` (dot separator, no decimals)
- Fuel efficiency: one decimal, comma as decimal separator (Indonesian
  locale): `42,5 km/L`
- Dates: `11 Jun 2026` (Indonesian locale month abbreviations)

---

## 3. Shape & Spacing

| Token | Value | Usage |
|---|---|---|
| `radiusCard` | 16dp | all cards |
| `radiusButton` | 12dp | buttons, tonal actions |
| `radiusChip` | 999dp (full) | badges, filter chips, FAB nav pill |
| `radiusIconChip` | 14dp | the square icon container on cards |
| screen padding | 20dp horizontal | every screen |
| card padding | 16dp | inside all cards |
| card gap | 12dp | vertical gap in lists |
| section gap | 24dp | between sections |

Elevation: cards use `1dp` tonal elevation + 1dp outline border. No drop
shadows anywhere except the floating bottom nav (8dp) and FAB (6dp).

---

## 4. Core Components

Create these in `ui/component/`. Every screen must use these shared
components. No screen-local duplicates.

### 4.1 `AutoBookCard`
Surface, radiusCard, 1dp outline border, 16dp padding, onClick optional.
The base container for every card in the app.

### 4.2 `VehicleIconChip(type: String, size: Dp = 46.dp)`
Rounded square (radiusIconChip). If type == "motor": motorcycle icon,
accentMotor colors. If type == "mobil": car icon, accentMobil colors.
Use Material Icons Extended: `Icons.Outlined.TwoWheeler` and
`Icons.Outlined.DirectionsCar`. Add the `material-icons-extended`
dependency if missing.

### 4.3 `StatusBadge(status: ReminderStatus?)`
Full-rounded pill, labelSmall, 4dp vertical / 10dp horizontal padding.
- ON_TRACK: "On track" with statusSuccess colors
- DUE_SOON: "Segera servis" with statusWarning colors
- OVERDUE: "Lewat jadwal" with statusDanger colors
- null (no reminder/service yet): "Belum ada servis" with statusWarning

### 4.4 `SummaryCard`
Two variants:
- `Filled`: primary background, onPrimary text. Label in labelMedium at
  70% alpha, value in displayNumber.
- `Outlined`: surface background, outline border. Label textSecondary,
  value displayNumber textPrimary.

### 4.5 `TonalActionButton(icon, label, onClick)`
primaryContainer background, onPrimaryContainer content, radiusButton,
12dp padding, icon 18dp + label labelMedium SemiBold. Used for quick
actions on Dashboard.

### 4.6 `AutoBookBottomNav`
THE signature component. Floating pill:
- Container: navBackground color, full-rounded (999dp), 8dp shadow
- Margins: 16dp horizontal, 16dp from bottom (use `navigationBarsPadding`)
- Height: 64dp
- 5 items, each: icon 22dp + label labelSmall below
- Active item: navActive color + a small 4dp dot OR pill highlight behind
  the icon using `primary` at 20% alpha
- Inactive: navInactive color
- Important: every screen's scrollable content needs bottom content
  padding of ~96dp so the floating nav never covers the last item.

### 4.7 `AutoBookFab`
Standard Material 3 FAB, primary container color = `primary`,
onPrimary icon, 6dp elevation. Positioned 16dp above the floating nav
(use offset or padding so they do not overlap).

### 4.8 `EmptyState(icon, headline, subtext)`
Centered column: icon 56dp in a 96dp circle of surfaceVariant, headline
titleMedium textPrimary, subtext bodyMedium textSecondary, 12dp gaps.
No character illustrations. Icon only.

### 4.9 `SectionHeader(title, actionLabel?, onAction?)`
Row: title in titleMedium, optional trailing action text in labelMedium
primary color ("Lihat semua").

---

## 5. Screen Specs

### 5.1 Dashboard (Beranda)
Top to bottom:
1. Header row (20dp top padding):
   - Left column: today's date in labelMedium textSecondary
     ("Kamis, 11 Jun"), below it "Halo!" greeting in headlineLarge.
     If user has vehicles, greeting stays generic "Halo!" (we store no
     user name).
   - Right: 40dp rounded-square (12dp radius) primaryContainer chip with
     bell icon (notification affordance, non-functional placeholder OK).
2. Summary row (two cards, 10dp gap):
   - SummaryCard.Filled: label "Bensin bulan ini", value = total fuel
     cost this month across ALL vehicles, formatted Rupiah.
   - SummaryCard.Outlined: label "Servis berikutnya", value = nearest
     upcoming reminder target across vehicles. Show "X km lagi" or a
     date, or "Belum diset" if none.
3. SectionHeader "Kendaraanku" + "Lihat semua" action navigating to
   the Kendaraan tab.
4. Vehicle cards list. Each card (AutoBookCard) row layout:
   - VehicleIconChip (left)
   - Middle column: nickname titleMedium, "Brand Model · Year" in
     labelMedium textSecondary, odometer in numberMedium with "km" unit
     in labelSmall textSecondary
   - Right: StatusBadge
5. Quick actions row: two TonalActionButtons, "Catat servis" (wrench
   icon) and "Isi bensin" (gas station icon). If multiple vehicles,
   tapping opens the existing vehicle picker bottom sheet.
6. Empty state (no vehicles): EmptyState with car icon, "Belum ada
   kendaraan", "Tambahkan kendaraan pertamamu", plus a filled primary
   button "Tambah Kendaraan".

### 5.2 Kendaraan
1. Screen title "Kendaraan" headlineLarge, 20dp padding.
2. Vehicle cards, same card component as Dashboard but right side shows
   chevron-right icon in textTertiary instead of StatusBadge (status
   lives on Dashboard; this screen is about managing vehicles).
3. AutoBookFab bottom-right for add.
4. Empty state as per Dashboard.

### 5.3 Servis
1. Title "Servis".
2. Vehicle filter chips row: full-rounded chips. Selected =
   primaryContainer bg + onPrimaryContainer text. Unselected =
   surfaceVariant bg + textSecondary text. Horizontal scroll if many.
3. Active reminder card (if exists): AutoBookCard with a 3dp left accent
   handled as a Row containing a 3dp colored vertical bar (status color)
   + content: "Servis berikutnya" labelMedium, target in numberMedium
   ("60.000 km" or date), StatusBadge on the right.
4. History list. Each AutoBookCard:
   - Row 1: service date labelMedium textSecondary (left), cost in
     numberMedium (right)
   - Row 2: service types as small chips (surfaceVariant bg, labelSmall,
     e.g. "Ganti Oli" "Tune-Up")
   - Row 3: odometer "di 54.854 km" labelMedium textTertiary
5. FAB. EmptyState: wrench icon, "Belum ada catatan servis",
   "Tap tombol + untuk mencatat servis pertama".

### 5.4 Bensin
1. Title "Bensin". Filter chips as Servis.
2. Hero summary: SummaryCard.Filled full-width:
   - "Total bulan ini" label, Rupiah value in displayNumber
   - Below value: "Rata-rata 42,5 km/L" in labelMedium at 70% alpha
     (omit the line if no data)
3. Log list. Each AutoBookCard row:
   - Left: 40dp icon chip with gas-station icon, primaryContainer
   - Middle: fuel type + liters in titleMedium-sized line
     ("Pertalite · 4,5 L"), date in labelMedium textSecondary
   - Right column (end-aligned): total cost numberMedium, km/L below in
     labelSmall statusSuccess color (or "—" textTertiary for first
     entry)
4. FAB. EmptyState: gas pump icon, existing copy.

### 5.5 Bengkel
1. Title "Bengkel".
2. Search field: surfaceVariant background, radiusButton, search icon
   leading, "Cari bengkel..." hint in textTertiary. No visible border
   until focused (primary 2dp on focus).
3. Workshop cards. Each AutoBookCard:
   - Row 1: name titleMedium (left), star rating right: filled stars in
     statusWarning amber, empty stars in outline color, 16dp
   - Row 2: address labelMedium textSecondary, maxLines 1 ellipsis
   - Row 3: specialization chips (surfaceVariant, labelSmall)
   - Row 4: "Buka di Maps" as a text button with map-pin icon, primary
     color, only if lat/lng present
4. FAB. EmptyState: store icon, existing copy.

### 5.6 Form screens (AddVehicle, AddService, AddFuel, AddWorkshop, Edit*)
Shared form style:
- Top app bar: surface background, back arrow, title titleMedium,
  no elevation, 1dp bottom outline divider.
- Text fields: `OutlinedTextField` restyled: surfaceVariant container,
  no visible outline when unfocused, 2dp primary outline focused,
  radiusButton corners, labels above the field in labelMedium
  textSecondary (not floating labels).
- Selection chips (type Motor/Mobil, service types, fuel type,
  specialization): full-rounded, selected = primaryContainer +
  onPrimaryContainer, unselected = surfaceVariant + textSecondary.
- Primary submit button: full-width, 52dp height, primary bg, onPrimary
  text titleMedium, radiusButton, fixed at the bottom with 20dp padding.
- Destructive (Delete) button: text button, statusDanger color, placed
  below submit.
- Live total on AddFuel (liters × price) shows in a SummaryCard.Outlined
  above the submit button, value in displayNumber.

---

## 6. Motion (keep minimal)

- Screen transitions: default Navigation Compose fade-through is fine.
- List item appearance: no staggered animations (keep it fast).
- StatusBadge and summary values: `animateContentSize()` where values
  change.
- Bottom nav active indicator: animate color change 150ms.

---

## 7. Implementation Order for Claude Code

Execute as separate sessions, verify each on emulator before next:

1. `ui/theme/` rewrite: Color.kt, Type.kt, Theme.kt, Shape.kt + FormatUtil
2. `ui/component/`: all 9 shared components (no screen changes yet)
3. Replace bottom nav + FAB integration in MainActivity/NavGraph scaffold
4. Dashboard screen
5. Kendaraan screen + its form screens
6. Servis screen + form
7. Bensin screen + form
8. Bengkel screen + forms
9. Final pass: dark theme verification on every screen, content padding
   audit (nothing hidden behind floating nav)

Per-session prompt template:
```
Read DESIGN.md section <X>. Implement it exactly as specified.
Do not modify any ViewModel, Repository, DAO, entity, or navigation
route. Only change ui/theme, ui/component, and the specified screen
files. After implementing, list every file you changed.
```

## 8. Acceptance Checklist

- [ ] No hardcoded colors in any screen composable
- [ ] All numbers render with tabular figures / monospace
- [ ] Floating pill nav never overlaps list content (96dp bottom padding)
- [ ] Status badge colors correct in light AND dark mode
- [ ] Odometer formatted with dot separators everywhere
- [ ] Rupiah formatted "Rp X.XXX" everywhere
- [ ] Empty states use icon style, no character illustrations
- [ ] FAB does not cover the floating nav
- [ ] All existing functionality still works (no logic regressions)
