#!/usr/bin/env python3
"""
For each inline 'type' block (a doc snippet that redeclares an SDK public type),
verify every declared type and member name exists in the real SDK source.
These blocks never compile (they duplicate SDK types), so member-existence is the
check. Reports any member named in the docs that is absent from source.

Usage: python scripts/csharp-verify/verify-types.py <manifest.tsv> <sdk_src_dir> [<sdk_src_dir2> ...]
"""
import re, sys, glob, os

manifest = sys.argv[1]
src_dirs = sys.argv[2:]

# Build a haystack of all SDK source text (public API).
hay = []
for d in src_dirs:
    for fn in glob.glob(os.path.join(d, '**', '*.cs'), recursive=True):
        if '/obj/' in fn.replace('\\', '/') or '/bin/' in fn.replace('\\', '/'):
            continue
        try:
            hay.append(open(fn, encoding='utf-8').read())
        except Exception:
            pass
HAY = '\n'.join(hay)

def in_src(name):
    return re.search(r'\b' + re.escape(name) + r'\b', HAY) is not None

rows = [l.split('\t') for l in open(manifest, encoding='utf-8').read().splitlines() if l.strip()]
type_blocks = [(f, s) for f, s, k in rows if k == 'type']

problems = []
checked = 0
for fn, src in type_blocks:
    body = open(fn, encoding='utf-8').read()
    # declared type names
    types = re.findall(r'\b(?:class|interface|enum|record)\s+([A-Za-z_]\w*)', body)
    # declared members: properties/methods (PascalCase identifier followed by { or ( )
    members = set(re.findall(r'\b([A-Z][A-Za-z0-9_]*)\s*(?:\{|\()', body))
    # enum values: lines that are a bare Identifier, (with optional trailing comma)
    enum_vals = set()
    if re.search(r'\benum\b', body):
        for m in re.findall(r'^\s*([A-Z][A-Za-z0-9_]*)\s*,?\s*$', body, re.M):
            enum_vals.add(m)
    names = set(types) | members | enum_vals
    # ignore common BCL/framework identifiers not owned by the SDK
    ignore = {'Task','TimeSpan','Func','ILogger','ILoggerFactory','ILambdaSerializer',
              'IReadOnlyList','CancellationToken','FromSeconds','FromMilliseconds','FromMinutes',
              'GetResults','GetErrors'}  # GetResults/GetErrors verified separately below
    for name in sorted(names):
        if name in ignore:
            continue
        checked += 1
        if not in_src(name):
            problems.append(f'{src}\tMISSING IN SDK: {name}')

# explicit method checks that the generic regex ignores
for fn, src in type_blocks:
    body = open(fn, encoding='utf-8').read()
    for meth in ['GetResults','GetErrors','ThrowIfError','GetResultAsync']:
        if meth in body and not in_src(meth):
            problems.append(f'{src}\tMISSING METHOD IN SDK: {meth}')

print(f'# checked {checked} identifiers across {len(type_blocks)} type blocks')
if problems:
    print('\n'.join(sorted(set(problems))))
    sys.exit(1)
print('ALL type-mirror identifiers exist in SDK source')
