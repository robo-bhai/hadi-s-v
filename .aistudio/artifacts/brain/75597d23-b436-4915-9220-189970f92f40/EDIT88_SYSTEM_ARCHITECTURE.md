# Edit88: Video Editing Application Architecture & Technical Specification

## 1. Executive Summary & Product Vision
**Edit88** (`com.aistudio.edit88.pro` / `com.edit88.app`) is an end-to-end, high-performance video editing suite offering a modern workflow comparable to **CapCut Pro**, uniquely augmented with **Urdu & Multi-language typography** (native Nastaliq RTL) and an **API-driven Google Gemini AI creative pipeline**.

---

## 2. System Architecture & Cross-Platform Tech Stack Recommendations

### Recommended Stack Matrix

| Target Platform | UI & Frontend Framework | Video Processing & Audio Engine | AI & Speech Layer | State & Persistence |
| :--- | :--- | :--- | :--- | :--- |
| **Android (Active Production Implementation)** | **Jetpack Compose (M3)** + Kotlin Coroutines & Flow | Android Media3 / Transformer + Hardware Canvas Shaders | Google Gemini REST (`gemini-3.5-flash`) + Android TTS | Local Room DB + Jetpack DataStore / SharedPreferences |
| **Desktop (Cross-Platform Windows/macOS/Linux)** | **Tauri (Rust + React 19 / Vite)** or **Electron** | Native C++ FFmpeg shared library bindings (`ffmpeg-next` in Rust) | Direct Gemini API REST / gRPC client | SQLite with SQLx / LevelDB |
| **Web Browser (PWA / Cloud Editor)** | **Next.js 15 (App Router) + Tailwind CSS** | `@ffmpeg/ffmpeg` (WebAssembly with multi-threading) + WebGL Canvas | Server-side Gemini API route handlers | IndexedDB + Web Workers |
| **Cloud Video Rendering Backend** | **FastAPI (Python 3.12) / Node.js (NestJS)** | Headless FFmpeg with NVENC (NVIDIA GPU acceleration) | Gemini 3.5 Flash + Whisper / Google Cloud Speech-to-Text | Redis Queue (Celery/BullMQ) + S3 Media Storage |

---

## 3. UI/UX Layout Scheme (CapCut Pro Inspired)

```
+----------------------------------------------------------------------------------+
|  Top Navigation Bar: [Back/Home]  [Title: My Edit88 Reel]  [Aspect: 9:16]  [Undo] [Redo]  [EXPORT]  |
+----------------------------------------------------------------------------------+
|                                                                                  |
|                        LIVE VIDEO CANVAS PREVIEW                                 |
|               - Aspect Ratio Mask (9:16, 16:9, 1:1, 4:5)                         |
|               - Dynamic Scene Graphics & LUT Shaders                             |
|               - Real-time Urdu Nastaliq Subtitles (Karaoke Word Glow)            |
|               - Interactive PiP / Stickers Overlays                              |
|                                                                                  |
+----------------------------------------------------------------------------------+
|  Playback & Timecode Bar: [00:03.45 / 00:15.00]    [◀◀]  [▶ Play/Pause]  [▶▶]    |
+----------------------------------------------------------------------------------+
|  Quick Clip Ribbon:  [✂ Split]  [⚡ Speed]  [📋 Duplicate]  [🎨 Color]  [🗑 Delete] |
+----------------------------------------------------------------------------------+
|  MULTI-TRACK TIMELINE:                                                           |
|  [Ruler]  | 0s      | 2s      | 4s      | 6s      | 8s      | 10s     | 12s   |
|  [Video]  [ Clip 1 (Neon City) ] [ Clip 2 (Sunrise) ] [ Clip 3 (Glitch) ]        |
|  [PiP]    [    🔥 Fire Sticker    ]                                              |
|  [Urdu]   [ خواب دیکھنا آسان ہے ] [ مگر انہیں پورا کرنا اصل ہمت ہے ]             |
|  [Audio]  [ ♬ Cinematic Beat (85% vol, AI De-Noise) ~~~~~~~~~~~~~~~~~~~ ]        |
|  (Red Playhead Line with pin head scrubbable across entire duration)             |
+----------------------------------------------------------------------------------+
|  Pro Toolbar:  [🎬 Clips]  [🎵 Audio]  [✍ Urdu Text]  [✨ AI Studio]  [🎨 Filters]  [⚙ Settings] |
+----------------------------------------------------------------------------------+
```

---

## 4. Built-in Auto Subtitles & Urdu Nastaliq Typography

### Typography Features
1. **Bundled Static Fonts**:
   - `Noto Nastaliq Urdu` (`app/src/main/res/font/noto_nastaliq_urdu.ttf`)
   - `Gulzar Nastaliq` (`app/src/main/res/font/gulzar.ttf`)
   - Modern System Sans for bilingual display.
2. **Native RTL Layout**:
   - Explicit `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`.
3. **Word-by-Word Karaoke Highlighting**:
   - Subtitle clips contain `List<TimedWord>`. As the playhead progresses, the active word glows with scale pulse (`1.14x`), vibrant color shift (`#FACC15`), and blurred drop shadow.
4. **Style Presets**:
   - `Karaoke`: Word-by-word active glow.
   - `Neon Glow`: Cyberpunk cyan-magenta borders with high-blur luminous glow.
   - `Boxed Sub`: High-contrast dark pill backdrop.
   - `Retro Stroke`: Bold drop shadow with thick typography contour.
   - `Minimal`: Elegant clean cinematic text.

---

## 5. User-Provided Gemini AI API & Security Architecture

1. **Persistent Local Storage**:
   - Stored in private app storage (`SettingsRepository`), masked in UI with show/hide toggle.
2. **One-Tap Verification ("Test Connection")**:
   - Direct ping to `gemini-3.5-flash` endpoint with latency reporting.
3. **Graceful Fallback**:
   - If offline or key is unconfigured, Edit88 automatically supplies rich, curated Urdu Nastaliq subtitles and scene clips, guaranteeing an uninterrupted editing flow.
4. **Supported AI Features**:
   - **Speech-to-Text / Auto-Captions**: Transcribes audio context into synchronized Urdu/English subtitle blocks.
   - **Script-to-Video**: Directs a multi-scene structure complete with visual themes, Urdu voiceover, and duration.
   - **AI Color Grading Assistant**: Translates natural language mood prompts (e.g., *"Moody cyberpunk neon teal and magenta"*) into matrix adjustments (brightness, contrast, saturation, temperature, vignette).
   - **Viral Clip Highlight Detector**: Identifies retention hooks and viral clips with engagement scoring.

---

## 6. FFmpeg / Canvas Export Pipeline

When exporting to MP4, MOV, GIF, or MP3:
```bash
# Complex filter combining video speed, LUT color grading, audio mixing, and burning Nastaliq subtitles:
ffmpeg -y \
  -i input_video.mp4 \
  -i background_music.mp3 \
  -filter_complex "\
    [0:v]setpts=1.0*PTS,\
    eq=brightness=0.04:contrast=1.25:saturation=1.20,\
    ass=subtitles_urdu_nastaliq.ass[v_out];\
    [1:a]volume=0.85[a_out]\" \
  -map \"[v_out]\" -map \"[a_out]\" -c:v libx264 -pix_fmt yuv420p -c:a aac output.mp4
```

---

## 7. Interactive Subtitle Layout & Video Timeline Features (Latest Enhancements)

1. **Strict Horizontal Single-Line Auto-Layout (Movie Subtitle Aesthetics)**:
   - All subtitle templates (`Karaoke`, `Neon Glow`, `Boxed`, `Retro Stroke`, `Minimal`) are forced to a single horizontal row (`maxLines = 1`, `softWrap = false`) with `wrapContentWidth()`.
   - Dynamic 3-4 word chunks ensure rapid readability and audio synchronization without vertical text wrapping.
   - Defaults to the standard lower-third safe zone (`posY = 0.82f`) with proportionate movie-style sizing (`18sp` scaled).

2. **4-Corner Interactive Resizing Bounding Box**:
   - Corner handles on Top-Left, Top-Right, Bottom-Left, and Bottom-Right with diagonal scale drag physics.
   - Center drag freely repositions subtitles across the entire canvas area.
   - Rotation stem handle (`↺`) with live angle readout.
   - Live scale readout badge (`100%`, `125%`, etc.) and top floating action bar (`Edit Inspector` and `Delete`).

3. **Multi-Touch Timeline Zoom & Precision Editing**:
   - Two-finger pinch-to-zoom gesture on the multi-track timeline and time ruler.
   - Expand & compress UI toolbar with `[-]` / `[+]` buttons, live zoom chip (`1.0x`), and an expandable slider ranging from `0.25x` (full video overview) to `5.0x` (frame-by-frame precision).
   - Instant presets: `Fit` (0.5x), `1x` (1.0x), `Frame` (3.0x).

4. **Dedicated Font Store & Asset Library Window**:
   - Browse, search, and preview pre-packaged and downloadable typography styles.
   - "Download Font" integration for instant in-app fetching.
   - "Add / Upload Custom Font" supporting `.ttf` and `.otf` local font files.

5. **Core Video Editing Tools & Real-Time Performance**:
   - 0.1x to 10.0x playback speed adjustment with pitch preservation.
   - Comprehensive color grading: Brightness, Contrast, Saturation, Temperature, Tint, Highlights, Shadows, and LUT Presets.
   - Hardware-accelerated real-time Compose Canvas preview.
## 8. Real-Time Hardware Video Export Engine & Typography Truncation Fixes

1. **Hardware-Accelerated Composited Video Export (`VideoExportEngine.kt`)**:
   - Built a comprehensive Android hardware video compositor utilizing native `MediaCodec` (H.264 / `video/avc`), `EGLRenderer` / OpenGL ES 2.0 (`createInputSurface()`), and `MediaMuxer`.
   - **Video Frame Compositing**: Seamlessly extracts and scales source video frames via `MediaMetadataRetriever` (or generates procedural background scenes).
   - **Color Grading & Filters**: Burns in real-time filter presets (Cyberpunk, Cinematic, Vintage, B&W, Golden Sunset, Arctic Blue), temperature shifts, tints, brightness/exposure adjustments, and radial vignettes into each frame.
   - **Burned-In Subtitles & Karaoke**: Renders Urdu Nastaliq typography with frame-accurate word glow, outlines, and rounded pill containers.
   - **Audio Muxing**: Preserves source video audio tracks and mixes external background tracks using `MediaExtractor` and `MediaMuxer`.
   - **Direct MediaStore Storage**: Saves encoded MP4 directly into the device gallery under `Movies/Edit88`.

2. **Urdu Nastaliq Truncation & Clipping Elimination**:
   - **Optimal Font Sizing**: Reduced default subtitle font size to `15sp` (movie subtitle aesthetic) preventing canvas overflow.
   - **Bidi Word Spacing**: Replaced trailing ASCII whitespace concatenation with Compose layout `Arrangement.spacedBy(8.dp)`, eliminating RTL reordering anomalies.
   - **Zero Clipping**: Applied `TextOverflow.Visible` across all subtitle styles.
   - **Generous Safety Padding**: Expanded container horizontal padding to `22.dp`, preventing rounded corners from intersecting with text ascenders, descenders, or swashes.
