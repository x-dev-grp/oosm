# OOSM versioning

Semantic versioning for **backend** (`oosm`) and **frontend** (`osm-ms-fe`). Keep the same version number on both repos for each client release.

## Branches

| Branch | Purpose | Version |
|--------|---------|---------|
| `feature/*` | New features | No bump — CI builds `sha-*` Docker tags |
| `develop` | Integration / UAT | Optional pre-release (`0.3.0-dev.1`) |
| `main` | Production | Released semver (`v0.2.1` tag) |
| `release` | Hotfix line | Same as `main` |

## Commit messages (conventional)

Use prefixes so changelogs and release notes stay readable:

```
feat(equipment): add mill equipment registry
fix(finance): correct oil sale payment total
chore(ci): update Railway deploy
docs: versioning guide
```

| Prefix | Semver bump on release |
|--------|------------------------|
| `feat:` | **minor** (or patch if you choose patch-only releases) |
| `fix:` | **patch** |
| `feat!:` or `BREAKING CHANGE:` | **major** |

## Single source of truth

Each repo has a root **`VERSION`** file (e.g. `0.2.1`).

- **Backend:** syncs Maven POMs + `app/src/main/resources/version.properties`
- **Frontend:** syncs `package.json` (used by `environment.prod.ts` as `appVersion`)

Check current version:

```bash
node scripts/version/bump-version.cjs
cat VERSION
```

## Bump locally

**Backend** (`oosm/`):

```bash
node scripts/version/bump-version.cjs patch   # 0.2.1 → 0.2.2
node scripts/version/bump-version.cjs minor   # 0.2.1 → 0.3.0
node scripts/version/bump-version.cjs major   # 0.2.1 → 1.0.0
node scripts/version/bump-version.cjs set 0.2.1
```

**Frontend** (`osm-ms-fe/`): same commands.

Then commit, tag **both repos** with the same tag:

```bash
git tag v0.2.2
git push origin v0.2.2
```

## Release via GitHub Actions

Workflow: **Release** (`.github/workflows/release.yml`)

1. Open Actions → **Release** → Run workflow
2. Choose bump: `patch` | `minor` | `major`
3. Workflow bumps `VERSION`, updates changelog, commits, tags `v*`, creates GitHub Release
4. **Docker Publish** runs on tag → images tagged `v0.2.2` and `sha-*`

Run on **`main`** after merging features from `develop`.

## Docker tags

| Trigger | Tags |
|---------|------|
| Push `main` / `release` | `release-latest`, `sha-<commit>` |
| Push tag `v1.2.3` | `v1.2.3`, `1.2.3`, `sha-<commit>` |

## Runtime version

- **API:** `GET /actuator/info` → `app.version`, `app.git-sha`
- **UI:** `environment.appVersion` from `package.json`

## Feature workflow (example)

```bash
git checkout -b feature/equipment-missions develop
# ... commits: feat(equipment): ...
git push -u origin feature/equipment-missions
# PR → develop → CI only

git checkout develop && git merge feature/equipment-missions
git push origin develop
# Railway/testing deploy from develop (optional)

git checkout main && git merge develop
git push origin main
# Run Release workflow → v0.3.0
```

## Align backend + frontend

For each production release use the **same version** and **same tag** on both GitHub repos:

| Release | Backend tag | Frontend tag |
|---------|-------------|--------------|
| Equipment MVP | `v0.3.0` in `oosm` | `v0.3.0` in `osm-ms-fe` |

Deploy matching tags together on Railway/Render/VPS.
