# CLAUDE.md — Dusk Mod

## Project Overview
Dusk is a Fabric mod for Minecraft that introduces real psychological phenomena into gameplay.
No jump scares. No monsters. Fear comes from the player's own mind.

First release: **Phobia Edition** — 3 phobia modules.

---

## Tech Stack
- **Minecraft**: 1.21.1
- **Mod Loader**: Fabric (only, no Forge/Quilt support)
- **Fabric Loader**: 0.16.x
- **Fabric API**: 0.116.x
- **Java**: 21
- **Build Tool**: Gradle
- **Mappings**: Mojang mappings

---

## Design Philosophy
- ไม่มี UI บอกผู้เล่นว่ากำลังเกิดอะไรขึ้น
- ไม่มี config screen อธิบาย mechanic
- ไม่มี death screen ไม่มี message log
- ผู้เล่นต้องค้นพบ trigger เอง
- เมื่อ episode จบ ทุกอย่างกลับมาปกติเหมือนไม่มีอะไรเกิดขึ้น

---

## Module 1 — Nyctophobia (กลัวความมืด)

### Trigger Condition
- `lightLevel <= 3`
- reset ทันทีเมื่อ `lightLevel > 3`
- ทำงานเฉพาะ dimension ปกติ (overworld)

### Variables
```
darknessMultiplier:
  light 0 = 1.00x
  light 1 = 0.75x
  light 2 = 0.50x
  light 3 = 0.25x

silenceMultiplier:
  mob density รอบ 16 block = 0 → 1.2x
  มี mob อยู่ → 1.0x

dreadScore = ticksInDarkness × darknessMultiplier × silenceMultiplier
```

### Stage Thresholds (based on dreadScore)
| Stage | dreadScore | Effect |
|-------|------------|--------|
| 1 | 0–300 | ambient sound หายไป เงียบสนิท |
| 2 | 300–600 | เสียงหลอน (footstep, mob sound ที่ไม่มี entity) |
| 3 | 600–900 | FOV pulse เบาๆ + screen edge มืดลง (vignette) |
| 4 | 900–1100 | input drift — movement ผิดทิศเล็กน้อย |
| 5 | 1100–1300 | particle หลอน ที่มี render แต่ไม่มี hitbox |
| 6 | 1300+ | fade out → teleport to spawn → of ครบ ไม่มี message |

### Teleport Behavior
- fade to black (0.5 วินาที)
- teleport to spawn point (เตียงหรือ world spawn)
- ตื่นขึ้นมาทันที ไม่มี transition ไม่มี sound
- inventory ครบ 100%
- ไม่มี chat message ไม่มี title ไม่มีอะไรเลย

### World Modification
- mob hostile spawn rate = 0 (โลกเงียบ)
- passive mob spawn ลดลง 80%
- ผู้เล่นต้องพึ่งพาเกษตรกรรมและล่าสัตว์ passive เป็นหลัก

---

## Module 2 — Acrophobia (กลัวที่สูง)

### Trigger Condition
- ผู้เล่นอยู่ที่ Y >= 150
- **และ** กำลัง look down ไปยังพื้นที่ต่ำกว่า 30 block ขึ้นไป
- reset ทันทีเมื่อออกจาก condition

### Stage Thresholds
| Stage | Duration | Effect |
|-------|----------|--------|
| 1 | 0–5s | screen edge vignette |
| 2 | 5–15s | FOV แคบลงเล็กน้อย |
| 3 | 15–25s | มือสั่น (camera shake เบา) |
| 4 | 25–35s | movement ช้าลง เข้าใกล้ขอบยิ่งช้า |
| 5 | 35s+ | freeze ชั่วคราว 2 วินาที แล้ว reset stage |

### หมายเหตุ
- ไม่มี teleport ใน Acrophobia เพราะอาการจริงไม่ทำให้หมดสติ
- แค่ทำให้เข้าใกล้ขอบยากขึ้น

---

## Module 3 — Thalassophobia (กลัวน้ำลึก)

### Trigger Condition
- ผู้เล่นอยู่ใน deep ocean biome
- อยู่ในน้ำ หรือ อยู่เหนือน้ำแต่ depth ใต้เท้า >= 30 block
- นับเวลาต่อเนื่อง reset เมื่อออกจาก biome หรือขึ้นฝั่ง

### Stage Thresholds
| Stage | Duration | Effect |
|-------|----------|--------|
| 1 | 0–10s | ambient sound เปลี่ยนเป็น underwater tone |
| 2 | 10–25s | visibility ลดลง (fog เพิ่ม) |
| 3 | 25–40s | เสียงหลอนใต้น้ำ |
| 4 | 40–55s | เงา entity ที่ไม่มีอยู่จริงใต้น้ำ |
| 5 | 55–70s | panic — swim speed ลดลง หายใจเร็วขึ้น (sound) |
| 6 | 70s+ | fade out → teleport to spawn → ของครบ ไม่มี message |

---

## File Structure
```
dusk/
├── src/main/java/com/dusk/
│   ├── Dusk.java                  # mod initializer
│   ├── tracker/
│   │   ├── DreadTracker.java      # core — track dreadScore per player
│   │   └── DreadStage.java        # enum stages
│   ├── module/
│   │   ├── NyctophobiaModule.java
│   │   ├── AcrophobiaModule.java
│   │   └── ThalassophobiaModule.java
│   ├── event/
│   │   └── PhobiaEventHandler.java # รับ stage → trigger effect
│   └── effect/
│       ├── SoundEffects.java
│       ├── VisualEffects.java
│       └── MovementEffects.java
├── src/main/resources/
│   ├── fabric.mod.json
│   └── assets/dusk/
└── build.gradle
```

---

## Project Setup (ทำครั้งแรกครั้งเดียว)

1. ดึง Fabric template จาก official source
```bash
git clone https://github.com/FabricMC/fabric-example-mod.git dusk
cd dusk
```

2. แก้ `build.gradle` ให้ตรงกับ version ที่ใช้
```gradle
minecraft_version=1.21.1
yarn_mappings=1.21.1+build.3
loader_version=0.16.9
fabric_version=0.116.0+1.21.1
```

3. แก้ `fabric.mod.json`
```json
{
  "id": "dusk",
  "version": "0.1.0",
  "name": "Dusk",
  "description": "Minecraft is quieter than you remember.",
  "authors": ["your name"],
  "environment": "*",
  "entrypoints": {
    "main": ["com.dusk.Dusk"]
  },
  "depends": {
    "fabricloader": ">=0.16.0",
    "fabric-api": "*",
    "minecraft": "~1.21.1"
  }
}
```

4. ลบ example code ทิ้งทั้งหมดใน `src/` แล้วเริ่ม scaffold ตาม file structure ด้านล่าง

5. ทดสอบว่า template พร้อมด้วย
```bash
./gradlew build
```

---

## Rules for Claude Code
1. อย่าเพิ่ม feature ที่ไม่ได้อยู่ใน CLAUDE.md โดยไม่ถาม
2. อย่าสร้าง config screen หรือ UI ใดๆ ทั้งสิ้น
3. อย่าส่ง chat message หรือ title ถึงผู้เล่นในทุกกรณี
4. ถ้า API ใน 1.21.1 ไม่มี method ที่ต้องการ ให้บอกและเสนอทางเลือก อย่า assume
5. build ต้องผ่าน `./gradlew build` โดยไม่มี error และ warning
