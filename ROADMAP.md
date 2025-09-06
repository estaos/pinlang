# PinLang roadmap

## Versioning Plan

- 0.x.y → Language is still evolving rapidly, expect breaking changes.
- 0.1.x → 0.4.x → Building the foundations (syntax, type system, core features).
- 0.5.x → 0.9.x → Stabilization, ecosystem (stdlib, tooling, embedded integration).
- 1.0.0 → First “production” release, usable by others without major breaking changes.

## Future versions

### 0.1.1
- Basic transpiler working
- Some form of type system (incomplete)
- No proper data structures/collections yet

### 0.2.0
Focus is on type system and basic data structures.
- [ ] Define and implement type system rules (primitive types, type checking and casting)
- [ ] Add structs and enums
- [ ] Improved error reporting (transpiler messages)

### 0.3.0
Focus is on usable core language
- [ ] Implement arrays with transpilation to c arrays and pointers
- [ ] Design and implement safe ways of represeting collections (And on memory allocation on embedded devices)
- [ ] For each loop on arrays and collections

### 0.4.0
Focus is on making the language practical
- [ ] Minimal standard library (stdlib) (math, string, basic collections)
- [ ] Allow configuration of entrypoint functions (e.g main)
- [ ] Allow configuration of targets directory (i.e where the transpiled files should go)
- [ ] Package sharing and pulling
- [ ] Full cstdlib definition
- [ ] Document usage of stdlib

### 0.5.0
Focus is on EstaOS prototype
- [ ] Add zephyros libs declarations
- [ ] Allow pinlang to be used in zephros projects
- [ ] Proptotype EstaOS

### 0.6.0
Focus is on tooling and usability
- [ ] Build proper `pin` tool (rethink usability and ease of use for transpiling, formatting, etc)
- [ ] Syntax highlighting or VsCode and, maybe, CLion.
- [ ] Improve error diagnostics (line, column hints, suggestions, etc)
- [ ] LSP for VS Code
- [ ] Website to include getting started guide with examples

### 0.7.0
Focus is on making sure we are open source ready (Community release)
- [ ] Clear README with license and contribution guidelines
- [ ] Clear and secure tool distributiuon pipeline (e.g `pin` tool downloads)
- [ ] Focus is developer experience (especially the estaos developers)
- [ ] More talks and developer engagements on real hardware

### 0.8.x -> 0.9.x
Focus is on stabilisation
- [ ] Refactor based on developer feedback
- [ ] Finalize language grammar and stdlib APIs
- [ ] Performance profiling + optimisations
- [ ] Larger embedded project (EstaOS)

### 1.0.0
We are production ready
- [ ] EstaOS is at least in pilot phase
- [ ] Complete type system + collections
- [ ] Solid stdlib (strings, math, collections)
- [ ] Docs + tutorials + website showcase

## Other initiatives
1. IDE Integration (VsCode)
2. Project website
3. EstaOS
