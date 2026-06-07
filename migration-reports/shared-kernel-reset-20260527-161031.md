# Shared Kernel Reset Report

- Source root: C:\osmproject
- Target root: C:\osmproject\OSM 2.0
- Dry run: False
- Import rewrite skipped: False

## Removed Paths

- C:\osmproject\OSM 2.0\modules\shared-kernel\src\main\java\com\xdev\ooms\sharedkernel
- C:\osmproject\OSM 2.0\modules\shared-kernel\legacy\osm-parent
- C:\osmproject\OSM 2.0\modules\shared-kernel\target

## Copied Source

| Source | Java files copied | Missing package |
|---|---:|---:|
| xdev-base | 50 | 0 |
| xdev-security | 3 | 0 |
| comunicator | 86 | 0 |

## Import Rewrite

- Existing project Java files rewritten: 0
- Unresolved old support imports after rewrite: 0
- Legacy reference poms copied: 3

## Package Mapping

- `com.xdev.xdevbase` -> `com.xdev.ooms.sharedkernel`
- `com.xdev.mailSender` -> `com.xdev.ooms.sharedkernel.mail`
- `com.xdev.onsignalNotifcations` -> `com.xdev.ooms.sharedkernel.notifications`
- `com.xdev.xdevsecurity` -> `com.xdev.ooms.sharedkernel.xdevsecurity`
- `com.xdev.communicator` -> `com.xdev.ooms.sharedkernel.communicator`
- `com.xdev.comunicator` -> `com.xdev.ooms.sharedkernel.communicator`
