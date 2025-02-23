# electronic-script
A programming that for microcontrollers that feels like JavaScript but compiles to and interops with C.

- microcontrollers (requires great level of control over data types and memory)
- feels like JS/TS
- transpiles to C

## Some rules
1. Not a superset of C
2. Little to no runtime
3. Same data types as C
5. Can call C functions and pass own functions to C
6. Robust functionality delivered throug its own, modern standard library

## Program structure

Like any C program, an e-script project has a main function defined which takes in char array and size of array.

```js
import extern "stdio.h";

function main(argv: char[][], arvc: int) {
  printf("hello World!");
}

// Note how we're don't do pointers but treat char* as char[]
```

## Import aliases

```js
import extern "stdio.h" as cstdio;

function main(argv: char[][], arvc: int) {
  cstdio.printf("hello World!");
}

// Note how we're don't do pointers but treat char* as char[]
```

## Variables

A variable can be declared with val to show that it cannot be re-assigned or var to show that it can be re-assigned.

```js
var myNum: int64 = 5;
```

The type of a variable can also be infered

```js
var myNum = 5;
```

Variables can be made nullable with the ? operator. For example:

```js
// Nullable variables
var y: int?;

// Not nullable
var z: int = 5;

// Error
z = null;
```

## Data types

1. int8
2. int16
3. int32
4. int64
5. int128
6. int256
7. int512

8. float
9. double

11. char
12. boolean

13. any
14. object
15. Function

All of these types (except for boolean) can signed or unsigned.

## Literals

Int literals are any positive or negative integers. Decimals are any positive or negative decimal numbers including those in scientific form with `e`.

A char and native strings can be defined with single and couble quotes. A char can hold only a single character, and a native string can hold multiple characers and ends with '\0`. If we use backtics, then the native string is multiline.

```js
val myChar = 'a';
val myNativeString = 'Hello World';

val myMultilineNativeString = `
  Hello World
  Hapoy people
`
```

We can also do string interpolation where we put variables that implelement a toString() method into strings:
```js
val myInt = 56;

val myString = `I have ${myint} items`.

// This is just like in JS
// Will require to import extensions that add toString() to int.
```

## Array
We can create arrays of all the data types

```js
val myitems = [1, 2, 3, 4, 5];

// The standard library will introduce managed dynamic arrays called lists
// And other collections like Maps and Sets.

```

## Objects and Structs

Objects are instances of structs. Internally, they are represented as pointers.

```rust
struct Person {
  name: string;
  surname: string;
  nickname: string?;
}

extention on Person {
  Person(name: string, surname: string) {
    this.name = name;
    this.surname = surname
  }

  ~Person() {
    // destructor
  }
  
  toString() {
    // no runtime, so these are not auto generated.
  }
  
  equals(other: Person) {
    // Checks if equal to another Person
  }

  equals(other: int8) {
    // checks if equal to an int
    // Based on operator operands, compiler will call correct equals method
  }
}

const myPerson = Person {
  name: "",
  // ... basically create a person object without calling a constructor
}

const myPerson2 = Person(
  name: ""
  // ... call constructor with named arguments
  // See how we don't use new
)

// We can also do mixins
struct Employee mixin Person {
  employeeId: int64;

  // Note how we're overriding nickname to now being required
  nickname: string;
}

```

Structs can be anonymous, that is, we can create typed objects without having to define a struct first.

```js
val myobj = {
  name: "Someone!"
}

// In the generated code there will be struct generated for this object. But only this object
// will be of that type.

//These are valid types and can be used anywehere a type is expected.
const myFunc = (a: int64, b: int64): {a: int64, b: int64} => ({a, b})
```

## Reference counting and native structs

Objects that are passed from C are called native objects and are not reference counted. Only objects whose structs are defined in e-script are reference counted. It's very important to note that refrenmce counting adds a bit of a runtime to the language, but this should be small. This means we will also add an extra field to every struct to track the ref count.


