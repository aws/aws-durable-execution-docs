#!/usr/bin/env python3
"""Vendor a pinned build of @mermaid-js/tiny into docs/assets/javascripts/.

Why this exists:
    docs.aws.amazon.com applies a Content Security Policy that disallows
    third-party script origins. Zensical's default Mermaid integration loads
    mermaid from unpkg.com, which the CSP blocks. We therefore self-host
    Mermaid from docs/assets/ (served same-origin).

    We use the "tiny" build (@mermaid-js/tiny), which ships as a single UMD
    file with no lazy-loaded chunks. All currently-used diagram types
    (flowchart, sequence, state, class, ER) are supported. Mindmap,
    architecture, and KaTeX math are not.

Configuration:
    Version and expected SHA-256 are read from scripts/vendor_mermaid.toml.

Usage:
    python3 scripts/vendor_mermaid.py             # download and verify
    python3 scripts/vendor_mermaid.py --check     # verify only, do not download
    python3 scripts/vendor_mermaid.py --latest    # print pinned + latest on npm

Upgrading:
    1. Bump `version` in scripts/vendor_mermaid.toml.
    2. Run the script. It will fail with the new SHA-256 printed. Paste that
       value into `sha256` in the TOML.
    3. Run the script again. It should succeed.
    4. Preview with `zensical serve`, then commit the TOML and the vendored
       file together.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
import tomllib
import urllib.request
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
PIN_FILE = Path(__file__).resolve().parent / "vendor_mermaid.toml"
DEST_FILE = REPO_ROOT / "docs" / "assets" / "javascripts" / "mermaid.tiny.js"
SOURCE_URL_TEMPLATE = (
    "https://cdn.jsdelivr.net/npm/@mermaid-js/tiny@{version}/dist/mermaid.tiny.js"
)
NPM_REGISTRY_LATEST = "https://registry.npmjs.org/@mermaid-js/tiny/latest"


def load_pin() -> tuple[str, str]:
    """Read the pinned version and expected SHA-256 from the TOML file."""
    if not PIN_FILE.exists():
        print(f"ERROR: pin file missing: {PIN_FILE}", file=sys.stderr)
        sys.exit(1)

    with PIN_FILE.open("rb") as handle:
        data = tomllib.load(handle)

    missing = [key for key in ("version", "sha256") if key not in data]
    if missing:
        print(
            f"ERROR: {PIN_FILE.name} is missing required keys: "
            f"{', '.join(missing)}",
            file=sys.stderr,
        )
        sys.exit(1)
    return data["version"], data["sha256"]


def sha256_of(path: Path) -> str:
    hasher = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(65536), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def check_only(version: str, expected_sha256: str) -> int:
    if not DEST_FILE.exists():
        print(
            f"ERROR: {DEST_FILE.relative_to(REPO_ROOT)} is missing. "
            f"Run without --check to download.",
            file=sys.stderr,
        )
        return 1

    actual = sha256_of(DEST_FILE)
    if actual != expected_sha256:
        print(
            f"ERROR: SHA-256 mismatch for {DEST_FILE.relative_to(REPO_ROOT)}",
            file=sys.stderr,
        )
        print(f"  expected (from {PIN_FILE.name}): {expected_sha256}", file=sys.stderr)
        print(f"  actual:                          {actual}", file=sys.stderr)
        return 1

    print(
        f"OK: {DEST_FILE.relative_to(REPO_ROOT)} matches "
        f"@mermaid-js/tiny@{version} (sha256={expected_sha256})"
    )
    return 0


def print_latest(version: str) -> int:
    with urllib.request.urlopen(NPM_REGISTRY_LATEST, timeout=10) as response:
        payload = json.load(response)
    print(f"Pinned:  {version}")
    print(f"Latest:  {payload['version']}")
    return 0


def download_and_verify(version: str, expected_sha256: str) -> int:
    # Idempotent: skip download if the committed file already matches.
    if DEST_FILE.exists() and sha256_of(DEST_FILE) == expected_sha256:
        print(f"Already up to date: @mermaid-js/tiny@{version}")
        return 0

    source_url = SOURCE_URL_TEMPLATE.format(version=version)
    print(f"Downloading @mermaid-js/tiny@{version} from {source_url}")

    DEST_FILE.parent.mkdir(parents=True, exist_ok=True)
    tmp_file = DEST_FILE.with_suffix(DEST_FILE.suffix + ".tmp")

    try:
        with urllib.request.urlopen(source_url, timeout=30) as response:
            tmp_file.write_bytes(response.read())

        actual = sha256_of(tmp_file)
        if actual != expected_sha256:
            print("ERROR: SHA-256 mismatch after download", file=sys.stderr)
            print(
                f"  expected (from {PIN_FILE.name}): {expected_sha256}",
                file=sys.stderr,
            )
            print(f"  actual:                          {actual}", file=sys.stderr)
            print(
                f"\nIf you intentionally bumped `version` in {PIN_FILE.name}, "
                f"update `sha256` to the 'actual' value above and re-run.",
                file=sys.stderr,
            )
            tmp_file.unlink(missing_ok=True)
            return 1

        tmp_file.replace(DEST_FILE)
    except Exception:
        tmp_file.unlink(missing_ok=True)
        raise

    print(f"Vendored @mermaid-js/tiny@{version}")
    print(f"  Path:    {DEST_FILE.relative_to(REPO_ROOT)}")
    print(f"  SHA-256: {expected_sha256}")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    group = parser.add_mutually_exclusive_group()
    group.add_argument(
        "--check",
        action="store_true",
        help="Verify the committed vendored file matches the pinned SHA-256 "
        "without downloading.",
    )
    group.add_argument(
        "--latest",
        action="store_true",
        help="Print the currently-pinned version and the latest version on npm, "
        "then exit.",
    )
    args = parser.parse_args()

    version, expected_sha256 = load_pin()

    if args.check:
        return check_only(version, expected_sha256)
    if args.latest:
        return print_latest(version)
    return download_and_verify(version, expected_sha256)


if __name__ == "__main__":
    sys.exit(main())
