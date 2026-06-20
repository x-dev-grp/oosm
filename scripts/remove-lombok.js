#!/usr/bin/env node
/** Remove Lombok annotations and generate explicit Java accessors. */

const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..");

const LOMBOK_IMPORT = /^import\s+lombok(?:\.[\w*]+|\.\*);\s*\n/gm;
const LOMBOK_ANNOTATION =
  /^@(?:Data|Getter|Setter|Builder|Slf4j|RequiredArgsConstructor|NoArgsConstructor|AllArgsConstructor|EqualsAndHashCode|ToString|Value)(?:\([^)]*\))?\s*\n/gm;
const FIELD_LINE =
  /^(\s*)((?:@\w+(?:\([^)]*\))?\s+)*)?(private|protected)\s+(?:(static)\s+)?(?:(final)\s+)?([\w<>,\[\]?.@\s]+?)\s+(\w+)\s*(=\s*[^;]+)?;/gm;
const EXISTING_METHOD =
  /^\s*(?:public|protected|private)\s+(?:static\s+)?(?:[\w<>,\[\]?.@\s]+\s+)?((?:get|set|is)[A-Z]\w*)\s*\(/gm;
const CLASS_DECL =
  /^[^{]*?\b(class|enum|record|interface)\s+(\w+)[^{]*\{/ms;

function walk(dir, files = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (entry.name === "target" || entry.name === "legacy" || entry.name === ".git") continue;
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(full, files);
    else if (entry.name.endsWith(".java")) files.push(full);
  }
  return files;
}

function capitalize(name) {
  return name ? name[0].toUpperCase() + name.slice(1) : name;
}

function getterName(fieldName, fieldType) {
  const t = fieldType.trim();
  if (t === "boolean" || t === "Boolean") return `is${capitalize(fieldName)}`;
  return `get${capitalize(fieldName)}`;
}

function existingAccessors(content) {
  const names = new Set();
  let m;
  const re = new RegExp(EXISTING_METHOD.source, EXISTING_METHOD.flags);
  while ((m = re.exec(content))) names.add(m[1]);
  return names;
}

function parseFields(body) {
  const fields = [];
  let m;
  const re = new RegExp(FIELD_LINE.source, FIELD_LINE.flags);
  while ((m = re.exec(body))) {
    if (m[4]) continue; // static
    fields.push({
      name: m[7],
      type: m[6].replace(/\s+/g, " ").trim(),
      final: Boolean(m[5]),
      indent: m[1],
    });
  }
  return fields;
}

function findClassBody(content) {
  const match = content.match(CLASS_DECL);
  if (!match) return null;
  const start = match.index + match[0].length - 1;
  let depth = 0;
  for (let i = start; i < content.length; i++) {
    if (content[i] === "{") depth++;
    else if (content[i] === "}") {
      depth--;
      if (depth === 0) return { className: match[2], bodyStart: start + 1, bodyEnd: i };
    }
  }
  return null;
}

function detectFlags(content) {
  return {
    data: content.includes("@Data"),
    getter: content.includes("@Getter"),
    setter: content.includes("@Setter"),
    noArgs: content.includes("@NoArgsConstructor"),
    allArgs: content.includes("@AllArgsConstructor"),
    requiredArgs: content.includes("@RequiredArgsConstructor"),
    slf4j: content.includes("@Slf4j"),
    isEnum: /\benum\s+\w+/.test(content),
  };
}

function generateGetter(field) {
  const name = getterName(field.name, field.type);
  return `\n    public ${field.type} ${name}() {\n        return ${field.name};\n    }\n`;
}

function generateSetter(field) {
  const method = `set${capitalize(field.name)}`;
  return `\n    public void ${method}(${field.type} ${field.name}) {\n        this.${field.name} = ${field.name};\n    }\n`;
}

function generateNoArgsConstructor(className) {
  return `\n    public ${className}() {\n    }\n`;
}

function generateAllArgsConstructor(className, fields) {
  if (!fields.length) return "";
  const params = fields.map((f) => `${f.type} ${f.name}`).join(", ");
  const assigns = fields.map((f) => `        this.${f.name} = ${f.name};`).join("\n");
  return `\n    public ${className}(${params}) {\n${assigns}\n    }\n`;
}

function generateRequiredArgsConstructor(className, fields) {
  const required = fields.filter((f) => f.final);
  if (!required.length) return "";
  const params = required.map((f) => `${f.type} ${f.name}`).join(", ");
  const assigns = required.map((f) => `        this.${f.name} = ${f.name};`).join("\n");
  return `\n    public ${className}(${params}) {\n${assigns}\n    }\n`;
}

function hasNoArgsConstructor(content, className) {
  return new RegExp(`public\\s+${className}\\s*\\(\\s*\\)`).test(content);
}

function transformFile(filePath) {
  const original = fs.readFileSync(filePath, "utf8");
  if (!original.includes("lombok")) return false;

  const flags = detectFlags(original);
  const existing = existingAccessors(original);
  let classInfo = findClassBody(original);
  if (!classInfo) {
    console.warn(`WARN: could not parse: ${filePath}`);
    return false;
  }

  const fields = parseFields(original.slice(classInfo.bodyStart, classInfo.bodyEnd));

  let content = original.replace(LOMBOK_IMPORT, "").replace(LOMBOK_ANNOTATION, "");
  classInfo = findClassBody(content);
  if (!classInfo) return false;

  const generated = [];

  if (flags.slf4j) {
    if (!content.includes("LoggerFactory")) {
      const pkg = content.match(/^package .+;\n/m);
      if (pkg) {
        const imports = "import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;\n";
        content = content.slice(0, pkg.index + pkg[0].length) + "\n" + imports + content.slice(pkg.index + pkg[0].length);
        classInfo = findClassBody(content);
      }
    }
    generated.push(`\n    private static final Logger log = LoggerFactory.getLogger(${classInfo.className}.class);\n`);
  }

  if (flags.noArgs && !hasNoArgsConstructor(content, classInfo.className)) {
    generated.push(generateNoArgsConstructor(classInfo.className));
  }
  if (flags.allArgs) generated.push(generateAllArgsConstructor(classInfo.className, fields));
  if (flags.requiredArgs) generated.push(generateRequiredArgsConstructor(classInfo.className, fields));

  const wantGetter = flags.data || flags.getter || flags.isEnum;
  const wantSetter = flags.data || flags.setter;

  if (wantGetter) {
    for (const field of fields) {
      const getter = getterName(field.name, field.type);
      if (!existing.has(getter)) {
        generated.push(generateGetter(field));
        existing.add(getter);
      }
    }
  }

  if (wantSetter) {
    for (const field of fields) {
      if (field.final) continue;
      const setter = `set${capitalize(field.name)}`;
      if (!existing.has(setter)) {
        generated.push(generateSetter(field));
        existing.add(setter);
      }
    }
  }

  const updated = content.slice(0, classInfo.bodyEnd) + generated.join("") + content.slice(classInfo.bodyEnd);
  fs.writeFileSync(filePath, updated, "utf8");
  return true;
}

let changed = 0;
for (const file of walk(ROOT)) {
  if (transformFile(file)) {
    changed++;
    console.log(path.relative(ROOT, file));
  }
}
console.log(`Updated ${changed} files`);
