#!/usr/bin/env python3
"""Vendor a pinned build of glightbox into docs/assets/.

Why this exists:
    docs.aws.amazon.com applies a Content Security Policy that disallows
    third-party script origins. Zensical's theme bundle loads glightbox
    (the image lightbox library) from unpkg.com, which the CSP blocks.
    We therefore self-host glightbox from docs/assets/ (served same-origin).

    The Zensical theme bundle only fetches glightbox from unpkg when
    ``typeof GLightbox === "undefined"``. By loading the vendored UMD
    build before the theme subscribes, ``window.GLightbox`` is already
    defined and the unpkg fetch is skipped. The matching CSS is loaded
    via ``extra_css`` in the same way.

Configuration:
    Version and expected SHA-256 for each file are read from
    scripts/vendor_glightbox.toml.

Usage:
    python3 scripts/vendor_glightbox.py             # download and verify
    python3 scripts/vendor_glightbox.py --check     # verify only, do not download
    python3 scripts/vendor_glightbox.py --latest    # print pinned + latest on npm

Upgrading:
    1. Bump ``version`` in scripts/vendor_glightbox.toml.
    2. Run the script. It will fail with the new SHA-256 values printed.
       Paste those values into ``js_sha256`` and ``css_sha256`` in the TOML.
    3. Run the script again. It should succeed.
    4. Preview with ``zensical serve``, then commit the TOML and the vendored
       files together.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
import tomllib
import urllib.request
from dataclasses import dataclass
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
PIN_FILE = Path(__file__).resolve().parent / "vendor_glightbox.toml"
NPM_REGISTRY_LATEST = "https://registry.npmjs.org/glightbox/latest"


@dataclass(frozen=True)
class VendoredFile:
    """A single file to download, verify, and commit."""

    # Human-readable name for log messages (e.g. "glightbox JS").
    name: str
    # Key in the TOML pin file holding the expected SHA-256.
    sha_key: str
    # URL template keyed by ``version``.
    source_url_template: str
    # Destination path, relative to the repo root.
    dest_path: Path


VENDORED_FILES: tuple[VendoredFile, ...] = (
    VendoredFile(
        name="glightbox JS",
        sha_key="js_sha256",
        source_url_template=(
            "https://cdn.jsdelivr.net/npm/glightbox@{version}/dist/js/glightbox.min.js"
        ),
        dest_path=REPO_ROOT / "docs" / "assets" / "javascripts" / "glightbox.min.js",
    ),
    VendoredFile(
        name="glightbox CSS",
        sha_key="css_sha256",
        source_url_template=(
            "https://cdn.jsdelivr.net/npm/glightbox@{version}/dist/css/glightbox.min.css"
        ),
        dest_path=REPO_ROOT / "docs" / "assets" / "stylesheets" / "glightbox.min.css",
    ),
)


def load_pin() -> tuple[str, dict[str, str]]:
    """Read the pinned version and expected SHA-256 values from the TOML file."""
    if not PIN_FILE.exists():
        print(f"ERROR: pin file missing: {PIN_FILE}", file=sys.stderr)
        sys.exit(1)

    with PIN_FILE.open("rb") as handle:
        data = tomllib.load(handle)

    required = ("version", *(f.sha_key for f in VENDORED_FILES))
    missing = [key for key in required if key not in data]
    if missing:
        print(
            f"ERROR: {PIN_FILE.name} is missing required keys: "
            f"{', '.join(missing)}",
            file=sys.stderr,
        )
        sys.exit(1)

    version = data["version"]
    shas = {f.sha_key: data[f.sha_key] for f in VENDORED_FILES}
    return version, shas


def sha256_of(path: Path) -> str:
    hasher = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(65536), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def check_only(version: str, shas: dict[str, str]) -> int:
    exit_code = 0
    for f in VENDORED_FILES:
        expected = shas[f.sha_key]
        rel = f.dest_path.relative_to(REPO_ROOT)

        if not f.dest_path.exists():
            print(
                f"ERROR: {rel} is missing. Run without --check to download.",
                file=sys.stderr,
            )
            exit_code = 1
            continue

        actual = sha256_of(f.dest_path)
        if actual != expected:
            print(f"ERROR: SHA-256 mismatch for {rel}", file=sys.stderr)
            print(f"  expected (from {PIN_FILE.name}): {expected}", file=sys.stderr)
            print(f"  actual:                          {actual}", file=sys.stderr)
            exit_code = 1
            continue

        print(
            f"OK: {rel} matches glightbox@{version} ({f.sha_key}={expected})"
        )
    return exit_code


def print_latest(version: str) -> int:
    with urllib.request.urlopen(NPM_REGISTRY_LATEST, timeout=10) as response:
        payload = json.load(response)
    print(f"Pinned:  {version}")
    print(f"Latest:  {payload['version']}")
    return 0


def download_and_verify(version: str, shas: dict[str, str]) -> int:
    exit_code = 0
    for f in VENDORED_FILES:
        expected = shas[f.sha_key]
        rel = f.dest_path.relative_to(REPO_ROOT)

        # Idempotent: skip download if the committed file already matches.
        if f.dest_path.exists() and sha256_of(f.dest_path) == expected:
            print(f"Already up to date: {f.name} (glightbox@{version})")
            continue

        source_url = f.source_url_template.format(version=version)
        print(f"Downloading {f.name} (glightbox@{version}) from {source_url}")

        f.dest_path.parent.mkdir(parents=True, exist_ok=True)
        tmp_file = f.dest_path.with_suffix(f.dest_path.suffix + ".tmp")

        try:
            with urllib.request.urlopen(source_url, timeout=30) as response:
                tmp_file.write_bytes(response.read())

            actual = sha256_of(tmp_file)
            if actual != expected:
                print(
                    f"ERROR: SHA-256 mismatch after download for {f.name}",
                    file=sys.stderr,
                )
                print(
                    f"  expected (from {PIN_FILE.name}): {expected}",
                    file=sys.stderr,
                )
                print(
                    f"  actual:                          {actual}",
                    file=sys.stderr,
                )
                print(
                    f"\nIf you intentionally bumped `version` in {PIN_FILE.name}, "
                    f"update `{f.sha_key}` to the 'actual' value above and re-run.",
                    file=sys.stderr,
                )
                tmp_file.unlink(missing_ok=True)
                exit_code = 1
                continue

            tmp_file.replace(f.dest_path)
        except Exception:
            tmp_file.unlink(missing_ok=True)
            raise

        print(f"Vendored {f.name} (glightbox@{version})")
        print(f"  Path:    {rel}")
        print(f"  SHA-256: {expected}")

    return exit_code


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    group = parser.add_mutually_exclusive_group()
    group.add_argument(
        "--check",
        action="store_true",
        help="Verify the committed vendored files match the pinned SHA-256 "
        "values without downloading.",
    )
    group.add_argument(
        "--latest",
        action="store_true",
        help="Print the currently-pinned version and the latest version on npm, "
        "then exit.",
    )
    args = parser.parse_args()

    version, shas = load_pin()

    if args.check:
        return check_only(version, shas)
    if args.latest:
        return print_latest(version)
    return download_and_verify(version, shas)


if __name__ == "__main__":
    sys.exit(main())
