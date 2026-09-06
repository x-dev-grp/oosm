import json
from collections import Counter

p = r"F:\oosm\scripts\_day_import_dry_run.json"
d = json.load(open(p, encoding="utf-8"))
data = d.get("data") or d
print("success", d.get("success"))
if not isinstance(data, dict):
    print(data)
    raise SystemExit(1)

print("businessDate", data.get("businessDate"))
print("canCommit", data.get("canCommit"))
print("totalRows", data.get("totalRows"), "valid", data.get("validRows"), "invalid", data.get("invalidRows"))
print("stockIn", data.get("totalStockIn"), "stockOut", data.get("totalStockOut"))
rows = data.get("rows") or []
errs = [r for r in rows if r.get("status") == "ERROR"]
print("ERROR count", len(errs))
for r in errs[:40]:
    print(" ERR", r.get("sheet"), r.get("rowNumber"), r.get("key"), r.get("message"))
print("status counts", Counter(r.get("status") for r in rows))
for r in rows:
    print(
        f"  {str(r.get('status')):16} {str(r.get('sheet')):18} #{r.get('rowNumber')} "
        f"{r.get('key') or ''} | {r.get('message')}"
    )
