#!/usr/bin/env python3
"""Vendor a pinned build of GLightbox into docs/assets/.

Why this exists:
    docs.aws.amazon.com applies a Content Security Policy that disallows
    third-party script and stylesheet origins. Zensical's GLightbox
    integration lazy-loads ``glightbox.min.js`` and ``glightbox.min.css``
    from ``unpkg.com`` when ``window.GLightbox`` is not already defined
    (see ``pp()`` in the theme's ``bundle.*.min.js``). The CSP blocks both.

    We therefore self-host GLightbox from ``docs/assets/`` (served
    same-origin). The JS is loaded early via ``extra_javascript`` in
    ``zensical.toml`` so it defines ``window.GLightbox`` before the theme
    needs it, short-circuiting the unpkg load. The CSS is loaded via
    ``extra_css``.

Configuration:
    Version and expected SHA-256 values are read from
    ``scripts/vendor_glightbox.toml``.

Usage:
    python3 scripts/vendor_glightbox.py             # download and verify
    python3 scripts/vendor_glightbox.py --check     # verify only
    python3 scripts/vendor_glightbox.py --latest    # pinned vs latest on npm

Upgrading:
    1. Bump ``version`` in ``scripts/vendor_glightbox.toml``.
    2. Run the script. It will fail with the new SHA-256s printed. Paste
       those values into ``sha256_js`` and ``sha256_css`` in the TOML.
    3. Run the script again. It should succeed.
    4. Preview with ``zensical serve`` and open a page with images, then
       commit the TOML and both vendored files in the same commit.
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

JS_DEST = REPO_ROOT / "docs" / "assets" / "javascripts" / "glightbox.min.js"
CSS_DEST = REPO_ROOT / "docs" / "assets" / "stylesheets" / "glightbox.min.css"

JS_URL_TEMPLATE = (
    "https://cdn.jsdelivr.net/npm/glightbox@{version}/dist/js/glightbox.min.js"
)
CSS_URL_TEMPLATE = (
    "https://cdn.jsdelivr.net/npm/glightbox@{version}/dist/css/glightbox.min.css"
)
NPM_REGISTRY_LATEST = "https://registry.npmjs.org/glightbox/latest"


@dataclass(frozen=True)
class Asset:
    """One vendored asset (JS or CSS)."""

    label: str
    dest: Path
    url_template: str
    sha_key: str
    expected_sha256: str

    def url(self, version: str) -> str:
        return self.url_template.format(version=version)


def load_pin() -> tuple[str, list[Asset]]:
    """Read the pinned version and expected SHA-256s from the TOML file."""
    if not PIN_FILE.exists():
        print(f"ERROR: pin file missing: {PIN_FILE}", file=sys.stderr)
        sys.exit(1)

    with PIN_FILE.open("rb") as handle:
        data = tomllib.load(handle)

    required = ("version", "sha256_js", "sha256_css")
    missing = [key for key in required if key not in data]
    if missing:
        print(
            f"ERROR: {PIN_FILE.name} is missing required keys: "
            f"{', '.join(missing)}",
            file=sys.stderr,
        )
        sys.exit(1)

    assets = [
        Asset(
            label="glightbox.min.js",
            dest=JS_DEST,
            url_template=JS_URL_TEMPLATE,
            sha_key="sha256_js",
            expected_sha256=data["sha256_js"],
        ),
        Asset(
            label="glightbox.min.css",
            dest=CSS_DEST,
            url_template=CSS_URL_TEMPLATE,
            sha_key="sha256_css",
            expected_sha256=data["sha256_css"],
        ),
    ]
    return data["version"], assets


def sha256_of(path: Path) -> str:
    hasher = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(65536), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def check_only(version: str, assets: list[Asset]) -> int:
    failed = False
    for asset in assets:
        if not asset.dest.exists():
            print(
                f"ERROR: {asset.dest.relative_to(REPO_ROOT)} is missing. "
                f"Run without --check to download.",
                file=sys.stderr,
            )
            failed = True
            continue

        actual = sha256_of(asset.dest)
        if actual != asset.expected_sha256:
            print(
                f"ERROR: SHA-256 mismatch for {asset.dest.relative_to(REPO_ROOT)}",
                file=sys.stderr,
            )
            print(
                f"  expected (from {PIN_FILE.name} {asset.sha_key}): "
                f"{asset.expected_sha256}",
                file=sys.stderr,
            )
            print(f"  actual:                             {actual}", file=sys.stderr)
            failed = True
            continue

        print(
            f"OK: {asset.dest.relative_to(REPO_ROOT)} matches "
            f"glightbox@{version} (sha256={asset.expected_sha256})"
        )

    return 1 if failed else 0


def print_latest(version: str) -> int:
    with urllib.request.urlopen(NPM_REGISTRY_LATEST, timeout=10) as response:
        payload = json.load(response)
    print(f"Pinned:  {version}")
    print(f"Latest:  {payload['version']}")
    return 0


def download_and_verify(version: str, assets: list[Asset]) -> int:
    failed = False
    for asset in assets:
        # Idempotent: skip download if committed file already matches.
        if (
            asset.dest.exists()
            and asset.expected_sha256
            and sha256_of(asset.dest) == asset.expected_sha256
        ):
            print(
                f"Already up to date: {asset.label} (glightbox@{version})"
            )
            continue

        source_url = asset.url(version)
        print(f"Downloading {asset.label} from {source_url}")

        asset.dest.parent.mkdir(parents=True, exist_ok=True)
        tmp_file = asset.dest.with_suffix(asset.dest.suffix + ".tmp")

        try:
            with urllib.request.urlopen(source_url, timeout=30) as response:
                tmp_file.write_bytes(response.read())

            actual = sha256_of(tmp_file)
            if actual != asset.expected_sha256:
                print(
                    f"ERROR: SHA-256 mismatch after download for {asset.label}",
                    file=sys.stderr,
                )
                print(
                    f"  expected (from {PIN_FILE.name} {asset.sha_key}): "
                    f"{asset.expected_sha256 or '(empty)'}",
                    file=sys.stderr,
                )
                print(
                    f"  actual:                             {actual}",
                    file=sys.stderr,
                )
                print(
                    f"\nIf you intentionally bumped `version` in {PIN_FILE.name}, "
                    f"update `{asset.sha_key}` to the 'actual' value above and "
                    f"re-run.",
                    file=sys.stderr,
                )
                tmp_file.unlink(missing_ok=True)
                failed = True
                continue

            tmp_file.replace(asset.dest)
            print(f"Vendored {asset.label} (glightbox@{version})")
            print(f"  Path:    {asset.dest.relative_to(REPO_ROOT)}")
            print(f"  SHA-256: {asset.expected_sha256}")
        except Exception:
            tmp_file.unlink(missing_ok=True)
            raise

    return 1 if failed else 0


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
        help="Print the currently-pinned version and the latest version on "
        "npm, then exit.",
    )
    args = parser.parse_args()

    version, assets = load_pin()

    if args.check:
        return check_only(version, assets)
    if args.latest:
        return print_latest(version)
    return download_and_verify(version, assets)


if __name__ == "__main__":
    sys.exit(main())
