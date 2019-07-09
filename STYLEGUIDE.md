# Axibase Style Guide for Java Code

This document serves as the definition of Axibase coding standards for source code in the Java™ Programming Language. It's mostly based on [Google Code Style](https://google.github.io/styleguide/javaguide.html) with some stricter rules covering tests implementation.

---

## Contents

* [1 Terminology notes](#1-terminology-notes)
* [2 Source file basics](#2-source-file-basics)
  * [2.1 File name](#21-file-name)
  * [2.2 File encoding: UTF-8](#22-file-encoding-utf-8)
  * [2.3 Special characters](#23-special-characters)
    * [2.3.1 Whitespace characters](#231-whitespace-characters)
    * [2.3.2 Special escape sequences](#232-special-escape-sequences)
    * [2.3.3 Non-ASCII characters](#233-non-ascii-characters)
* [3 Source file structure](#3-source-file-structure)
  * [3.1 License or copyright information, if present](#31-license-or-copyright-information-if-present)
  * [3.2 Package statement](#32-package-statement)
  * [3.3 Import statements](#33-import-statements)
    * [3.3.1 Wildcard imports](#331-wildcard-imports)
    * [3.3.2 No line-wrapping](#332-no-line-wrapping)
    * [3.3.3 Ordering and spacing](#333-ordering-and-spacing)
    * [3.3.4 No static import for classes](#334-no-static-import-for-classes)
    * [3.3.5 Prefer static imports for Assert.* methods, non-static imports otherwise](#335-prefer-static-imports-for-assert-methods-non-static-imports-otherwise)
    * [3.3.6 Unused imports must be deleted](#336-unused-imports-must-be-deleted)
  * [3.4 Class declaration](#34-class-declaration)
    * [3.4.1 Exactly one top-level class declaration](#341-exactly-one-top-level-class-declaration)
    * [3.4.2 Ordering of class contents](#342-ordering-of-class-contents)
      * [3.4.2.1 Overloads: never split](#3421-overloads-never-split)
      * [3.4.2.2 Private methods placement](#3422-private-methods-placement)
* [4 Formatting](#4-formatting)
  * [4.1 Braces](#41-braces)
    * [4.1.1 Braces are used where optional](#411-braces-are-used-where-optional)
    * [4.1.2 Nonempty blocks: K & R style](#412-nonempty-blocks-k--r-style)
    * [4.1.3 Empty blocks: can be concise](#413-empty-blocks-can-be-concise)
  * [4.2 Block indentation: +4 spaces](#42-block-indentation-4-spaces)
  * [4.3 One statement per line](#43-one-statement-per-line)
  * [4.4 Column limit: 100](#44-column-limit-100)
  * [4.5 Line-wrapping](#45-line-wrapping)
    * [4.5.1 Where to break](#451-where-to-break)
    * [4.5.2 Indent continuation lines at least +8 spaces](#452-indent-continuation-lines-at-least-8-spaces)
  * [4.6 Whitespace](#46-whitespace)
    * [4.6.1 Vertical Whitespace](#461-vertical-whitespace)
    * [4.6.2 Horizontal whitespace](#462-horizontal-whitespace)
    * [4.6.3 Horizontal alignment: never required](#463-horizontal-alignment-never-required)
  * [4.7 Grouping parentheses: recommended](#47-grouping-parentheses-recommended)
  * [4.8 Specific constructs](#48-specific-constructs)
    * [4.8.1 Enum classes](#481-enum-classes)
    * [4.8.2 Variable declarations](#482-variable-declarations)
      * [4.8.2.1 One variable per declaration](#4821-one-variable-per-declaration)
      * [4.8.2.2 Declared when needed](#4822-declared-when-needed)
    * [4.8.3 Arrays](#483-arrays)
      * [4.8.3.1 Array initializers: can be "block-like"](#4831-array-initializers)
      * [4.8.3.2 No C-style array declarations](#4832-no-c-style-array-declarations)
    * [4.8.4 Switch statements](#484-switch-statements)
      * [4.8.4.1 Indentation](#4841-indentation)
      * [4.8.4.2 Fall-through: commented](#4842-fall-through-commented)
      * [4.8.4.3 The `default` case is present](#4843-the-default-case-is-present)
    * [4.8.5 Annotations](#485-annotations)
    * [4.8.6 Comments](#486-comments)
      * [4.8.6.1 Block comment style](#4861-block-comment-style)
    * [4.8.7 Modifiers](#487-modifiers)
    * [4.8.8 Numeric Literals](#488-numeric-literals)
* [5 Naming](#5-naming)
  * [5.1 Rules common to all identifiers](#51-rules-common-to-all-identifiers)
  * [5.2 Rules by identifier type](#52-rules-by-identifier-type)
    * [5.2.1 Package names](#521-package-names)
    * [5.2.2 Class names](#522-class-names)
    * [5.2.3 Method names](#523-method-names)
    * [5.2.4 Constant names](#524-constant-names)
    * [5.2.5 Non-constant field names](#525-non-constant-field-names)
    * [5.2.6 Parameter names](#526-parameter-names)
    * [5.2.7 Local variable names](#527-local-variable-names)
    * [5.2.8 Type variable names](#528-type-variable-names)
  * [5.3 Camel case: defined](#53-camel-case-defined)
* [6 Programming Practices](#6-programming-practices)
  * [6.1 `@Override`: always used](#61-override-always-used)
  * [6.2 Caught exceptions: not ignored](#62-caught-exceptions-not-ignored)
  * [6.3 Static members: qualified using class](#63-static-members-qualified-using-class)
  * [6.4 Finalizers: not used](#64-finalizers-not-used)
  * [6.5 Static members: never modify after initialization](#65-static-members-never-modify-after-initialization)

---

## 1 Terminology notes

In this document, unless otherwise clarified:

1. The term _class_ is used inclusively to mean an "ordinary" class, enum class, interface or annotation type (`@interface`).
2. The term _member_ (of a class) is used inclusively to mean a nested class, field, method, _or constructor_; that is, all top-level contents of a class except initializers and comments.
3. The term _comment_ always refers to _implementation_ comments. We do not use the phrase "documentation comments", instead using the common term "Javadoc."

Other "terminology notes" are appear occasionally throughout the document.

## 2 Source file basics

### 2.1 File name

The source file name consists of the case-sensitive name of the top-level class it contains (of which there is [exactly one](#341-one-top-level-class)), plus the `.java` extension.

### 2.2 File encoding: UTF-8

Source files are encoded in **UTF-8**.

### 2.3 Special characters

#### 2.3.1 Whitespace characters

Aside from the line terminator sequence, the **ASCII horizontal space character** (**0x20**) is the only whitespace character that appears anywhere in a source file. This implies that:

1. All other whitespace characters in string and character literals are escaped.
2. Tab characters are **not** used for indentation.

#### 2.3.2 Special escape sequences

For any character that has a [special escape sequence](http://docs.oracle.com/javase/tutorial/java/data/characters.html) (`\b`, `\t`, `\n`, `\f`, `\r`, `\"`, `\'` and `\\`), that sequence is used rather than the corresponding octal or Unicode escape.

* :heavy_check_mark: **GOOD**

  ```java
  String a = "\n";
  ```

* :no_entry: **BAD**

  ```java
  String a = "\012";
  String a = "\u000a";
  ```

#### 2.3.3 Non-ASCII characters

For the remaining non-ASCII characters, either the actual Unicode character (e.g. `∞`) or the equivalent Unicode escape (e.g. `\u221e`) is used. The choice depends only on which makes the code **easier to read and understand**, although Unicode escapes outside string literals and comments are strongly discouraged.

> **Tip:** In the Unicode escape case, and occasionally even when actual Unicode characters are used, an explanatory comment can be very helpful.

Example|Discussion
---|---
`String unitAbbrev = "μs";`|:heavy_check_mark: Best: perfectly clear even without a comment.
`String unitAbbrev = "\u03bcs"; // "μs"`|:warning: Allowed, but there's no reason to do this.
`String unitAbbrev = "\u03bcs"; // Greek letter mu, "s"`|:warning: Allowed, but awkward and prone to mistakes.
`String unitAbbrev = "\u03bcs";`|:warning: Poor: the reader has no idea what this is.
`return '\ufeff' + content; // byte order mark`|:heavy_check_mark: Good: use escapes for non-printable characters, and comment if necessary.

> **Tip:** Never make your code less readable simply out of fear that some programs might not handle non-ASCII characters properly. If that should happen, those programs are **broken** and they must be **fixed**.

## 3 Source file structure

A source file consists of, **in order**:

1. License or copyright information, if present
2. Package statement
3. Import statements
4. Exactly one top-level class

**Exactly one blank line** separates each section that is present.

IDE-generated header with information about the author **must be removed**.

### 3.1 License or copyright information, if present

If license or copyright information belongs in a file, it belongs here.

### 3.2 Package statement

The package statement is **not line-wrapped**. The [column limit](#44-column-limit) does not apply to package statements.

### 3.3 Import statements

#### 3.3.1 Wildcard imports

**Wildcard imports**, opposite to [Google style guide](https://google.github.io/styleguide/javaguide.html), **are allowed**.

#### 3.3.2 No line-wrapping

Import statements are **not line-wrapped**. The [column limit](#44-column-limit) does not apply to import statements.

#### 3.3.3 Ordering and spacing

Imports are ordered as follows:

1. All non-static imports in a single block.
2. All static imports in a single block.

If there are both static and non-static imports, a single blank line separates the two blocks. There are no other blank lines between import statements.

Within each block the imported names appear in ASCII sort order. 

> **Note:** this is not the same as the import _statements_ being in ASCII sort order, since '.' sorts before ';'.

#### 3.3.4 No static import for classes

Static import is not used for static nested classes. They are imported with normal imports.

#### 3.3.5 Prefer static imports for Assert.* methods, non-static imports otherwise

* :heavy_check_mark: **GOOD**

  ```java
  assertEquals(code, 0);
  ```

  ```java
  Wait().withMessage(() -> ...)
          .until(ExpectedConditions.titleIs(expectedTitle));
  ```

* :no_entry: **BAD**

  ```java  
  Assert.assertEquals(code, 0);
  ```

  ```java
  Wait().withMessage(() -> ...)
          .until(titleIs(expectedTitle));
  ```

#### 3.3.6 Unused imports must be deleted

All imported classes must be used

### 3.4 Class declaration

#### 3.4.1 Exactly one top-level class declaration

Each top-level class resides in a source file of its own.

#### 3.4.2 Ordering of class contents

The order you choose for the members and initializers of your class can have a great effect on learnability. However, there's no single correct recipe for how to do it; different classes can order their contents in different ways.

What is important is that each class uses **_some_ logical order**, which its maintainer could explain if asked. For example, new methods are not just habitually added to the end of the class, as that would yield "chronological by date added" ordering, which is not a logical ordering.

##### 3.4.2.1 Overloads: never split

When a class has multiple constructors, or multiple methods with the same name, these appear sequentially, with no other code in between (not even private members).

##### 3.4.2.2 Private methods placement

The private method must be placed under the first method it is called from. Multiple private methods used in one place must be placed in order of usage.

## 4 Formatting

> **Terminology Note:** _block-like construct_ refers to the body of a class, method or constructor. Note that any [array initializer](#4831-array-initializers) _can_ optionally be treated as a block-like construct.

### 4.1 Braces

#### 4.1.1 Braces are used where optional

Braces are used with `if`, `else`, `for`, `do` and `while` statements, even when the body is empty or contains only a single statement.

#### 4.1.2 Nonempty blocks: K & R style

Braces follow the Kernighan and Ritchie style ("[Egyptian brackets](http://www.codinghorror.com/blog/2012/07/new-programming-jargon.html)") for _nonempty_ blocks and block-like constructs:

* No line break before the opening brace.
* Line break after the opening brace.
* Line break before the closing brace.
* Line break after the closing brace, _only if_ that brace terminates a statement or terminates the body of a method, constructor, or _named_ class. For example, there is _no_ line break after the brace if it is followed by `else` or a comma.

* :heavy_check_mark: **GOOD**

  ```java
  return () -> {
      while (condition()) {
          method();
      }
  };
  ```

  ```java
  return new MyClass() {
      @Override public void method() {
          if (condition()) {
              try {
                  something();
              } catch (ProblemException e) {
                  recover();
              }
          } else if (otherCondition()) {
              somethingElse();
          } else {
              lastThing();
          }
      }
  };
  ```

A few exceptions for enum classes are given in [4.8.1 Enum classes](#481-enum-classes).

#### 4.1.3 Empty blocks: can be concise

An empty block or block-like construct can be in [K & R style](#412-nonempty-blocks-k--r-style). Alternatively, it can be closed immediately after it is opened, with no characters or line break in between (`{}`), **unless** it is part of a _multi-block statement_ (one that directly contains multiple blocks: `if/else` or `try/catch/finally`).

* :heavy_check_mark: **GOOD**

  ```java
  void doNothing() {}
  ```

  ```java
  void doNothingElse() {
  }
  ```

* :no_entry: **BAD**

  ```java
  // No concise empty blocks in a multi-block statement.
  try {
      doSomething();
  } catch (Exception e) {}
  ```

### 4.2 Block indentation: +4 spaces

Each time a new block or block-like construct is opened, the indent increases by **4** spaces. When the block ends, the indent returns to the previous indent level. The indent level applies to both code and comments throughout the block.

* :heavy_check_mark: **GOOD**

  ```java
  if (name != null) {
      // Perform check, if name is initialized.
      Registry.Metric.checkExists(name);
  }
  ```

### 4.3 One statement per line

Each statement is followed by a line break.

* :heavy_check_mark: **GOOD**

  ```java
  int a = 1;
  int b = 2;
  ```

* :no_entry: **BAD**

  ```java
  int a = 1; int b = 2;
  ```

### 4.4 Column limit: 100

Java code has a column limit of 100 characters. A "character" means any Unicode code point. Except as noted below, any line that is exceed this limit must be [line-wrapped](#45-line-wrapping).

Each Unicode code point counts as one character, even if its display width is greater or less. For example, if using [fullwidth characters](https://en.wikipedia.org/wiki/Halfwidth_and_fullwidth_forms), you can choose to wrap the line earlier than where this rule strictly requires.

**Exceptions:**

1. Lines where obeying the column limit is not possible (for example, a long URL in Javadoc, or a long JSNI method reference).
2. [`package`](#32-package-statement) and [`import`](#33-import-statements) statements.
3. Command lines in a comment that can be cut-and-pasted into a shell.

### 4.5 Line-wrapping

**Terminology Note:** When code that might otherwise legally occupy a single line is divided into multiple lines, this activity is called _line-wrapping_.

There is no comprehensive, deterministic formula showing _exactly_ how to line-wrap in every situation. Very often there are several valid ways to line-wrap the same piece of code.

> **Note:** While the typical reason for line-wrapping is to avoid overflowing the column limit, even code that is in fact fit within the column limit _can_ be line-wrapped at the author's discretion.

> **Tip:** Extracting a method or local variable can solve the problem without the need to line-wrap.

#### 4.5.1 Where to break

The prime directive of line-wrapping is: prefer to break at a **higher syntactic level**. Also:

1. When a line is broken at a _non-assignment_ operator the break comes _before_ the symbol.
    * This also applies to the following "operator-like" symbols:
      * the dot separator (`.`)
      * the two colons of a method reference (`::`)
      * an ampersand in a type bound (`<T extends Foo & Bar>`)
      * a pipe in a catch block (`catch (FooException | BarException e)`)
2. When a line is broken at an _assignment_ operator the break typically comes _after_ the symbol, but either way is acceptable.
    * This also applies to the "assignment-operator-like" colon in an enhanced `for` ("foreach") statement.
3. A method or constructor name stays attached to the open parenthesis (`(`) that follows it.
4. A comma (`,`) stays attached to the token that precedes it.
5. A line is never broken adjacent to the arrow in a lambda, except that a break can come immediately after the arrow if the body of the lambda consists of a single unbraced expression. Examples:

```java
MyLambda<String, Long, Object> lambda =
        (String label, Long value, Object obj) -> {
            ...
        };

Predicate<String> predicate = str ->
        longExpressionInvolving(str);
```

> **Note:** The primary goal for line wrapping is to have clear code, _not necessarily_ code that fits in the smallest number of lines.

#### 4.5.2 Indent continuation lines at least +8 spaces

When line-wrapping, each line after the first (each _continuation line_) is indented at least **+8** from the original line.

When there are multiple continuation lines, indentation can be varied beyond **+8**. In general, two continuation lines use the same indentation level if and only if they begin with syntactically parallel elements.

[Horizontal alignment](#463-horizontal-alignment) addresses the discouraged practice of using a variable number of spaces to align certain tokens with previous lines.

### 4.6 Whitespace

#### 4.6.1 Vertical Whitespace

A single blank line always appears:

1. _Between_ consecutive members or initializers of a class: fields, constructors, methods, nested classes, static initializers, and instance initializers.
    * **Exception:** A blank line between two consecutive fields (having no other code between them) is optional. Such blank lines are used as needed to create _logical groupings_ of fields.</span>
    * **Exception:** Blank lines between enum constants are covered in [Enum classes](#481-enum-classes).</span>
2. As required by other sections of this document (such as [Source file structure](#3-source-file-structure) and [Import statements](#3.3-import-statements)).

A single blank line can also appear anywhere it improves readability, for example between statements to organize the code into logical subsections. A blank line before the first member or initializer, or after the last member or initializer of the class, is neither encouraged nor discouraged.

_Multiple_ consecutive blank lines are permitted, but never required (or encouraged).

#### 4.6.2 Horizontal whitespace

Beyond where required by the language or other style rules, and apart from literals, comments and Javadoc, a single ASCII space also appears in the following places **only**:

1. Separating any reserved word, such as `if`, `for` or `catch`, from an open parenthesis (`(`) that follows it on that line
2. Separating any reserved word, such as `else` or `catch`, from a closing curly brace (`}`) that precedes it on that line
3. Before any open curly brace (`{`), with two exceptions:
    * `@SomeAnnotation({a, b})` (no space is used)
    * `String[][] x = {{"foo"}};` (no space is required between `{{`, by item 8 below)
4. On both sides of any binary or ternary operator. This also applies to the following "operator-like" symbols:
    * the ampersand in a conjunctive type bound: `<T extends Foo & Bar>`
    * the pipe for a catch block that handles multiple exceptions: `catch (FooException | BarException e)`
    * the colon (`:`) in an enhanced `for` ("foreach") statement
    * the arrow in a lambda expression: `(String str) -> str.length()`

    **but not**

    * the two colons (`::`) of a method reference, which is written like `Object::toString`
    * the dot separator (`.`), which is written like `object.toString()`
5. After `,:;` or the closing parenthesis (`)`) of a cast
6. On both sides of the double slash (`//`) that begins an end-of-line comment. Here, multiple spaces are allowed, but not required
7. Between the type and variable of a declaration: `List<String> list`
8. _Optional_ just inside both braces of an array initializer
    * `new int[] {5, 6}` and `new int[] { 5, 6 }` are both valid
9. Between a type annotation and `[]` or `...`.

* :heavy_check_mark: **GOOD**

  ```java
  @SomeAnnotation({a, b})
  class D <T extends A & B & C> { /* ... */ }

  if (true) {
      ...
  } else { // If false.
      ...
  }
  ```

* :no_entry: **BAD**

  ```java
  @SomeAnnotation({ a,b })
  class D <T extends A&B&C> { /* ... */ }
  
  if(true){
      ...
  }else{//If false.
      ...
  }
  ```

This rule is never interpreted as requiring or forbidding additional space at the start or end of a line; it addresses only _interior_ space.

#### 4.6.3 Horizontal alignment: never required

**Terminology Note:** _Horizontal alignment_ is the practice of adding a variable number of additional spaces in your code with the goal of making certain tokens appear directly below certain other tokens on previous lines.

This practice is permitted, but is **never required**. It is not even required to _maintain_ horizontal alignment in places where it is already used.

Here is an example without alignment, then using alignment:

```java
private int x; // this is fine
private Color color; // this too

private int   x;      // permitted, but future edits
private Color color;  // may leave it unaligned
```

**Tip:** Alignment can aid readability, but it creates problems for future maintenance. Consider a future change that needs to touch just one line. This change can leave the formerly-pleasing formatting mangled, and that is **allowed**. More often it prompts the coder (perhaps you) to adjust whitespace on nearby lines as well, possibly triggering a cascading series of reformattings. That one-line change now has a "blast radius." This can at worst result in pointless busywork, but at best it still corrupts version history information, slows down reviewers and exacerbates merge conflicts.

### 4.7 Grouping parentheses: recommended

Optional grouping parentheses are omitted only when author and reviewer agree that there is no reasonable chance the code will be misinterpreted without them, nor would they have made the code easier to read. It is _not_ reasonable to assume that every reader has the entire Java operator precedence table memorized.

### 4.8 Specific constructs

#### 4.8.1 Enum classes

After each comma that follows an enum constant, a line break is optional. Additional blank lines (usually just one) are also allowed.

* :heavy_check_mark: **GOOD**

  ```java
  private enum Answer {
    YES {
        @Override
        public String toString() {
            return "yes";
        }
    },

    NO, MAYBE
  }
  ```

An enum class with no methods and no documentation on its constants can optionally be formatted as an [array initializer](#4831-array-initializers).

* :heavy_check_mark: **GOOD**

  ```java
  private enum Suit { CLUBS, HEARTS, SPADES, DIAMONDS }
  ```

Since enum classes _are classes_, all other rules for formatting classes apply.

#### 4.8.2 Variable declarations

##### 4.8.2.1 One variable per declaration

Every variable declaration (field or local) declares only one variable.

* :heavy_check_mark: **GOOD**

  ```java
  int a;
  int b;
  ```

* :no_entry: **BAD**

  ```java  
  int a, b;
  ```

**Exception:** Multiple variable declarations are acceptable in the header of a `for` loop.

##### 4.8.2.2 Declared when needed

Local variables are **not** habitually declared at the start of their containing block or block-like construct. Instead, local variables are declared close to the point they are first used (within reason), to minimize their scope. Local variable declarations typically have initializers, or are initialized immediately after declaration.

#### 4.8.3 Arrays

##### 4.8.3.1 Array initializers

Any array initializer can _optionally_ be formatted as a "block-like construct".

* :heavy_check_mark: **GOOD** (**not** an exhaustive list)

  ```java
  new int[] {
      0, 1, 2, 3
  }
  ```

  ```java
  new int[] {
      0,
      1,
      2,
      3
  }
  ```

  ```java
  new int[] {
      0, 1,
      2, 3
  }
  ```

  ```java
  new int[]
      { 0, 1, 2, 3 }
  ```

##### 4.8.3.2 No C-style array declarations

The square brackets form a part of the _type_, not the variable.

* :heavy_check_mark: **GOOD**

  ```java
  String[] args;
  ```

* :no_entry: **BAD**

  ```java  
  String args[];
  ```

#### 4.8.4 Switch statements

**Terminology Note:** Inside the braces of a _switch block_ are one or more _statement groups_. Each statement group consists of one or more _switch labels_ (either `case FOO:` or `default:`), followed by one or more statements (or, for the _last_ statement group, _zero_ or more statements).

##### 4.8.4.1 Indentation

As with any other block, the contents of a switch block are indented **+4**.

After a `switch` label, there is a **line break**, and the indentation level is increased **+4**, exactly as if a block is being opened. The following `switch` label returns to the previous indentation level, as if a block had been closed.

##### 4.8.4.2 Fall-through: commented

Within a switch block, each statement group either terminates abruptly (with a `break`, `continue`, `return` or thrown exception), or is marked with a comment to indicate that execution will or _might_ continue into the next statement group. Any comment that communicates the idea of fall-through is sufficient (typically `// fall through`). This special comment is not required in the last statement group of the switch block.

* :heavy_check_mark: **GOOD**

  ```java
  switch (input) {
      case 1:
      case 2:
        prepareOneOrTwo();
        // fall through
      case 3:
        handleOneTwoOrThree();
        break;
      default:
        handleLargeNumber(input);
  }
  ```

Notice that no comment is needed after `case 1:`, only at the end of the statement group.

##### 4.8.4.3 The `default` case is present

Each switch statement includes a `default` statement group, even if it contains no code.

**Exception:** A switch statement for an `enum` type _can_ omit the `default` statement group, _if_ it includes explicit cases covering _all_ possible values of that type. This enables IDEs or other static analysis tools to issue a warning if any cases are missed.

#### 4.8.5 Annotations

Annotations applying to a class, method or constructor appear immediately after the documentation block, and each annotation is listed on a line of its own (that is, one annotation per line). These line breaks do not constitute [4.5 Line-wrapping](#45-line-wrapping), and the indentation level is not increased.

* :heavy_check_mark: **GOOD**

  ```java
  @Override
  @Nullable
  public String getNameIfPresent() { ... }
  ```

* :no_entry: **BAD**

  ```java  
  @Override @Nullable
  public String getNameIfPresent() { ... }
  ```

**Exception:** A _single_ parameterless annotation **can** instead appear together with the first line of the signature

* :heavy_check_mark: **GOOD**

  ```java
  @Override public int hashCode() { ... }
  ```

Annotations applying to a field also appear immediately after the documentation block, but in this case, **_multiple_** annotations (possibly parameterized) can be listed on the same line; for example:

* :heavy_check_mark: **GOOD**

  ```java
  @Partial @Mock DataLoader loader;
  ```

There are no specific rules for formatting annotations on parameters, local variables, or types.

#### 4.8.6 Comments

This section addresses _implementation comments_. Javadoc is exhaustively described in [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html#s7-javadoc).

Any line break can be preceded by arbitrary whitespace followed by an implementation comment. Such a comment renders the line non-blank.

##### 4.8.6.1 Block comment style

Block comments are indented at the same level as the surrounding code. They can be in `/* ... */` style or `// ...` style. For multi-line `/* ... */` comments, subsequent lines must start with `*` aligned with the `*` on the previous line.

```java
/*
 * This is          // And so           /* Or you can
 * okay.            // is this.          * even do this. */
 */
```

Comments are not enclosed in boxes drawn with asterisks or other characters.

**Tip:** When writing multi-line comments, use the `/* ... */` style if you want automatic code formatters to re-wrap the lines when necessary (paragraph-style). Most formatters don not re-wrap lines in `// ...` style comment blocks.

#### 4.8.7 Modifiers

Class and member modifiers, when present, appear in the order recommended by the Java Language Specification:

```java
public protected private abstract default static final transient volatile synchronized native strictfp
```

#### 4.8.8 Numeric Literals

`long`-valued integer literals use an uppercase `L` suffix, never lowercase (to avoid confusion with the digit `1`). For example, `3000000000L` rather than `3000000000l`.

* :heavy_check_mark: **GOOD**: `3000000000L`
* :no_entry: **BAD**:`3000000000l`

## 5 Naming

### 5.1 Rules common to all identifiers

Identifiers use only ASCII letters and digits, and, in a small number of cases noted below, underscores. Thus each valid identifier name is matched by the regular expression `\w+` .

Special prefixes or suffixes are **not** used.

* :no_entry: **BAD**
  * `name_`
  * `mName`
  * `s_name`
  * `kName`

### 5.2 Rules by identifier type

#### 5.2.1 Package names

Package names are all lowercase, with consecutive words simply concatenated together (no underscores).

* :heavy_check_mark: **GOOD**

  ```java
  package com.example.deepspace;
  ```

* :no_entry: **BAD**

  ```java
  package com.example.deepSpace;
  ```

  ```java
  package com.example.deep_space;
  ```

#### 5.2.2 Class names

Class names are written in [UpperCamelCase](#53-camel-case-defined).

Class names are typically nouns or noun phrases. For example, `Character` or `ImmutableList`. Interface names can also be nouns or noun phrases (for example, `List`), but can sometimes be adjectives or adjective phrases instead (for example, `Readable`).

* :heavy_check_mark: **GOOD**

  ```java
  public class EntityCheck ...
  ```

* :no_entry: **BAD**

  ```java
  public class entityCheck ...
  ```

  ```java
  public class entity_check ...
  ```

There are no specific rules or even well-established conventions for naming annotation types.

_Test_ classes are named starting with the name of the class they are testing, and ending with `Test`. For example, `HashTest` or `HashIntegrationTest`.

#### 5.2.3 Method names

Method names are written in [lowerCamelCase](#53-camel-case-defined).

Method names are typically verbs or verb phrases. For example, `sendMessage` or `stop`.

* :heavy_check_mark: **GOOD**

  ```java
  public void sendMessage(String message) {}
  ```

* :no_entry: **BAD**

  ```java
  public void send_message(String message) {}
  ```

Underscores can appear in JUnit _test_ method names to separate logical components of the name, with _each_ component written in [lowerCamelCase](#53-camel-case-defined). One typical pattern is `_<methodUnderTest>___<state>_`, for example `pop_emptyStack`. There is no one correct way to name test methods.

#### 5.2.4 Constant names

Constant names use `CONSTANT_CASE`: all uppercase letters, with each word separated from the next by a single underscore. But what _is_ a constant, exactly?

Constants are **static final fields** whose contents are deeply **immutable** and whose methods have **no** detectable **side effects**. This includes primitives, Strings, immutable types, and immutable collections of immutable types. If any of the instance's observable state can change, it is not a constant. Merely _intending_ to never mutate the object is not enough.

* :heavy_check_mark: **Constants**

  ```java
  static final int NUMBER = 5;
  static final ImmutableList<String> NAMES = ImmutableList.of("Ed", "Ann");
  static final ImmutableMap<String, Integer> AGES = ImmutableMap.of("Ed", 35, "Ann", 32);
  static final Joiner COMMA_JOINER = Joiner.on(','); // because Joiner is immutable
  static final SomeMutableType[] EMPTY_ARRAY = {};
  enum SomeEnum { ENUM_CONSTANT }
  ```

* :warning: **Not constants**

  ```java
  static String nonFinal = "non-final";
  final String nonStatic = "non-static";
  static final Set<String> mutableCollection = new HashSet<String>();
  static final ImmutableSet<SomeMutableType> mutableElements = ImmutableSet.of(mutable);
  static final ImmutableMap<String, SomeMutableType> mutableValues =
    ImmutableMap.of("Ed", mutableInstance, "Ann", mutableInstance2);
  static final Logger logger = Logger.getLogger(MyClass.getName());
  static final String[] nonEmptyArray = {"these", "can", "change"};
  ```

These names are typically nouns or noun phrases.

#### 5.2.5 Non-constant field names

Non-constant field names (static or otherwise) are written in [lowerCamelCase](#53-camel-case-defined).

These names are typically nouns or noun phrases. For example, `computedValues` or `index`.

#### 5.2.6 Parameter names

Parameter names are written in [lowerCamelCase](#53-camel-case-defined).

One-character parameter names in public methods **must** be avoided.

* :heavy_check_mark: **GOOD**

  ```java
  public void someMethod(int parameterName) {}
  ```

* :no_entry: **BAD**

  ```java
  public void someMethod(int p) {}
  public void someMethod(int parameter_name) {}
  ```

#### 5.2.7 Local variable names

Local variable names are written in [lowerCamelCase](#53-camel-case-defined).

Even when final and immutable, local variables are not considered to be constants, and **must not** be styled as constants.

#### 5.2.8 Type variable names

Each type variable is named in one of two styles:

* A single capital letter, optionally followed by a single numeral (such as `E`, `T`, `X`, `T2`)
* A name in the form used for classes, see [5.2.2 Class names](#522-class-names), followed by the capital letter `T`:
  * `RequestT`
  * `FooBarT`

### 5.3 Camel case: defined

Sometimes there is more than one reasonable way to convert an English phrase into camel case, such as when acronyms or unusual constructs like `IPv6` or `iOS` are present. To improve predictability, the following (nearly) deterministic scheme must be used:

* Beginning with the prose form of the name

  1. Convert the phrase to plain ASCII and remove any apostrophes. For example, **"Müller's algorithm"** might become `Muellers algorithm`.
  2. Divide this result into words, splitting on spaces and any remaining punctuation (typically hyphens).
      * _Recommended:_ if any word already has a conventional camel-case appearance in common usage, split this into its constituent parts (e.g., **"AdWords"** becomes `ad words`). Note that a word such as `iOS` is not really in camel case _per se_; it defies _any_ convention and this recommendation does not apply.
  3. Now lowercase _everything_ (including acronyms), then uppercase only the first character of:
      * ... each word, to yield **upper camel case**, or
      * ... each word except the first, to yield **lower camel case**
  4. Finally, join all the words into a single identifier.

* Note that the casing of the original words is almost entirely disregarded.

  Examples:

    Prose form|:heavy_check_mark: **GOOD**|:no_entry: **BAD**
  ---|---|---
  XML HTTP request|`XmlHttpRequest`|`XMLHTTPRequest`
  new customer ID|`newCustomerId`|`newCustomerID`
  inner stopwatch|`innerStopwatch`|`innerStopWatch`
  supports IPv6 on iOS|`supportsIpv6OnIos`|`supportsIPv6OnIOS`
  YouTube importer|`YouTubeImporter`|`YoutubeImporter`<sup>[1](#notes)</sup>

#### Notes

1. Acceptable, but not recommended.
2. Some words are ambiguously hyphenated in the English language: for example **"nonempty"** and **"non-empty"** are both correct, and the method names `checkNonempty` and `checkNonEmpty` are likewise both correct.

## 6 Programming Practices

### 6.1 `@Override`: always used

A method is marked with the `@Override` annotation whenever it is legal. This includes a class method overriding a superclass method, a class method implementing an interface method, and an interface method respecifying a superinterface method.

**Exception:** `@Override` can be omitted when the parent method is `@Deprecated`.

### 6.2 Caught exceptions: not ignored

Except as noted below, it is very rarely correct to do nothing in response to a caught exception. Typical action is to log it, or if it is considered "impossible", rethrow it as an `AssertionError`.

When it truly is appropriate to take no action whatsoever in a catch block, the reason this is justified is explained in a comment.

* :heavy_check_mark: **GOOD**

  ```java
  try {
      int i = Integer.parseInt(response);
  } catch (NumberFormatException exception) {
      log.error(exception.getMessage());
  }
  ```

  ```java
  try {
      int i = Integer.parseInt(response);
  } catch (NumberFormatException exception) {
      // it's not numeric; that's fine, just continue
  }
  ```

* :no_entry: **BAD**

  ```java
  try {
      int i = Integer.parseInt(response);
  } catch (NumberFormatException exception) {

  }
  ```

**Exception:** In tests, a caught exception can be ignored without comment _if_ its name is or begins with **`expected`**. The following is a very common idiom for ensuring that the code under test _does_ throw an exception of the expected type and a comment is unnecessary here.

* :heavy_check_mark: **GOOD**

  ```java
  try {
      emptyStack.pop();
      fail();
  } catch (NoSuchElementException expected) {

  }
  ```

### 6.3 Static members: qualified using class

When a reference to a static class member must be qualified, it is qualified with name of that class, not with a reference or expression.

* :heavy_check_mark: **GOOD**

  ```java
  Foo aFoo = ...;
  Foo.aStaticMethod();
  ```

* :no_entry: **BAD**

  ```java
  Foo aFoo = ...;
  aFoo.aStaticMethod();
  somethingThatYieldsAFoo().aStaticMethod(); // VERY BAD
  ```

### 6.4 Finalizers: not used

It is **extremely rare** to override `Object.finalize`.

**Tip:** Do not do it. If you absolutely must, first read and understand [_Effective Java_ Item 7,](http://books.google.com/books?isbn=8131726592) "Avoid Finalizers," very carefully, and _then_ Do not do it.

### 6.5 Static members: never modify after initialization

* :heavy_check_mark: **GOOD**

  ```java
  private static final List<String[]> DATA = Collections.unmodifiableList(prepareData());
  ```

* :no_entry: **BAD**

  ```java
  private static final List<String[]> DATA = new ArrayList<>();

  @BeforeClass
  private void generateData() throws Exception {
      setData(); // Modifies DATA
  }
  ```
  
### 6.6 Multiple assertions in tests: allowed

Prefer several checks on the same object to splitting the methods and writing more configuration.

* :heavy_check_mark: **GOOD**

  ```java
  @Test
  public void testLastTimestamp(String minDate) {
      SeriesQuery query = QUERY.toBuilder().maxInsertDate(minDate).build();
      List<Series> seriesList = querySeriesAsList(query);
      assertTrue(seriesList.size() == 1 && !seriesList.get(0).getData().isEmpty(), "Output series is empty");
  }
  ```

* :no_entry: **BAD**

  ```java
  @Test (groups = "count")
  public void testCountSeries(String maxDate) {
      SeriesQuery query = QUERY.toBuilder().maxInsertDate(maxDate).build();
      List<Series> seriesList = querySeriesAsList(query);
      assertTrue(seriesList.size() == 1, "Wrong count of series");
  }

  @Test (dependsOnGroups = "count")
  public void testLastTimestamp(String minDate) {
      SeriesQuery query = QUERY.toBuilder().maxInsertDate(minDate).build();
      List<Series> seriesList = querySeriesAsList(query);
      assertFalse(seriesList.isEmpty() || seriesList.get(0).getData().isEmpty(), "Output series is empty");
  }
  ```
  
### 6.7 Number objects: avoid

Prefer primitive values to numeric objects. If number is nullable, this fact must be documented explicitly.

* :heavy_check_mark: **GOOD**

  ```java
  final int count = 1;
  ```
  
* :heavy_check_mark: **GOOD**

  ```java
  public class Response {
      Integer value; // JSON contains either value or error 
      String error
  }
  ```

* :no_entry: **BAD**

  ```java
  final Integer count = 1;
  ```

