#!/usr/bin/env python3
"""
Extract every inline C# code block (NOT --8<-- includes) from the C# tabs of the
docs and dump each to a file under a scratch dir, tagged by source page + line.
These are the snippets the compile harness never sees (they live only in markdown).

Usage: python scripts/csharp-verify/extract-inline.py <out_dir>
Prints a manifest: <outfile>\t<source.md>:<line>\t<kind>
kind = 'type' (declares interface/enum/class/record -> verify members vs SDK, do not compile)
     | 'code' (has await/lambda/Fact -> try to compile)
     | 'frag' (bare statements/signatures -> neither)
"""
import re, glob, os, sys

out = sys.argv[1] if len(sys.argv) > 1 else '.verify/inline'
os.makedirs(out, exist_ok=True)

def classify(body):
    if re.search(r'\b(interface|enum)\s+\w', body):
        return 'type'
    if re.search(r'\b(class|record)\s+\w', body) and 'await' not in body and '=>' not in body and 'return ' not in body:
        return 'type'
    if 'await' in body or '=>' in body or '[Fact]' in body:
        return 'code'
    return 'frag'

manifest = []
n = 0
for fn in sorted(glob.glob('docs/**/*.md', recursive=True)):
    lines = open(fn, encoding='utf-8').read().split('\n')
    incs = False; inblock = False; buf = []; startln = 0
    for i, ln in enumerate(lines, 1):
        s = ln.strip()
        if re.match(r'=== "C#"', s): incs = True; continue
        if re.match(r'=== "', s): incs = False
        if re.match(r'^#', s): incs = False
        if incs and s.startswith('```csharp'):
            inblock = True; buf = []; startln = i; continue
        if inblock and s.startswith('```'):
            inblock = False
            body = '\n'.join(buf)
            if '--8<--' in body or not body.strip():
                continue
            kind = classify(body)
            n += 1
            outfn = os.path.join(out, f'block_{n:03d}.cs')
            open(outfn, 'w', encoding='utf-8').write(body + '\n')
            manifest.append(f'{outfn}\t{fn}:{startln}\t{kind}')
            continue
        if inblock:
            # de-indent 4 spaces (tab body indentation)
            buf.append(ln[4:] if ln.startswith('    ') else ln)

open(os.path.join(out, 'manifest.tsv'), 'w', encoding='utf-8').write('\n'.join(manifest) + '\n')
for m in manifest:
    print(m)
print(f'\n# {n} inline literal C# blocks extracted to {out}', file=sys.stderr)
