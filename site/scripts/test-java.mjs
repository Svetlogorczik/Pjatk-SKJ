// Compiles every Java example in snippets/ with `javac --release 8` (the course requires Java 8).
// Usage: npm run test:java
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', 'snippets');
let failed = 0;
for (const dir of fs.readdirSync(root)) {
  const full = path.join(root, dir);
  // project/ holds fragments and pseudo-code, not complete classes
  if (!fs.statSync(full).isDirectory() || dir === 'project') continue;
  const files = fs.readdirSync(full).filter((f) => f.endsWith('.java')).map((f) => path.join(full, f));
  if (!files.length) continue;
  const out = fs.mkdtempSync(path.join(os.tmpdir(), `skj-${dir}-`));
  try {
    execFileSync('javac', ['--release', '8', '-Xlint:-options', '-d', out, ...files], { stdio: 'pipe' });
    console.log(`OK    ${dir} (${files.length} files)`);
  } catch (e) {
    failed++;
    console.log(`FAIL  ${dir}\n${e.stderr}`);
  } finally {
    fs.rmSync(out, { recursive: true, force: true });
  }
}
process.exit(failed ? 1 : 0);
