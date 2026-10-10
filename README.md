# BetonDZ — مقدّر الخرسانة (offline concrete/mortar estimator, Algeria)

Offline-first Android app (Kotlin, Compose Material3, Room). No account, no network.
- Concrete dosage calculator (150/250/300/350/400 kg/m³, E/C 0.50)
- Mortar calculator (250/300/350)
- Materials cost estimator in DZD (editable prices; default cement 50kg = 490 DZD)
- Chantier logbook (Room)
- FR/AR (RTL via supportsRtl)

## Dosage references
NF EN 206/CN classes; calculis.net; calculs-btp.fr. 1m³ @350 = 350kg cement, ~820kg sand, ~1125kg gravel, 175L water.

## Build
CI runs `gradle test lint assembleDebug` and uploads the debug APK artifact.

## Limitations
Estimates only, dry aggregates; reduce water 10–20% if sand is wet; validate structural work with an engineer.
MIT clean-room code.
