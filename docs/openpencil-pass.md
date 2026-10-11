# OpenPencil pass — betondz

## Design (Shared/OpenPencil/betondz/)
- `design.html` (concrete calculator + result + bottom nav, DZ tokens) → `design.fig` via `openpencil import`
- `lint --preset accessibility`: 0 errors, 0 warnings

## Code changes
- NEW `DzTheme.kt`: DZ 2026 light/dark M3 schemes + dynamic color (API 31+) + `DzEmptyState`.
- `MainActivity.kt`: wrapped in `DzTheme`; bottom-nav items now have icons + `contentDescription`
  (were `icon = {}`); dosage radio rows use `selectableGroup` + `Role.RadioButton` (TalkBack).

## APKs
`android.yml` now builds debug AND release (`assembleRelease`, unsigned) and uploads both
(`betondz-debug-apk`, `betondz-release-apk`). Release APK also available per run in Artifacts.
