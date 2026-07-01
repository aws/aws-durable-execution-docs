#!/usr/bin/env python3
"""Cross-check every SDK-ish identifier in 'frag' inline blocks against SDK source."""
import re, glob, os, sys

manifest = sys.argv[1]
src_dirs = sys.argv[2:]

hay = []
for d in src_dirs:
    for fn in glob.glob(os.path.join(d, '**', '*.cs'), recursive=True):
        p = fn.replace('\\', '/')
        if '/obj/' in p or '/bin/' in p:
            continue
        try:
            hay.append(open(fn, encoding='utf-8').read())
        except Exception:
            pass
HAY = '\n'.join(hay)

def has(n):
    return re.search(r'\b' + re.escape(n) + r'\b', HAY) is not None

ignore = set('''Task Func IReadOnlyList CancellationToken String Int Void Params Object Exception
ILogger TimeSpan Boolean Double LogTrace LogDebug LogInformation LogWarning LogError LogCritical
Where ToList Select FromSeconds FromMilliseconds FromMinutes System Threading Tasks Microsoft
Extensions Logging Amazon Lambda Core BeginScope Dictionary
Assert Equal NotNull True False Contains Empty Throws ThrowsAsync
Drive Complete Keep Start Register Set Use Both Configuration Running Not Running'''.split())

# Words that only ever appear inside // comments are not API; strip comment lines first.

rows = [l.split('\t') for l in open(manifest, encoding='utf-8').read().splitlines() if l.strip()]
frags = [(f, s) for f, s, k in rows if k == 'frag']
prob = []
checked = 0
for f, src in frags:
    body = open(f, encoding='utf-8').read()
    # drop // comment tails so prose words in comments aren't treated as API
    body = '\n'.join(re.sub(r'//.*$', '', ln) for ln in body.split('\n'))
    for name in set(re.findall(r'\b([A-Z][A-Za-z0-9_]{2,})\b', body)):
        if name in ignore:
            continue
        checked += 1
        if not has(name):
            prob.append(f'{src}\t{name}')

print(f'# checked {checked} identifiers across {len(frags)} frag blocks')
if prob:
    for p in sorted(set(prob)):
        print('MISSING:', p)
    sys.exit(1)
print('ALL frag identifiers exist in SDK source')
