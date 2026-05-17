# Unlock Mod — Context for Claude

## Environment

- **Minecraft**: Java Edition 1.21.10
- **Mod Loader**: Fabric
- **Fabric Loader**: 0.17.2
- **Fabric API**: 0.138.3+1.21.10
- **Mappings**: Mojang official mappings (ไม่ใช้ Yarn — เลิก support ตั้งแต่ 1.21.4)
- **Fabric Loom**: 1.11-SNAPSHOT (ต้องการ Gradle 8.14+)
- **Gradle**: 8.14.5
- **Java**: 21
- **Mod Menu**: ใช้สำหรับ config UI
- **Cloth Config**: ใช้สำหรับ config system

## Build Commands

```powershell
# Build ปกติ
.\gradlew.bat build

# ถ้า wrapper ผิดเวอร์ชัน (รันแค่ครั้งแรก)
gradle wrapper --gradle-version 8.14.5
.\gradlew.bat build
```

Output jar อยู่ที่ `build/libs/unlock-1.0.0.jar`
Copy ไปที่ `.minecraft/mods/`

## Mod Info

- **Mod ID**: `unlock`
- **Name**: Unlock
- **Description**: ปลดขีดจำกัดต่างๆ ของ Minecraft ทั้งฝั่ง client และ server
- **Environment**: `*` (ทั้ง client และ server)
- **Package**: `com.example.unlock`

## Features & Logic

### 1. Render Distance (Client only)
- ขยาย slider สูงสุดจาก 32 → 128 chunks
- Mixin target: `GameOptions` หรือ `OptionInstance` ที่ควบคุม render distance
- Toggle ใน Mod Menu: เปิด/ปิด และ slider เลือก max chunks

### 2. Particle Limit (Client only)
- ปลด particle limit ที่ Minecraft จำกัดไว้ประมาณ 16,384
- Mixin target: `ParticleEngine` (Mojang mappings) — method ที่ตัด particle เมื่อเกิน limit
- Toggle ใน Mod Menu: เปิด/ปิด

### 3. FOV (Client only)
- ขยาย FOV slider จาก 30-110 → 15-160 องศา
- Mixin target: `OptionInstance` ที่ควบคุม FOV clamp
- Toggle ใน Mod Menu: เปิด/ปิด

### 4. Command Suggestions (Client only)
- แสดง command suggestions ครบทุกอัน (vanilla แสดงแค่ 10)
- Mixin target: `CommandSuggestor` — method ที่ limit จำนวน suggestions
- Toggle ใน Mod Menu: เปิด/ปิด

### 5. Stack Size (Client + Server — ต้องติดทั้งสองฝั่งใน multiplayer)
- ขยาย stack size สูงสุดจาก 64 → ปรับเองได้ไม่เกิน 4096
- Mixin target: `Item.getMaxStackSize()` และ `ItemStack`
- ถ้า server ไม่มี mod จะถูก clamp กลับเป็น 64 อัตโนมัติ
- Toggle ใน Mod Menu: เปิด/ปิด + slider หรือ input สำหรับค่า max

### 6. Reach Distance (Client + Server — ต้องติดทั้งสองฝั่งใน multiplayer)
- ขยาย reach distance จาก 4.5 (survival) / 5.0 (creative) → 10 บล็อก
- Client: Mixin target `GameRenderer` หรือ `MultiPlayerGameMode`
- Server: ใช้ Fabric API `ServerPlayNetworking` หรือ `EntityAttributeModifier` บน `BLOCK_INTERACTION_RANGE` และ `ENTITY_INTERACTION_RANGE`
- Toggle ใน Mod Menu: เปิด/ปิด

## Config System

ใช้ Cloth Config เก็บค่าต่อไปนี้:
```java
boolean enableRenderDistance = true;
int     maxRenderDistance    = 128;

boolean enableParticleLimit  = true;

boolean enableFov            = true;
// FOV ใช้ Minecraft slider เอง ไม่ต้องเก็บค่า

boolean enableCommandSuggestions = true;

boolean enableStackSize      = true;
int     maxStackSize         = 4096;

boolean enableReach          = true;
// reach ใช้ fixed 10 บล็อก
```

## Notes สำหรับ Claude

- ใช้ Mojang mappings เสมอ ไม่ใช้ Yarn
- Fabric Loom 1.11-SNAPSHOT ต้องการ Gradle 8.14+
- `gradle-wrapper.jar` ต้องเป็นเวอร์ชันที่ถูกต้อง (ผู้ใช้มีไฟล์นี้อยู่แล้ว)
- Mixin class names อาจต่างจาก 1.20.x ให้ระบุเสมอว่าไม่แน่ใจถ้าไม่รู้จริง
- ถ้า compile error ให้รอดู error log ก่อนแก้
- mod นี้ environment `*` มี entrypoint แยก main / client / server
