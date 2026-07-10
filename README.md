# Shelfy — 在庫管理システム

> 棚（Shelf）を起点に、買いすぎ・食品ロス・使い忘れをなくす在庫管理システム。

---

## 技術スタック（Linkleと同じ構成）

| 項目 | 内容 |
|---|---|
| バックエンド | Java 17 + Spring Boot 3.x |
| フロントエンド | Thymeleaf + HTML/CSS/JS |
| DB（Shelfy用） | Neon PostgreSQL（別プロジェクト） |
| DB（Linkle共用） | Linkle の Neon PostgreSQL（ユーザー・グループ・買い物リスト） |
| ホスティング | Render（Docker） |
| 画像ストレージ | Cloudflare R2 |

---

## 環境変数一覧

| 変数名 | 必須 | 説明 |
|---|---|---|
| `SHELFY_DATABASE_URL` | ✅ | Shelfy用NeonのURL（`postgresql://...`） |
| `LINKLE_DATABASE_URL` | ✅ | LinkleのNeonのURL（ユーザー認証・買い物リスト連携） |
| `R2_ACCOUNT_ID` | 写真機能使用時 | Cloudflare R2 アカウントID |
| `R2_ACCESS_KEY` | 写真機能使用時 | R2 アクセスキー |
| `R2_SECRET_KEY` | 写真機能使用時 | R2 シークレットキー |
| `R2_BUCKET_NAME` | 写真機能使用時 | R2 バケット名（デフォルト: `shelfy-images`） |
| `R2_PUBLIC_URL` | 写真機能使用時 | R2 公開URL（`https://...`） |
| `THYMELEAF_CACHE` | - | テンプレートキャッシュ（ローカルは`false`推奨） |
| `TZ` | 推奨 | `Asia/Tokyo`（当日判定ずれ防止） |

---

## ローカル起動

```bash
git clone https://github.com/あなたのリポジトリ/shelfy.git
cd shelfy
mvn spring-boot:run
```

- `SHELFY_DATABASE_URL` 未設定 → `data/shelfy.mv.db`（H2）で自動起動
- `LINKLE_DATABASE_URL` 未設定 → `data/linkle.mv.db`（H2）を使用
- ブラウザ: `http://localhost:8081`

### ローカル初期設定

```
http://localhost:8081/h2-console
JDBC URL: jdbc:h2:file:./data/linkle
ユーザー名: sa / パスワード: (空欄)
```

```sql
-- Linkle共用テーブルをローカルでも用意する場合
CREATE TABLE IF NOT EXISTS app_user (
  id       BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  approved BOOLEAN DEFAULT false NOT NULL,
  admin    BOOLEAN DEFAULT false NOT NULL,
  rejected BOOLEAN DEFAULT false NOT NULL,
  color    VARCHAR(20)
);

-- 管理者ユーザーを手動で作成
-- パスワードはBCryptで生成してから入れる
INSERT INTO app_user (username, password, approved, admin)
VALUES ('admin', '$2a$10$xxxx...', true, true);
```

---

## Renderへのデプロイ

1. GitHubにプッシュ
2. Render → New Web Service → リポジトリ選択
3. Runtime: **Docker**
4. 環境変数を上記の表に従って設定
5. Deploy

---

## Linkle連携の仕組み

```
Shelfy（Shelfy Neon DB）               Linkle（Linkle Neon DB）
─────────────────────────────          ─────────────────────────
shelfy_item.stock が 0 になる
        ↓
LinkleShoppingService が検知
        ↓
shopping_item テーブルに INSERT ────→  買い物リストに「商品名」が追加
source = 'shelfy' で識別可能

在庫を補充（stock > 0）
        ↓
Linkleの該当アイテムを checked=true ─→ 買い物リストで購入済みになる
```

---

## DBテーブル構成

### Shelfy Neon DB

```sql
shelfy_item         -- 商品マスタ（日用品・食品・調味料統合）
```

### Linkle Neon DB（読み取り + 書き込み）

```sql
app_user            -- 読み取り（認証）
group               -- 読み取り（グループ）
group_membership    -- 読み取り（所属確認）
shopping_item       -- 書き込み（在庫0連携）
```

---

## Phase別開発ロードマップ

| Phase | 内容 | 状態 |
|---|---|---|
| Phase 1 | ダッシュボード・日用品管理・食品期限管理・Linkle連携 | ✅ 実装済み |
| Phase 2 | 写真アップロード（Cloudflare R2）・プッシュ通知 | 🔜 |
| Phase 3 | バーコードスキャン・Finterra連携 | 🔜 |
