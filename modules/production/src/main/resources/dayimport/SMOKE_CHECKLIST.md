# Day Excel import — smoke checklist

Manual checks after deploying `feature/excel-day-import` (BE + FE).

## Template & dry-run

1. Open `/reception/import`.
2. Download **template** and **sample**.
3. Confirm sheets: Guide, ImportMeta, Regions, Parcels, SupplierTypes, QcRules, Suppliers, OilContainers, StorageUnits, Receptions, QcResults, Payments, OilSales, OilSaleContainers, Expenses.
4. Confirm Receptions headers include `oliveOilType` (OC|OB) and `varietyName`, plus dropdowns (deliveryType, operationType, supplierKey, regionName, storageUnitKey).
5. Upload sample → **Dry-run**:
   - Missing `oliveOilType` → ERROR (required for lot number).
   - Missing/unknown `storageUnitKey` on oil rows → ERROR (expected until tanks exist in DB / StorageUnits sheet matches).
   - Report shows CREATE / LINK_EXISTING / ERROR rows.
   - Download report as XLSX and CSV.
6. Fix workbook until `canCommit=true`.

## Create-if-missing / no overwrite

1. Existing parcel/region/supplier/container/QC rule in DB → dry-run `LINK_EXISTING` (fields not changed on commit).
2. New names only in sheet → `CREATE` and appear in DB after commit.
3. Re-run same file → receptions/sales/expenses/QC → `SKIP_DUPLICATE` (no double stock / FT).

## Stock

1. Priced OIL reception with valid tank → after commit, tank `currentVolume` increases (`RECEPTION_IN`).
2. Re-import → no second stock increase.
3. Oil sale with qty + tank → tank decreases after import approve path.
4. OilSaleContainers → `OilContainer.stockQuantity` decreases once.
5. Dry-run with sale qty > tank volume → ERROR (commit blocked).

## Payments & expenses

1. Payments sheet settles reception via `processPayment` (ledger OUT/IN per operation type).
2. Expenses sheet creates expense + OUTBOUND FT; same `externalRef` re-import → SKIP.

## Google Drive OAuth (per tenant)

1. Configure server env:
   - `oosm.import.gdrive.oauth.client-id`
   - `oosm.import.gdrive.oauth.client-secret`
   - `oosm.import.gdrive.oauth.redirect-uri` = `https://<api-host>/api/public/gdrive/oauth/callback`
   - `oosm.import.gdrive.oauth.frontend-redirect` = `https://<fe-host>/reception/import`
2. In Google Cloud Console create OAuth client (Web) and add the redirect URI; enable Google Drive API + people/email scope.
3. On `/reception/import` click **Connect Google Drive** → Google login (personal or Workspace).
4. Status shows connected email; **Sync now** lists/commits files from `IMPORT_GDRIVE_FOLDER_ID`.
5. **Disconnect** clears the tenant refresh token.


## UI

1. Commit button disabled while `canCommit` is false.
2. Menu: Reception → Import (`/reception/import`).
