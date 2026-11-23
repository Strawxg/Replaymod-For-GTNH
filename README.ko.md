# ReplayMod 1.7.10 · GT New Horizons 호환 패치

[简体中文](README.md) | [English](README.en.md) | [日本語](README.ja.md) | **한국어**

> **ReplayMod 1.7.10(2.5.2)** 을 **GTNH 2.9.0 Stable 정식판(Java 17–26 / lwjgl3ify)** 에서 정상 동작시킵니다:
> 녹화, 리플레이, 영상 내보내기.
> ReplayMod(MIT, 원작자 **CrushedPixel** / **johni0702**) 기반입니다.

**현재 버전: Release 1.0** · 대응 jar: `Replaymod-2.5.2-GTNH-release1.0.jar`

> ⚠️ **면책 조항(필독)**: 이 패치 레이어의 구현은 **주로 AI 코딩 에이전트(DeepSeek V4.1 Pro / Flash)에 의해 이루어졌습니다**. 관리자는 전문 프로그래머가 아니며 코드를 완전히 이해하지 못합니다. 따라서 **알 수 없는 결함, 호환성 문제 또는 데이터 위험이 존재할 수 있습니다**. **자기 책임**으로 사용하십시오. 피드백은 환영하지만 수정을 보장하지는 않습니다.

---

## 이것은 무엇인가

이 저장소는 **패치 레이어(patch layer)** 이며, ReplayMod 소스를 재컴파일하지 않습니다. 대신 런타임에 Mixin/ASM을 사용해 공식
`replaymod-1.7.10-2.5.2.jar` 에 패치를 주입하고, GTNH에 필요한 런타임 의존성(UniMixins / GTNHLib / LWJGL3ify)을 추가합니다.

- **ReplayMod / Forge / Minecraft 소스 코드를 일절 포함하지 않습니다**([`NOTICE.md`](NOTICE.md) 참조).
- 산출물은 공식 jar를 **완전히 대체하는** 패치 jar입니다(원본 전체 클래스 + 패치 레이어 포함):
  **원본 ReplayMod를 따로 넣을 필요가 없습니다**.

## 설치

1. `Replaymod-2.5.2-GTNH-release1.0.jar` 를 다운로드하여 GTNH 인스턴스의 `mods/` 에 넣습니다.
2. `mods/` 에 다음 **런타임 의존성**이 있는지 확인합니다(모두 필수):

   | 의존성 | 최소 버전 | 설명 |
   |---|---|---|
   | UniMixins | 0.1.7 | Mixin 0.8.7 런타임 |
   | GTNHLib | 0.6.6 | GTNH 공용 라이브러리 |
   | LWJGL3ify | 1.5.7 (+ forgePatches) | Java 17+ 용 LWJGL3 호환 레이어 |

3. **원본** `replaymod-1.7.10-2.5.2.jar` 를 **반드시 제거**하십시오: modid가 동일(`replaymod`)하므로 둘 다 넣으면 중복 로딩 충돌이 발생합니다.
4. 게임을 실행하면 메인 메뉴 오른쪽에 "回放中心" 항목이 나타납니다. ReplayMod 키 바인딩(기본)으로 녹화 시작/중지를 할 수 있습니다.

## 영상 내보내기에는 ffmpeg 필요

ReplayMod의 영상 내보내기는 **ffmpeg** 에 의존합니다. Windows에서는 다음 순서로 찾습니다:

1. `<GAMEDIR>\ffmpeg\bin\ffmpeg.exe` (**권장**)
2. 없으면 **게임 디렉터리 전체**를 재귀 검색하여 아무 `ffmpeg.exe` 나 찾음
3. 둘 다 없으면 → 내보낼 때 "要渲染视频需要安装 ffmpeg"(ffmpeg 설치 필요) 표시

**설치 방법(둘 중 하나)**:

- **권장**: 다운로드한 Windows용 `ffmpeg.exe` 를
  `<GAMEDIR>\ffmpeg\bin\ffmpeg.exe` 에 둡니다
  (`<GAMEDIR>` 는 해당 인스턴스의 `.minecraft` / 버전 디렉터리).
- 또는: `ffmpeg.exe` 를 게임 디렉터리 어디든 둡니다.

**다운로드**: <https://ffmpeg.org/download.html> 또는 <https://www.gyan.dev/ffmpeg/builds/>
(`libx264` 가 포함된 빌드 필요; essentials / full 모두 가능).
이 패치는 **ffmpeg 9.1 full_build** 에서 내보내기를 확인했습니다.

> 기본 내보내기 명령:
> `-y -f rawvideo -pix_fmt bgra -s <W>x<H> -r <FPS> -i - -an -c:v libx264 -b:v <비트레이트> -pix_fmt yuv420p "<out.mp4>"`
> 이 명령은 ffmpeg 7/8/9 에서 동작합니다.

## 알려진 제한(Release 1.0 허용)

- **미등록 / 동적 차원으로의 이동을 리플레이하면 크래시**: 예를 들어 PersonalSpace의 개인 차원(차원 180).
  녹화에 "해당 차원으로 전환" 패킷이 포함되어 있으면 클라이언트 `DimensionManager` 가 그 차원을 찾지 못해 Forge가 예외를 던집니다.
  *회피*: **녹화했던 것과 같은 환경**에서 리플레이하거나, **차원을 바꾸지 않은** 녹화만 리플레이하십시오.
- **월드 로딩이 끝난 후 녹화를 시작하면 지형 / 플레이어 항목이 빠질 수 있음**: ReplayMod는 청크/플레이어가 처음 도착할 때만 기록합니다.
  *권장*: ReplayMod의 **AUTO_START_RECORDING(자동 녹화 시작)** 을 켜서 월드에 들어가자마자 녹화하십시오.
- **리플레이 중 "플레이어가 사라짐"은 보통 카메라 문제**(1인칭 또는 관전 중인 플레이어에 카메라가 붙어 있음)이며 엔티티 손실이 아닙니다. 자유 카메라로 확인하십시오.
- 더 완전한 알려진 문제는 [`docs/KNOWN_ISSUES.md`](docs/KNOWN_ISSUES.md) 참조.

## 빌드(선택, 소스에서 jar 재현)

자세한 내용은 [`docs/BUILDING.md`](docs/BUILDING.md). 필요:

- **JDK 8**(패치 레이어 컴파일), **JDK 25 + Gradle 9.4.1**(RFG로 디컴파일 베이스 `recompiled_minecraft-1.7.10.jar` 생성)
- 컴파일 의존성(**저장소에 포함되지 않음**): 공식 `replaymod-1.7.10-2.5.2.jar`, Forge 1.7.10 dev jar, launchwrapper, netty, gson, guava, asm, AE2 등
- 2단계: `tools/build.ps1` 로 패치 레이어 컴파일 → `tools/apply.ps1` 로 공식 jar에 주입

## 라이선스 및 크레딧

- 이 패치 레이어는 **MIT** 로 배포됩니다([`LICENSE`](LICENSE) 참조).
- **ReplayMod**(MIT, 원작자 **CrushedPixel**, **johni0702**) 기반입니다. 패치 jar에는 원본 `LICENSE.txt` 가 유지됩니다.
- 서드파티 고지는 [`NOTICE.md`](NOTICE.md), 업스트림과의 정확한 관계(대상 jar의 SHA-256 포함)는 [`UPSTREAM.md`](UPSTREAM.md) 참조.
- 아이디어 출처: 이 패치 레이어 방식은 [`replaymod-gtnh-compat`](https://github.com/Luoxueruoling/replaymod-gtnh-compat) 프로젝트의 탐구에서 영감을 받았습니다. 감사합니다.
