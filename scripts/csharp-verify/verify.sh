#!/usr/bin/env bash
# Compile-check the C# documentation examples against the local SDK. NOT part of CI.
#
# Categorizes each examples/csharp/**/*.cs file:
#   - signature/fragment (*-signature.cs, plus known illustrative fragments) -> SKIP (non-compilable by design)
#   - test file (references DurableExecution.Testing) -> compile with its companion via the testing harness
#   - everything else -> compile standalone via dll-verify.csproj
#
# Prereqs (build once):
#   dotnet build <dotnet>/Libraries/src/Amazon.Lambda.DurableExecution/*.csproj -c Release -f net10.0
#   dotnet build <dotnet>/Libraries/src/Amazon.Lambda.DurableExecution.Testing/*.csproj -c Release -f net10.0
#
# Usage (from repo root): bash scripts/csharp-verify/verify.sh
set -u

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
proj="$repo_root/scripts/csharp-verify/dll-verify.csproj"
tproj="$repo_root/scripts/csharp-verify/dll-verify-testing.csproj"
examples_dir="$repo_root/examples/csharp"

# Illustrative fragments (no class, mirroring the other languages' snippets). Not compilable.
is_fragment() {
  case "$1" in
    *-signature.cs) return 0 ;;
    */core/wait/duration-helpers.cs) return 0 ;;
    */sdk-reference/error-handling/exception-hierarchy.cs) return 0 ;;
  esac
  return 1
}

# Map a test file to the companion that defines the workflow class it references.
companion_for() {
  case "$1" in
    */operations/child-contexts/test-child-context.cs) echo "$examples_dir/operations/child-contexts/basic-child-context.cs" ;;
    *) echo "" ;;  # self-contained test
  esac
}

pass=0; fail=0; skip=0; n=0
failed_files=()

while IFS= read -r f; do
  n=$((n+1))
  if is_fragment "$f"; then
    echo "SKIP  ${f#"$repo_root/"}  (fragment)"
    skip=$((skip+1))
    continue
  fi

  obj=".verify/v$n/obj/"; bin=".verify/v$n/bin/"
  if grep -q "DurableExecution.Testing" "$f"; then
    companion="$(companion_for "$f")"
    if [ -n "$companion" ]; then
      # Compile test + companion together in an isolated temp project dir.
      # Use an absolute, normalized testing-bin path (HintPath in the copied csproj
      # would otherwise resolve relative to the temp dir).
      testing_bin="$(cd "$repo_root/../aws-lambda-dotnet/Libraries/src/Amazon.Lambda.DurableExecution.Testing/bin/Release/net10.0" && pwd)"
      # dotnet on Windows needs a drive-qualified path in HintPath; -m gives C:/... with
      # forward slashes (MSBuild-safe, and no backslash-escaping headaches in sed).
      command -v cygpath >/dev/null 2>&1 && testing_bin="$(cygpath -m "$testing_bin")"
      td="$(mktemp -d)"
      cp "$f" "$companion" "$td/"
      sed "s#__TB__#$testing_bin#" \
        "$repo_root/scripts/csharp-verify/testing-pair.csproj.in" > "$td/pair.csproj"
      if dotnet build "$td/pair.csproj" -v q --nologo >/tmp/csharp-verify.log 2>&1; then
        echo "PASS  ${f#"$repo_root/"}  (+ companion)"; pass=$((pass+1))
      else
        echo "FAIL  ${f#"$repo_root/"}  (+ companion)"; grep -E "error " /tmp/csharp-verify.log | head -5; fail=$((fail+1)); failed_files+=("$f")
      fi
      rm -rf "$td"
    else
      if dotnet build "$tproj" -p:ExampleFile="$f" -p:BaseIntermediateOutputPath="$obj" -p:BaseOutputPath="$bin" -v q --nologo >/tmp/csharp-verify.log 2>&1; then
        echo "PASS  ${f#"$repo_root/"}  (testing)"; pass=$((pass+1))
      else
        echo "FAIL  ${f#"$repo_root/"}  (testing)"; grep -E "error " /tmp/csharp-verify.log | head -5; fail=$((fail+1)); failed_files+=("$f")
      fi
    fi
  else
    if dotnet build "$proj" -p:ExampleFile="$f" -p:BaseIntermediateOutputPath="$obj" -p:BaseOutputPath="$bin" -v q --nologo >/tmp/csharp-verify.log 2>&1; then
      echo "PASS  ${f#"$repo_root/"}"; pass=$((pass+1))
    else
      echo "FAIL  ${f#"$repo_root/"}"; grep -E "error " /tmp/csharp-verify.log | head -5; fail=$((fail+1)); failed_files+=("$f")
    fi
  fi
done < <(find "$examples_dir" -name '*.cs' | sort)

rm -rf "$repo_root/.verify"
echo ""
echo "=== $pass passed, $fail failed, $skip skipped (of $n files) ==="
if [ "$fail" -gt 0 ]; then
  printf '  FAIL %s\n' "${failed_files[@]}"
  exit 1
fi
