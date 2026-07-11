# Shelfy — 在庫管理システム

> 棚（Shelf）を起点に、買いすぎ・食品ロス・使い忘れをなくす在庫管理システム。

---

## 📱 本番URL

```
https://shelfy-1fi2.onrender.com
```

---

## 🛠 技術スタック

| 項目 | 内容 |
|---|---|
| バックエンド | Java 17 + Spring Boot 3.x |
| フロントエンド | Thymeleaf + HTML/CSS/JS |
| DB（Shelfy用） | Neon PostgreSQL（独立プロジェクト） |
| DB（Linkle共用） | Linkle の Neon PostgreSQL |
| ホスティング | Render（Docker） |
| 画像ストレージ | Cloudflare R2 |
| 認証 | Spring Security（Linkleアカウント共用） |
| プッシュ通知 | Web Push（VAPID） |

---

## ✅ 実装済み機能

### 基本機能
| 機能 | 説明 |
|---|---|
| ログイン・新規登録 | Linkleアカウントを共用。セッション30日維持 |
| グループ選択 | Linkleのグループを切り替えて共有管理 |
| ダッシュボード | 総アイテム数・期限状況・在庫切れをひと目で確認 |

### 在庫管理
| 機能 | 説明 |
|---|---|
| 日用品管理 | 在庫数・補充目安・±ボタンでクイック更新 |
| 食品・調味料管理 | 賞味/消費期限管理・期限カラー表示（期限順ソート） |
| アイテム詳細 | 写真・期限・在庫・保管場所・開封日・最終更新者 |
| アイテム追加・編集 | カテゴリ・在庫数・補充目安・期限・保管場所・開封日 |
| 写真アップロード | Cloudflare R2に保存。ライブラリ/カメラから選択 |
| 写真一覧 | カテゴリ別フィルター付きグリッド表示 |

### 連携機能
| 機能 | 説明 |
|---|---|
| Linkle買い物リスト連携 | 在庫が0になると自動でLinkleの買い物リストに追加 |
| 買い物リスト画面 | Shelfy内でLinkleの買い物リストを確認・購入済みチェック |

### アラート・通知
| 機能 | 説明 |
|---|---|
| アラート一覧 | 期限切れ・7日以内・在庫切れをまとめて表示 |
| プッシュ通知 | 毎朝8時に期限切れ・7日以内・在庫切れを通知 |

### 記録・分析
| 機能 | 説明 |
|---|---|
| 消費ログ | 「使い切った」ボタンで消費履歴を記録 |
| 月次レポート | 今月の消費数・期限切れ数・カテゴリ別グラフ |
| スコア・バッジ | 使い切り回数・期限切れゼロ継続日数でスコア獲得 |

### UI・PWA
| 機能 | 説明 |
|---|---|
| PWA対応 | ホーム画面に追加可能・スプラッシュ画面あり |
| アプリアイコン | カスタムアイコン設定済み |
| ボトムナビ | 日用品・食品・ホーム・写真一覧・追加 |
| ハンバーガーメニュー | スコア・グループ・買い物リスト・消費ログ・レポート・ログアウト |

---

## 🔗 Linkle連携の仕組み

```
Shelfy（Shelfy Neon DB）          Linkle（Linkle Neon DB）
────────────────────────          ──────────────────────
在庫が0になる
        ↓
LinkleShoppingService が検知
        ↓
shopping_item に INSERT ────────→ 買い物リストに追加
        ↓
在庫を補充（stock > 0）
        ↓
purchased = true に UPDATE ─────→ 買い物リストで購入済みに
```

---

## 🗄 DBテーブル構成

### Shelfy Neon DB

| テーブル | 説明 |
|---|---|
| `shelfy_item` | 商品マスタ（日用品・食品・調味料統合） |
| `shelfy_push_subscription` | プッシュ通知サブスクリプション |
| `shelfy_consumption_log` | 消費履歴ログ |
| `shelfy_score` | グループのスコア情報 |
| `shelfy_badge` | 獲得済みバッジ |

### Linkle Neon DB（読み取り＋書き込み）

| テーブル | 操作 | 用途 |
|---|---|---|
| `app_user` | 読み取り | 認証 |
| `app_group` | 読み取り | グループ名表示 |
| `group_membership` | 読み取り | グループ所属確認 |
| `shopping_item` | 書き込み | 在庫0連携 |

---

## 🌐 画面一覧

| URL | 画面名 |
|---|---|
| `/dashboard` | ダッシュボード |
| `/daily` | 日用品一覧 |
| `/food` | 食品・調味料一覧 |
| `/items/new` | アイテム追加 |
| `/items/{id}` | アイテム詳細 |
| `/items/{id}/edit` | アイテム編集 |
| `/photos` | 写真一覧 |
| `/alerts` | アラート一覧 |
| `/shopping` | 買い物リスト |
| `/consumption` | 消費ログ |
| `/report` | 月次レポート |
| `/score` | スコア・バッジ |
| `/groups` | グループ選択 |

---

## ⚙️ 環境変数一覧

| 変数名 | 必須 | 説明 |
|---|---|---|
| `SHELFY_DATABASE_URL` | ✅ | Shelfy用NeonのURL |
| `LINKLE_DATABASE_URL` | ✅ | LinkleのNeonのURL |
| `R2_ACCOUNT_ID` | ✅ | Cloudflare R2 アカウントID |
| `R2_ACCESS_KEY` | ✅ | R2 アクセスキー |
| `R2_SECRET_KEY` | ✅ | R2 シークレットキー |
| `R2_BUCKET_NAME` | ✅ | R2 バケット名（shelfy-images） |
| `R2_PUBLIC_URL` | ✅ | R2 公開URL |
| `VAPID_PUBLIC_KEY` | ✅ | プッシュ通知用VAPIDキー（Linkleと共用） |
| `VAPID_PRIVATE_KEY` | ✅ | プッシュ通知用VAPIDキー（Linkleと共用） |
| `VAPID_SUBJECT` | ✅ | VAPIDサブジェクト |
| `TZ` | 推奨 | Asia/Tokyo |

---

## 💻 ローカル起動

```bash
git clone https://github.com/あなたのユーザー名/shelfy.git
cd shelfy
mvn spring-boot:run
```

- `SHELFY_DATABASE_URL` 未設定 → H2ローカルDBで自動起動
- ブラウザ: `http://localhost:8082`

---

## 🚀 開発フロー（Renderへの自動デプロイ）

```bash
cd ~/shelfy/shelfy

# コードを修正したら以下の3行でデプロイ完了
git add -A
git commit -m "修正内容をここに書く"
git push origin main
```

pushするとRenderが自動でビルド＆デプロイします。

---

## 📊 スコア・バッジ一覧

| バッジ | 条件 |
|---|---|
| 🌱 使い切り10回達成 | 合計10個使い切る |
| ⭐ 使い切り50回達成 | 合計50個使い切る |
| 🏆 使い切り100回達成 | 合計100個使い切る |
| 🥗 期限切れゼロ7日継続 | 7日間期限切れゼロを維持 |
| 🎖️ 期限切れゼロ30日継続 | 30日間期限切れゼロを維持 |
| 💫 スコア100点突破 | スコアが100ptを超える |
| 🔥 スコア500点突破 | スコアが500ptを超える |
| 👑 スコア1000点突破 | スコアが1000ptを超える |

---

## 🔗 関連システム

| システム | URL | 説明 |
|---|---|---|
| Linkle | https://schedule-app-827q.onrender.com | スケジュール×買い物リスト共有アプリ |
| Finterra | - | 家計・資産管理システム（将来的に連携予定） |

---

*最終更新：2026年7月*
