# ReplayMod 1.7.10 · GT New Horizons 互換パッチ

[简体中文](README.md) | [English](README.en.md) | **日本語** | [한국어](README.ko.md)

> **ReplayMod 1.7.10（2.5.2）** を **GTNH 2.9.0 Stable 正式版（Java 17–26 / lwjgl3ify）** で正常動作させます：
> 録画、リプレイ、動画の書き出し。
> ReplayMod（MIT、原作者 **CrushedPixel** / **johni0702**）をベースにしています。

**現在のバージョン：Release 1.1** · 対応 jar：`Replaymod-2.5.2-GTNH-release1.1.jar`

> ⚠️ **免責事項（必読）**：このパッチレイヤーの実装は**主に AI コーディングエージェント（DeepSeek V4.1 Pro / Flash）によって行われました**。メンテナーはプロのプログラマーではなく、コードを完全に理解しているわけではありません。そのため、**未知の不具合・互換性問題・データリスクが存在する可能性があります**。**自己責任**でご利用ください。フィードバックは歓迎しますが、修正を保証するものではありません。

---

## これは何か

このリポジトリは**パッチレイヤー（patch layer）**であり、ReplayMod のソースを再コンパイルしません。代わりに、実行時に Mixin/ASM を使って公式の
`replaymod-1.7.10-2.5.2.jar` にパッチを注入し、GTNH に必要なランタイム依存（UniMixins / GTNHLib / LWJGL3ify）を追加します。

- **ReplayMod / Forge / Minecraft のソースコードは一切含みません**（[`NOTICE.md`](NOTICE.md) を参照）。
- 成果物は公式 jar を**完全に置き換える**パッチ jar です（元の全クラス + パッチレイヤーを含む）：
  **元の ReplayMod を別途入れる必要はありません**。

## インストール

1. `Replaymod-2.5.2-GTNH-release1.1.jar` をダウンロードし、GTNH インスタンスの `mods/` に入れます。
2. `mods/` に以下の**ランタイム依存**が入っていることを確認します（すべて必須）：

   | 依存 | 最低バージョン | 説明 |
   |---|---|---|
   | UniMixins | 0.1.7 | Mixin 0.8.7 ランタイム |
   | GTNHLib | 0.6.6 | GTNH 共通ライブラリ |
   | LWJGL3ify | 1.5.7（+ forgePatches） | Java 17+ 用 LWJGL3 互換レイヤー |

3. **元の** `replaymod-1.7.10-2.5.2.jar` を**必ず削除**してください：modid が同じ（`replaymod`）ため、両方入れると重複読み込みの競合が起きます。
4. ゲームを起動すると、メインメニュー右側に「回放中心」エントリが表示されます。ReplayMod のキー設定（デフォルト）で録画の開始/停止ができます。

## 動画の書き出しには ffmpeg が必要

ReplayMod の動画書き出しは **ffmpeg** に依存します。Windows では次の順で探します：

1. `<GAMEDIR>\ffmpeg\bin\ffmpeg.exe`（**推奨**）
2. 見つからなければ、**ゲームディレクトリ全体**を再帰的に検索して任意の `ffmpeg.exe` を探す
3. どちらも見つからない → 書き出し時に「要渲染视频需要安装 ffmpeg」（ffmpeg のインストールが必要）と表示

**インストール方法（どちらか）**：

- **推奨**：ダウンロードした Windows 版 `ffmpeg.exe` を
  `<GAMEDIR>\ffmpeg\bin\ffmpeg.exe` に置く
  （`<GAMEDIR>` はそのインスタンスの `.minecraft` / バージョンディレクトリ）。
- または：`ffmpeg.exe` をゲームディレクトリの任意の場所に置く。

**ダウンロード**：<https://ffmpeg.org/download.html> または <https://www.gyan.dev/ffmpeg/builds/>
（`libx264` を含むビルドが必要。essentials / full のどちらでも可）。
このパッチは **ffmpeg 9.1 full_build** で書き出しを確認済みです。

> デフォルトの書き出しコマンド：
> `-y -f rawvideo -pix_fmt bgra -s <W>x<H> -r <FPS> -i - -an -c:v libx264 -b:v <ビットレート> -pix_fmt yuv420p "<out.mp4>"`
> このコマンドは ffmpeg 7/8/9 で動作します。

## 既知の制限（Release 1.1 で許容）

- **未登録ディメンションへの移動はスキップされる（1.1 でクラッシュ修正）**：例として PersonalSpace の個人ディメンション（ディメンション 180）。
  以前は `IllegalArgumentException: Could not get provider type ...` で落ちていましたが、現在は**現在のワールドを保持**し、ログに 1 行
  `[replaycompat] skipped respawn to unregistered dimension N` を出すだけになります。代償として、その区間のディメンション切り替えは行われません（前のディメンションのまま再生されます）。
  判定は `DimensionManager.isDimensionRegistered` のみで、ディメンション名は一切固定していません。Galacticraft など通常登録されるディメンションの挙動は変わりません。
- **他の MOD の画面がリプレイのタイムラインに重なる**：ギャラクシーマップ、GT の機械 GUI、NEI などを開くと、それらがタイムラインの上に描画され、タイムラインが見えない・操作できない。GC の HUD 警告（「酸素装置の設定が無効」など）も同様。これは描画順の問題で、**1.1 では未修正**です。
- **ワールド読み込み完了後に録画を開始すると、地形 / プレイヤーエントリが欠けることがある**：ReplayMod はチャンク/プレイヤーが最初に到着したときにしか記録しません。
  *推奨*：ReplayMod の **AUTO_START_RECORDING（自動録画開始）** を有効にし、ワールドに入った時点から録画する。
- **リプレイ中の「プレイヤーが消える」は通常カメラの問題**（一人称または観戦中のプレイヤーにカメラが付いている）であり、エンティティの消失ではありません。フリーカメラで確認してください。
- より完全な既知の問題は [`docs/KNOWN_ISSUES.md`](docs/KNOWN_ISSUES.md) を参照。

## ビルド（任意、ソースから jar を再現）

詳細は [`docs/BUILDING.md`](docs/BUILDING.md)。必要：

- **JDK 8**（パッチレイヤーのコンパイル）、**JDK 25 + Gradle 9.4.1**（RFG で逆コンパイルベース `recompiled_minecraft-1.7.10.jar` を生成）
- コンパイル依存（**リポジトリには含まれません**）：公式 `replaymod-1.7.10-2.5.2.jar`、Forge 1.7.10 dev jar、launchwrapper、netty、gson、guava、asm、AE2 など
- 2 ステップ：`tools/build.ps1` でパッチレイヤーをコンパイル → `tools/apply.ps1` で公式 jar に注入

## ライセンスとクレジット

- このパッチレイヤーは **MIT** で公開されています（[`LICENSE`](LICENSE) を参照）。
- **ReplayMod**（MIT、原作者 **CrushedPixel**、**johni0702**）をベースにしています。パッチ jar には元の `LICENSE.txt` が保持されています。
- サードパーティの通知は [`NOTICE.md`](NOTICE.md)、アップストリームとの正確な関係（対象 jar の SHA-256 を含む）は [`UPSTREAM.md`](UPSTREAM.md) を参照。
- アイデアの出典：このパッチレイヤー方式は [`replaymod-gtnh-compat`](https://github.com/Luoxueruoling/replaymod-gtnh-compat) プロジェクトの試みに触発されました。感謝します。
