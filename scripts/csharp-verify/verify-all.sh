#!/usr/bin/env bash
# One-command verification of ALL C# in the docs against the real SDK source.
# NOT part of CI. Run from the repo root:  bash scripts/csharp-verify/verify-all.sh
#
# It performs four checks and prints a single PASS/FAIL summary:
#   1. Build the core + testing SDKs (Release, net10.0) so the harness has fresh DLLs.
#   2. Compile every examples/csharp/**/*.cs file against the SDK (verify.sh).
#   3. Verify inline type-mirror blocks in the docs (types the docs redeclare) —
#      every declared type/member must exist in the SDK source (verify-types.py).
#   4. Verify inline signature/fragment blocks — every SDK-ish identifier must
#      exist in the SDK source (verify-frags.py).
#
# Prereqs: dotnet SDK (net10.0), python3, and the aws-lambda-dotnet repo checked out
# as a sibling of this repo (../aws-lambda-dotnet). Override with LAMBDA_DOTNET=<path>.
set -u

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"
LAMBDA_DOTNET="${LAMBDA_DOTNET:-$repo_root/../aws-lambda-dotnet}"
CORE="$LAMBDA_DOTNET/Libraries/src/Amazon.Lambda.DurableExecution"
TESTING="$LAMBDA_DOTNET/Libraries/src/Amazon.Lambda.DurableExecution.Testing"
PY="$(command -v python || command -v python3)"

fail=0
step() { echo ""; echo "======================================================================"; echo "$1"; echo "======================================================================"; }

if [ ! -d "$CORE" ]; then
  echo "ERROR: aws-lambda-dotnet not found at $LAMBDA_DOTNET (set LAMBDA_DOTNET=<path>)"; exit 2
fi

step "1/4  Build SDKs (Release net10.0)"
dotnet build "$CORE/Amazon.Lambda.DurableExecution.csproj" -c Release -f net10.0 -v q --nologo \
  && echo "  core SDK OK" || { echo "  core SDK BUILD FAILED"; fail=1; }
dotnet build "$TESTING/Amazon.Lambda.DurableExecution.Testing.csproj" -c Release -f net10.0 -v q --nologo \
  && echo "  testing SDK OK" || { echo "  testing SDK BUILD FAILED"; fail=1; }

step "2/4  Compile every examples/csharp file against the SDK"
if bash "$repo_root/scripts/csharp-verify/verify.sh"; then
  echo "  example files OK"
else
  echo "  EXAMPLE FILE COMPILE FAILURES (see above)"; fail=1
fi

step "3/4  Verify inline type-mirror blocks vs SDK source"
"$PY" "$repo_root/scripts/csharp-verify/extract-inline.py" .verify/inline >/dev/null
if "$PY" "$repo_root/scripts/csharp-verify/verify-types.py" .verify/inline/manifest.tsv "$CORE" "$TESTING"; then
  echo "  type mirrors OK"
else
  echo "  TYPE-MIRROR MISMATCHES (see above)"; fail=1
fi

step "4/4  Verify inline signature/fragment identifiers vs SDK source"
if "$PY" "$repo_root/scripts/csharp-verify/verify-frags.py" .verify/inline/manifest.tsv "$CORE" "$TESTING"; then
  echo "  fragments OK"
else
  echo "  FRAGMENT IDENTIFIER MISMATCHES (see above)"; fail=1
fi

rm -rf "$repo_root/.verify"
echo ""
echo "======================================================================"
if [ "$fail" -eq 0 ]; then
  echo "RESULT: PASS — all C# in the docs verified against the SDK."
else
  echo "RESULT: FAIL — see sections above."
fi
echo "======================================================================"
exit "$fail"
