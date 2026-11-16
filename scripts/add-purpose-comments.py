#!/usr/bin/env python3
# PURPOSE: Inserts a short file-purpose header on human-maintained non-NOVA source and config files.
"""Add PURPOSE comments. Skips NOVA, generated files, lockfiles, and JSON."""

from __future__ import annotations

import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

SKIP_DIR_PARTS = {
    "node_modules", "target", "dist", "build", ".git", "backups",
    "NOVA_DNA_source_for_SWARM", "NOVA_OP_SWARM_PACK",
}
SKIP_NAME_SUBSTR = (
    "/novaspace/", "/components/nova/", "/pages/NovaSpaceOp",
    "nova-keyframes", "novaBubbleState", "NovaLogo", "NovaOp",
)
SKIP_SUFFIX = {
    ".json", ".lock", ".png", ".jpg", ".jpeg", ".gif", ".webp", ".ico",
    ".woff", ".woff2", ".ttf", ".eot", ".jar", ".class", ".dump",
    ".mv.db", ".trace.db", ".bin", ".map", ".min.js", ".min.css",
}

COMMENT = {
    ".java": "// PURPOSE: {p}",
    ".js": "// PURPOSE: {p}",
    ".jsx": "// PURPOSE: {p}",
    ".css": "/* PURPOSE: {p} */",
    ".yml": "# PURPOSE: {p}",
    ".yaml": "# PURPOSE: {p}",
    ".sql": "-- PURPOSE: {p}",
    ".sh": "# PURPOSE: {p}",
    ".md": "<!-- PURPOSE: {p} -->",
    ".xml": "<!-- PURPOSE: {p} -->",
    ".properties": "# PURPOSE: {p}",
    ".conf": "# PURPOSE: {p}",
    ".template": "# PURPOSE: {p}",
    ".ino": "// PURPOSE: {p}",
    ".gitignore": "# PURPOSE: {p}",
    ".example": "# PURPOSE: {p}",
}


def is_nova(rel: str) -> bool:
    lower = rel.replace("\\", "/")
    return any(s.lower() in lower.lower() for s in SKIP_NAME_SUBSTR) or "/novaspace/" in lower


def purpose_for(rel: str) -> str:
    p = rel.replace("\\", "/")
    name = Path(p).name
    if "db/migration" in p:
        return None
    if p.endswith("docker-compose.yml"):
        return "Defines the production SWARM Docker Compose services."
    if name == "pom.xml":
        return "Maven build and dependencies for the SWARM Spring Boot API."
    if name == "package.json":
        return "npm manifest for the SWARM React frontend."
    if "db/migration" in p:
        return None  # Flyway checksums already-applied SQL; do not rewrite.
    if "/controller/" in p:
        return f"HTTP API controller: {Path(p).stem}."
    if "/service/" in p:
        return f"Application service: {Path(p).stem}."
    if "/repository/" in p:
        return f"Database access for {Path(p).stem.replace('Repository', '').replace('Impl', '')}."
    if "/model/entity/" in p:
        return f"JPA entity mapping for {Path(p).stem}."
    if "/model/enums/" in p:
        return f"Domain enum {Path(p).stem} used by SWARM APIs and persistence."
    if "/dto/" in p:
        return f"API DTO {Path(p).stem}."
    if "/security/" in p:
        return f"Security component {Path(p).stem} for authentication and access control."
    if "/config/" in p:
        return f"Spring configuration {Path(p).stem}."
    if "/exception/" in p:
        return f"API error type {Path(p).stem}."
    if "/mqtt/" in p:
        return f"MQTT ingest component {Path(p).stem}."
    if "/telemetry/" in p:
        return f"Device ingest payload type {Path(p).stem}."
    if "/util/" in p and p.endswith(".java"):
        return f"Shared backend helper {Path(p).stem}."
    if "/pages/control-center/" in p:
        return f"Control Center page {Path(p).stem}."
    if "/pages/" in p and p.endswith(".jsx"):
        return f"SWARM plant-app page {Path(p).stem}."
    if "/components/hmi/" in p:
        return f"Plant HMI UI {Path(p).stem}."
    if "/components/control-center/" in p:
        return f"Control Center layout {Path(p).stem}."
    if "/components/common/" in p:
        return f"Shared UI component {Path(p).stem}."
    if "/context/" in p:
        return f"React context {Path(p).stem}."
    if p.endswith("api.js"):
        return "Frontend HTTP client for SWARM backend APIs."
    if "/utils/" in p and p.endswith(".js"):
        return f"Frontend helper {Path(p).stem}."
    if "/data/" in p:
        return f"Static plant/HMI reference data {Path(p).stem}."
    if p.endswith("theme.js"):
        return "MUI theme tokens for the SWARM visual identity."
    if p.endswith("main.jsx"):
        return "React application bootstrap."
    if p.endswith("App.jsx"):
        return "SWARM route table and role-based layouts."
    if "firmware/" in p:
        return f"ESP firmware source {name}."
    if p.startswith("docs/"):
        return f"SWARM documentation: {name}."
    if p.startswith("scripts/"):
        return f"Operator/maintenance script {name}."
    if "test/java" in p:
        return f"Automated test {Path(p).stem}."
    if name.startswith("application"):
        return f"Spring configuration {name}."
    if name == "Dockerfile":
        return "Container build for this SWARM component."
    return f"SWARM project file {p}."


def insert(path: Path, rel: str) -> bool:
    text = path.read_text(encoding="utf-8", errors="replace")
    if "PURPOSE:" in text.splitlines()[0] if text else False:
        return False
    if any("PURPOSE:" in line for line in text.splitlines()[:8]):
        return False
    suffix = path.suffix if path.suffix != ".example" else ".example"
    if path.name == ".gitignore":
        fmt = COMMENT[".gitignore"]
    elif path.name.endswith(".conf.template"):
        fmt = COMMENT[".template"]
    elif path.suffix in COMMENT:
        fmt = COMMENT[path.suffix]
    else:
        return False
    purpose = purpose_for(rel)
    if not purpose:
        return False
    line = fmt.format(p=purpose)
    # Preserve shebang
    lines = text.splitlines(keepends=True)
    if lines and lines[0].startswith("#!"):
        rest = "".join(lines[1:])
        path.write_text(lines[0] + line + "\n" + rest, encoding="utf-8")
    elif path.suffix == ".xml" and text.lstrip().startswith("<?xml"):
        first, _, remainder = text.partition("\n")
        path.write_text(first + "\n" + line + "\n" + remainder, encoding="utf-8")
    else:
        path.write_text(line + "\n" + text, encoding="utf-8")
    return True


def main() -> None:
    changed = 0
    skipped_nova = 0
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIR_PARTS]
        for fn in filenames:
            path = Path(dirpath) / fn
            rel = str(path.relative_to(ROOT))
            if path.suffix in SKIP_SUFFIX or fn.endswith(".lock"):
                continue
            if is_nova(rel):
                skipped_nova += 1
                continue
            if path.suffix not in COMMENT and fn not in {".gitignore"} and not fn.endswith(".conf.template") and not fn.endswith(".env.example"):
                continue
            try:
                if insert(path, rel):
                    changed += 1
                    print(rel)
            except Exception as exc:
                print(f"skip {rel}: {exc}")
    print(f"updated={changed} nova_skipped={skipped_nova}")


if __name__ == "__main__":
    main()
