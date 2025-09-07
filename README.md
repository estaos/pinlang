# Pin Programming Language

A programming language for microcontrollers that feels like JavaScript but compiles to and interoperates with C.

* Designed for microcontrollers (requires fine-grained control over data types and memory)
* JavaScript/TypeScript-inspired syntax
* Transpiles to C

> **Work in Progress**: Pin is still under development. There’s a lot to do before it’s ready for production use.

## Key Rules

1. Not a superset of C
2. Little to no runtime
3. Same data types as C
4. Can call C functions and pass Pin functions to C
5. Robust functionality delivered through its own small, modern standard library

## Hello World

```js
// main.pin

import extern "stdio.h";

function main(): int32 {
  printf("Hello World!");
  return 0;
}
```

Run it with:

```bash
> pin emit main.pin; clang main.c; ./a
```

## Installation (Windows)

1. Download the cli [here](https://github.com/estaos/pinlang/releases).
2. Place the cli in a folder of your choice and add that folder to your `PATH`.
3. After refreshing your environment variables (usually by restarting your terminal), you should be able to use the `pin` command.
   Test it by running:

   ```bash
   > pin
   No source to compile
   ```

## Developer Setup (Contributing)

1. Install **Java 22**.
2. Use **IntelliJ IDEA** (recommended) or any Java IDE/editor.

To contribute, make a PR to the `dev` branch. Ensure that you also update the version in the relevant `.pom` files.

## License

Project license can be found [here](./LICENSE).
