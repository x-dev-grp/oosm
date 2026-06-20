#!/usr/bin/env python3
"""Remove Lombok annotations and generate explicit Java accessors."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

LOMBOK_IMPORT = re.compile(r"^import\s+lombok(?:\.[\w*]+|\.\*);\s*\n", re.MULTILINE)
LOMBOK_ANNOTATION = re.compile(
    r"^@(?:Data|Getter|Setter|Builder|Slf4j|RequiredArgsConstructor|NoArgsConstructor|AllArgsConstructor|EqualsAndHashCode|ToString|Value)(?:\([^)]*\))?\s*\n",
    re.MULTILINE,
)
FIELD_LINE = re.compile(
    r"^(?P<indent>\s*)(?P<mods>(?:@\w+(?:\([^)]*\))?\s+)*)"
    r"(?P<access>private|protected)\s+(?P<static>static\s+)?(?P<final>final\s+)?"
    r"(?P<type>[\w<>,\[\]?.@\s]+?)\s+(?P<name>\w+)\s*(?P<init>=\s*[^;]+)?;",
    re.MULTILINE,
)
EXISTING_METHOD = re.compile(
    r"^\s*(?:public|protected|private)\s+(?:static\s+)?(?:[\w<>,\[\]?.@\s]+\s+)?"
    r"(?P<name>(?:get|set|is)[A-Z]\w*)\s*\(",
    re.MULTILINE,
)
CLASS_DECL = re.compile(
    r"(?P<prefix>^[^{]*?\b(?P<kind>class|enum|record|interface)\s+(?P<name>\w+)[^{]*\{)",
    re.MULTILINE | re.DOTALL,
)


def capitalize(name: str) -> str:
    return name[0].upper() + name[1:] if name else name


def getter_name(field_name: str, field_type: str) -> str:
    if field_type == "boolean" or field_type == "Boolean":
        return f"is{capitalize(field_name)}"
    return f"get{capitalize(field_name)}"


def existing_accessors(content: str) -> set[str]:
    return {match.group("name") for match in EXISTING_METHOD.finditer(content)}


def parse_fields(body: str) -> list[dict]:
    fields: list[dict] = []
    for match in FIELD_LINE.finditer(body):
        if match.group("static"):
            continue
        field_type = " ".join(match.group("type").split())
        fields.append(
            {
                "name": match.group("name"),
                "type": field_type,
                "final": bool(match.group("final")),
                "indent": match.group("indent"),
            }
        )
    return fields


def find_class_body(content: str) -> tuple[str, int, int] | None:
    match = CLASS_DECL.search(content)
    if not match:
        return None
    start = match.end() - 1
    depth = 0
    for index in range(start, len(content)):
        char = content[index]
        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return match.group("name"), start + 1, index
    return None


def detect_lombok_flags(content: str) -> dict[str, bool]:
    return {
        "data": "@Data" in content,
        "getter": "@Getter" in content,
        "setter": "@Setter" in content,
        "no_args": "@NoArgsConstructor" in content,
        "all_args": "@AllArgsConstructor" in content,
        "required_args": "@RequiredArgsConstructor" in content,
        "slf4j": "@Slf4j" in content,
        "is_enum": bool(re.search(r"\benum\s+\w+", content)),
    }


def should_getter(flags: dict[str, bool]) -> bool:
    return flags["data"] or flags["getter"] or flags["is_enum"]


def should_setter(flags: dict[str, bool]) -> bool:
    return flags["data"] or flags["setter"]


def generate_getter(field: dict) -> str:
    name = getter_name(field["name"], field["type"])
    return (
        f"\n    public {field['type']} {name}() {{\n"
        f"        return {field['name']};\n"
        f"    }}\n"
    )


def generate_setter(field: dict) -> str:
    param = field["name"]
    method = f"set{capitalize(field['name'])}"
    return (
        f"\n    public void {method}({field['type']} {param}) {{\n"
        f"        this.{field['name']} = {param};\n"
        f"    }}\n"
    )


def generate_no_args_constructor(class_name: str, fields: list[dict], existing: set[str]) -> str:
    if "NoArgsConstructor" in existing or any(
        re.search(rf"public\s+{class_name}\s*\(\s*\)", line)
        for line in existing
    ):
        return ""
    return f"\n    public {class_name}() {{\n    }}\n"


def generate_all_args_constructor(class_name: str, fields: list[dict]) -> str:
    if not fields:
        return ""
    params = ", ".join(f"{field['type']} {field['name']}" for field in fields)
    assigns = "\n".join(f"        this.{field['name']} = {field['name']};" for field in fields)
    return (
        f"\n    public {class_name}({params}) {{\n"
        f"{assigns}\n"
        f"    }}\n"
    )


def generate_required_args_constructor(class_name: str, fields: list[dict]) -> str:
    required = [field for field in fields if field["final"]]
    if not required:
        return ""
    params = ", ".join(f"{field['type']} {field['name']}" for field in required)
    assigns = "\n".join(f"        this.{field['name']} = {field['name']};" for field in required)
    return (
        f"\n    public {class_name}({params}) {{\n"
        f"{assigns}\n"
        f"    }}\n"
    )


def generate_logger(class_name: str) -> tuple[str, str]:
    import_line = "import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;\n"
    field = f"\n    private static final Logger log = LoggerFactory.getLogger({class_name}.class);\n"
    return import_line, field


def transform_file(path: Path) -> bool:
    original = path.read_text(encoding="utf-8")
    if "lombok" not in original:
        return False

    flags = detect_lombok_flags(original)
    existing = existing_accessors(original)
    class_info = find_class_body(original)
    if not class_info:
        print(f"WARN: could not parse class body: {path}", file=sys.stderr)
        return False

    class_name, body_start, body_end = class_info
    body = original[body_start:body_end]
    fields = parse_fields(body)

    content = LOMBOK_IMPORT.sub("", original)
    content = LOMBOK_ANNOTATION.sub("", content)

    class_info = find_class_body(content)
    if not class_info:
        return False
    _, body_start, body_end = class_info

    generated: list[str] = []

    if flags["slf4j"]:
        import_line, logger_field = generate_logger(class_name)
        if "LoggerFactory" not in content:
            package_match = re.search(r"^package .+;\n", content, re.MULTILINE)
            if package_match:
                insert_at = package_match.end()
                content = content[:insert_at] + "\n" + import_line + content[insert_at:]
                class_info = find_class_body(content)
                if class_info:
                    _, body_start, body_end = class_info
        generated.append(logger_field)

    if flags["no_args"]:
        generated.append(generate_no_args_constructor(class_name, fields, existing))
    if flags["all_args"]:
        generated.append(generate_all_args_constructor(class_name, fields))
    if flags["required_args"]:
        generated.append(generate_required_args_constructor(class_name, fields))

    if should_getter(flags):
        for field in fields:
            getter = getter_name(field["name"], field["type"])
            if getter not in existing:
                generated.append(generate_getter(field))
                existing.add(getter)

    if should_setter(flags):
        for field in fields:
            if field["final"]:
                continue
            setter = f"set{capitalize(field['name'])}"
            if setter not in existing:
                generated.append(generate_setter(field))
                existing.add(setter)

    if not generated:
        path.write_text(content, encoding="utf-8")
        return True

    insertion = "".join(generated)
    updated = content[:body_end] + insertion + content[body_end:]
    path.write_text(updated, encoding="utf-8")
    return True


def main() -> int:
    java_files = [
        path
        for path in ROOT.rglob("*.java")
        if "legacy" not in path.parts and "target" not in path.parts
    ]
    changed = 0
    for path in java_files:
        if transform_file(path):
            changed += 1
            print(path.relative_to(ROOT))
    print(f"Updated {changed} files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
