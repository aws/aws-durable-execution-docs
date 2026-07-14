#!/usr/bin/env python3
"""Verify that examples/ and docs/ snippet includes stay in sync.

Fails when an example file is not referenced by any ``--8<--`` include in
docs/ (an orphan), or when an include references a file that does not
exist (a broken include). Run from the repository root:

    python3 scripts/check_example_refs.py
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

INCLUDE_PATTERN = re.compile(r'--8<--\s+"(examples/[^"]+)"')


def main() -> int:
    root = Path(__file__).resolve().parent.parent
    docs_dir = root / "docs"
    examples_dir = root / "examples"

    included: set[str] = set()
    for page in docs_dir.rglob("*.md"):
        included.update(INCLUDE_PATTERN.findall(page.read_text(encoding="utf-8")))

    on_disk = {
        str(path.relative_to(root))
        for path in examples_dir.rglob("*")
        if path.is_file()
    }

    orphans = sorted(on_disk - included)
    missing = sorted(included - on_disk)

    for path in orphans:
        print(f"orphaned example (not included by any docs page): {path}")
    for path in missing:
        print(f"broken include (file does not exist): {path}")

    if orphans or missing:
        print(
            f"\n{len(orphans)} orphaned example file(s), "
            f"{len(missing)} broken include(s)."
        )
        return 1

    print(f"OK: {len(on_disk)} example files, all referenced; no broken includes.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
