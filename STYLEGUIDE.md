# Axibase Style Guide for Java Code

## 1 Introduction

This document serves as the definition of Axibase coding standards for source code in the Java™ Programming Language. It's mostly based on [Google Code Style](https://google.github.io/styleguide/javaguide.html) with some stricter rules covering tests implementation.

### 1.1 Terminology notes

In this document, unless otherwise clarified:

1.  The term _class_ is used inclusively to mean an "ordinary" class, enum class, interface or annotation type (`@interface`).
2.  The term _member_ (of a class) is used inclusively to mean a nested class, field, method, _or constructor_; that is, all top-level contents of a class except initializers and comments.
3.  The term _comment_ always refers to _implementation_ comments. We do not use the phrase "documentation comments", instead using the common term "Javadoc."

Other "terminology notes" will appear occasionally throughout the document.

## 2 Source file basics

### 2.1 File name

The source file name consists of the case-sensitive name of the top-level class it contains (of which there is [exactly one](#s3.4.1-one-top-level-class)), plus the `.java` extension.

### 2.2 File encoding: UTF-8

Source files are encoded in **UTF-8**.

### 2.3 Special characters

#### 2.3.1 Whitespace characters

Aside from the line terminator sequence, the **ASCII horizontal space character** (**0x20**) is the only whitespace character that appears anywhere in a source file. This implies that:

1.  All other whitespace characters in string and character literals are escaped.
2.  Tab characters are **not** used for indentation.

#### 2.3.2 Special escape sequences

For any character that has a [special escape sequence](http://docs.oracle.com/javase/tutorial/java/data/characters.html) (`\b`, `\t`, `\n`, `\f`, `\r`, `\"`, `\'` and `\\`), that sequence is used rather than the corresponding octal (e.g. `\012`) or Unicode (e.g. `\u000a`) escape.

#### 2.3.3 Non-ASCII characters

For the remaining non-ASCII characters, either the actual Unicode character (e.g. `∞`) or the equivalent Unicode escape (e.g. `\u221e`) is used. The choice depends only on which makes the code **easier to read and understand**, although Unicode escapes outside string literals and comments are strongly discouraged.

**Tip:** In the Unicode escape case, and occasionally even when actual Unicode characters are used, an explanatory comment can be very helpful.

Examples:

<table>

<tbody>

<tr>

<th>Example</th>

<th>Discussion</th>

</tr>

<tr>

<td>`String unitAbbrev = "μs";`</td>

<td>Best: perfectly clear even without a comment.</td>

</tr>

<tr>

<td>`String unitAbbrev = "\u03bcs"; // "μs"`</td>

<td>Allowed, but there's no reason to do this.</td>

</tr>

<tr>

<td>`String unitAbbrev = "\u03bcs"; // Greek letter mu, "s"`</td>

<td>Allowed, but awkward and prone to mistakes.</td>

</tr>

<tr>

<td>`String unitAbbrev = "\u03bcs";`</td>

<td>Poor: the reader has no idea what this is.</td>

</tr>

<tr>

<td>`return '\ufeff' + content; // byte order mark`</td>

<td>Good: use escapes for non-printable characters, and comment if necessary.</td>

</tr>

</tbody>

</table>

**Tip:** Never make your code less readable simply out of fear that some programs might not handle non-ASCII characters properly. If that should happen, those programs are **broken** and they must be **fixed**.

## 3 Source file structure

<div>

A source file consists of, **in order**:

1.  License or copyright information, if present
2.  Package statement
3.  Import statements
4.  Exactly one top-level class

</div>

**Exactly one blank line** separates each section that is present.

IDE-generated header with information about the author **must be removed**.

### 3.1 License or copyright information, if present

If license or copyright information belongs in a file, it belongs here.

### 3.2 Package statement

The package statement is **not line-wrapped**. The column limit (Section 4.4, [Column limit: 100](#s4.4-column-limit)) does not apply to package statements.

### 3.3 Import statements

#### 3.3.1 Wildcard imports

**Wildcard imports**, opposite to [Google style guide](https://google.github.io/styleguide/javaguide.html), **are allowed**.

#### 3.3.2 No line-wrapping

Import statements are **not line-wrapped**. The column limit (Section 4.4, [Column limit: 100](#s4.4-column-limit)) does not apply to import statements.

#### 3.3.3 Ordering and spacing

Imports are ordered as follows:

1.  All static imports in a single block.
2.  All non-static imports in a single block.

If there are both static and non-static imports, a single blank line separates the two blocks. There are no other blank lines between import statements.

Within each block the imported names appear in ASCII sort order. (**Note:** this is not the same as the import _statements_ being in ASCII sort order, since '.' sorts before ';'.)

#### 3.3.4 No static import for classes

Static import is not used for static nested classes. They are imported with normal imports.

### 3.3.5 Prefer static imports for Assert.* methods, non-static imports otherwise.

Bad:

<pre class="prettyprint lang-java">Assert.assertEquals(code, 0);
List<String> list = asList("a", "b", "c");
</pre>

Good:

<pre class="prettyprint lang-java">assertEquals(code, 0);
List<String> list = Arrays.asList("a", "b", "c");
</pre>

### 3.3.6 Unused imports must be deleted

All imported classes must be used

### 3.4 Class declaration

#### 3.4.1 Exactly one top-level class declaration

Each top-level class resides in a source file of its own.

#### 3.4.2 Ordering of class contents

The order you choose for the members and initializers of your class can have a great effect on learnability. However, there's no single correct recipe for how to do it; different classes may order their contents in different ways.

What is important is that each class uses **_some_ logical order**, which its maintainer could explain if asked. For example, new methods are not just habitually added to the end of the class, as that would yield "chronological by date added" ordering, which is not a logical ordering.

##### 3.4.2.1 Overloads: never split

When a class has multiple constructors, or multiple methods with the same name, these appear sequentially, with no other code in between (not even private members).

##### 3.4.2.2. Private methods placement

The private method must be placed under the first method it is called from. Multiple private methods used in one place must be placed in order of usage.

## 4 Formatting

**Terminology Note:** _block-like construct_ refers to the body of a class, method or constructor. Note that, by Section 4.8.3.1 on [array initializers](#s4.8.3.1-array-initializers), any array initializer _may_ optionally be treated as if it were a block-like construct.

### 4.1 Braces

#### 4.1.1 Braces are used where optional

Braces are used with `if`, `else`, `for`, `do` and `while` statements, even when the body is empty or contains only a single statement.

#### 4.1.2 Nonempty blocks: K & R style

Braces follow the Kernighan and Ritchie style ("[Egyptian brackets](http://www.codinghorror.com/blog/2012/07/new-programming-jargon.html)") for _nonempty_ blocks and block-like constructs:

*   No line break before the opening brace.
*   Line break after the opening brace.
*   Line break before the closing brace.
*   Line break after the closing brace, _only if_ that brace terminates a statement or terminates the body of a method, constructor, or _named_ class. For example, there is _no_ line break after the brace if it is followed by `else` or a comma.

Examples:

<pre class="prettyprint lang-java">return () -> {
  while (condition()) {
    method();
  }
};

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
</pre>

A few exceptions for enum classes are given in Section 4.8.1, [Enum classes](#s4.8.1-enum-classes).

#### 4.1.3 Empty blocks: may be concise

An empty block or block-like construct may be in K & R style (as described in [Section 4.1.2](#s4.1.2-blocks-k-r-style)). Alternatively, it may be closed immediately after it is opened, with no characters or line break in between (`{}`), **unless** it is part of a _multi-block statement_ (one that directly contains multiple blocks: `if/else` or `try/catch/finally`).

Examples:

<pre class="prettyprint lang-java">  // This is acceptable
  void doNothing() {}

  // This is equally acceptable
  void doNothingElse() {
  }
</pre>

<pre class="prettyprint lang-java badcode">  // This is not acceptable: No concise empty blocks in a multi-block statement
  try {
    doSomething();
  } catch (Exception e) {}
</pre>

### 4.2 Block indentation: +4 spaces

Each time a new block or block-like construct is opened, the indent increases by two spaces. When the block ends, the indent returns to the previous indent level. The indent level applies to both code and comments throughout the block. (See the example in Section 4.1.2, [Nonempty blocks: K & R Style](#s4.1.2-blocks-k-r-style).)

### 4.3 One statement per line

Each statement is followed by a line break.

### 4.4 Column limit: 100

Java code has a column limit of 100 characters. A "character" means any Unicode code point. Except as noted below, any line that would exceed this limit must be line-wrapped, as explained in Section 4.5, [Line-wrapping](#s4.5-line-wrapping).

Each Unicode code point counts as one character, even if its display width is greater or less. For example, if using [fullwidth characters](https://en.wikipedia.org/wiki/Halfwidth_and_fullwidth_forms), you may choose to wrap the line earlier than where this rule strictly requires.

**Exceptions:**

1.  Lines where obeying the column limit is not possible (for example, a long URL in Javadoc, or a long JSNI method reference).
2.  `package` and `import` statements (see Sections 3.2 [Package statement](#s3.2-package-statement) and 3.3 [Import statements](#s3.3-import-statements)).
3.  Command lines in a comment that may be cut-and-pasted into a shell.

### 4.5 Line-wrapping

**Terminology Note:** When code that might otherwise legally occupy a single line is divided into multiple lines, this activity is called _line-wrapping_.

There is no comprehensive, deterministic formula showing _exactly_ how to line-wrap in every situation. Very often there are several valid ways to line-wrap the same piece of code.

**Note:** While the typical reason for line-wrapping is to avoid overflowing the column limit, even code that would in fact fit within the column limit _may_ be line-wrapped at the author's discretion.

**Tip:** Extracting a method or local variable may solve the problem without the need to line-wrap.

#### 4.5.1 Where to break

The prime directive of line-wrapping is: prefer to break at a **higher syntactic level**. Also:

1.  When a line is broken at a _non-assignment_ operator the break comes _before_ the symbol.
    *   This also applies to the following "operator-like" symbols:
        *   the dot separator (`.`)
        *   the two colons of a method reference (`::`)
        *   an ampersand in a type bound (`<T extends Foo & Bar>`)
        *   a pipe in a catch block (`catch (FooException | BarException e)`).
2.  When a line is broken at an _assignment_ operator the break typically comes _after_ the symbol, but either way is acceptable.
    *   This also applies to the "assignment-operator-like" colon in an enhanced `for` ("foreach") statement.
3.  A method or constructor name stays attached to the open parenthesis (`(`) that follows it.
4.  A comma (`,`) stays attached to the token that precedes it.
5.  A line is never broken adjacent to the arrow in a lambda, except that a break may come immediately after the arrow if the body of the lambda consists of a single unbraced expression. Examples:

    <pre class="prettyprint lang-java">MyLambda<String, Long, Object> lambda =
        (String label, Long value, Object obj) -> {
            ...
        };

    Predicate<String> predicate = str ->
        longExpressionInvolving(str);
    </pre>

**Note:** The primary goal for line wrapping is to have clear code, _not necessarily_ code that fits in the smallest number of lines.

#### 4.5.2 Indent continuation lines at least +8 spaces

When line-wrapping, each line after the first (each _continuation line_) is indented at least +8 from the original line.

When there are multiple continuation lines, indentation may be varied beyond +8 as desired. In general, two continuation lines use the same indentation level if and only if they begin with syntactically parallel elements.

Section 4.6.3 on [Horizontal alignment](#s4.6.3-horizontal-alignment) addresses the discouraged practice of using a variable number of spaces to align certain tokens with previous lines.

### 4.6 Whitespace

#### 4.6.1 Vertical Whitespace

A single blank line always appears:

1.  _Between_ consecutive members or initializers of a class: fields, constructors, methods, nested classes, static initializers, and instance initializers.
    *   <span class="exception">**Exception:** A blank line between two consecutive fields (having no other code between them) is optional. Such blank lines are used as needed to create _logical groupings_ of fields.</span>
    *   <span class="exception">**Exception:** Blank lines between enum constants are covered in [Section 4.8.1](#s4.8.1-enum-classes).</span>
2.  As required by other sections of this document (such as Section 3, [Source file structure](#s3-source-file-structure), and Section 3.3, [Import statements](#s3.3-import-statements)).

A single blank line may also appear anywhere it improves readability, for example between statements to organize the code into logical subsections. A blank line before the first member or initializer, or after the last member or initializer of the class, is neither encouraged nor discouraged.

_Multiple_ consecutive blank lines are permitted, but never required (or encouraged).

#### 4.6.2 Horizontal whitespace

Beyond where required by the language or other style rules, and apart from literals, comments and Javadoc, a single ASCII space also appears in the following places **only**.

1.  Separating any reserved word, such as `if`, `for` or `catch`, from an open parenthesis (`(`) that follows it on that line
2.  Separating any reserved word, such as `else` or `catch`, from a closing curly brace (`}`) that precedes it on that line
3.  Before any open curly brace (`{`), with two exceptions:
    *   `@SomeAnnotation({a, b})` (no space is used)
    *   `String[][] x = {{"foo"}};` (no space is required between `{{`, by item 8 below)
4.  On both sides of any binary or ternary operator. This also applies to the following "operator-like" symbols:
    *   the ampersand in a conjunctive type bound: `<T extends Foo & Bar>`
    *   the pipe for a catch block that handles multiple exceptions: `catch (FooException | BarException e)`
    *   the colon (`:`) in an enhanced `for` ("foreach") statement
    *   the arrow in a lambda expression: `(String str) -> str.length()`
    
    but not
    *   the two colons (`::`) of a method reference, which is written like `Object::toString`
    *   the dot separator (`.`), which is written like `object.toString()`
5.  After `,:;` or the closing parenthesis (`)`) of a cast
6.  On both sides of the double slash (`//`) that begins an end-of-line comment. Here, multiple spaces are allowed, but not required.
7.  Between the type and variable of a declaration: `List<String> list`
8.  _Optional_ just inside both braces of an array initializer
    *   `new int[] {5, 6}` and `new int[] { 5, 6 }` are both valid
9.  Between a type annotation and `[]` or `...`.

This rule is never interpreted as requiring or forbidding additional space at the start or end of a line; it addresses only _interior_ space.

#### 4.6.3 Horizontal alignment: never required

**Terminology Note:** _Horizontal alignment_ is the practice of adding a variable number of additional spaces in your code with the goal of making certain tokens appear directly below certain other tokens on previous lines.

This practice is permitted, but is **never required**. It is not even required to _maintain_ horizontal alignment in places where it was already used.

Here is an example without alignment, then using alignment:

<pre class="prettyprint lang-java">private int x; // this is fine
private Color color; // this too

private int   x;      // permitted, but future edits
private Color color;  // may leave it unaligned
</pre>

**Tip:** Alignment can aid readability, but it creates problems for future maintenance. Consider a future change that needs to touch just one line. This change may leave the formerly-pleasing formatting mangled, and that is **allowed**. More often it prompts the coder (perhaps you) to adjust whitespace on nearby lines as well, possibly triggering a cascading series of reformattings. That one-line change now has a "blast radius." This can at worst result in pointless busywork, but at best it still corrupts version history information, slows down reviewers and exacerbates merge conflicts.

### 4.7 Grouping parentheses: recommended

Optional grouping parentheses are omitted only when author and reviewer agree that there is no reasonable chance the code will be misinterpreted without them, nor would they have made the code easier to read. It is _not_ reasonable to assume that every reader has the entire Java operator precedence table memorized.

### 4.8 Specific constructs

#### 4.8.1 Enum classes

After each comma that follows an enum constant, a line break is optional. Additional blank lines (usually just one) are also allowed. This is one possibility:

<pre class="prettyprint lang-java">private enum Answer {
  YES {
    @Override public String toString() {
      return "yes";
    }
  },

  NO,
  MAYBE
}
</pre>

An enum class with no methods and no documentation on its constants may optionally be formatted as if it were an array initializer (see Section 4.8.3.1 on [array initializers](#s4.8.3.1-array-initializers)).

<pre class="prettyprint lang-java">private enum Suit { CLUBS, HEARTS, SPADES, DIAMONDS }
</pre>

Since enum classes _are classes_, all other rules for formatting classes apply.

#### 4.8.2 Variable declarations

##### 4.8.2.1 One variable per declaration

Every variable declaration (field or local) declares only one variable: declarations such as `int a, b;` are not used.

**Exception:** Multiple variable declarations are acceptable in the header of a `for` loop.

##### 4.8.2.2 Declared when needed

Local variables are **not** habitually declared at the start of their containing block or block-like construct. Instead, local variables are declared close to the point they are first used (within reason), to minimize their scope. Local variable declarations typically have initializers, or are initialized immediately after declaration.

#### 4.8.3 Arrays

##### 4.8.3.1 Array initializers: can be "block-like"

Any array initializer may _optionally_ be formatted as if it were a "block-like construct." For example, the following are all valid (**not** an exhaustive list):

<pre class="prettyprint lang-java">new int[] {           new int[] {
  0, 1, 2, 3            0,
}                       1,
                        2,
new int[] {             3,
  0, 1,               }
  2, 3
}                     new int[]
                          {0, 1, 2, 3}
</pre>

##### 4.8.3.2 No C-style array declarations

The square brackets form a part of the _type_, not the variable: `String[] args`, not `String args[]`.

#### 4.8.4 Switch statements

**Terminology Note:** Inside the braces of a _switch block_ are one or more _statement groups_. Each statement group consists of one or more _switch labels_ (either `case FOO:` or `default:`), followed by one or more statements (or, for the _last_ statement group, _zero_ or more statements).

##### 4.8.4.1 Indentation

As with any other block, the contents of a switch block are indented +4.

After a switch label, there is a line break, and the indentation level is increased +4, exactly as if a block were being opened. The following switch label returns to the previous indentation level, as if a block had been closed.

##### 4.8.4.2 Fall-through: commented

Within a switch block, each statement group either terminates abruptly (with a `break`, `continue`, `return` or thrown exception), or is marked with a comment to indicate that execution will or _might_ continue into the next statement group. Any comment that communicates the idea of fall-through is sufficient (typically `// fall through`). This special comment is not required in the last statement group of the switch block. Example:

<pre class="prettyprint lang-java">switch (input) {
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
</pre>

Notice that no comment is needed after `case 1:`, only at the end of the statement group.

##### 4.8.4.3 The `default` case is present

Each switch statement includes a `default` statement group, even if it contains no code.

**Exception:** A switch statement for an `enum` type _may_ omit the `default` statement group, _if_ it includes explicit cases covering _all_ possible values of that type. This enables IDEs or other static analysis tools to issue a warning if any cases were missed.

#### 4.8.5 Annotations

Annotations applying to a class, method or constructor appear immediately after the documentation block, and each annotation is listed on a line of its own (that is, one annotation per line). These line breaks do not constitute line-wrapping (Section 4.5, [Line-wrapping](#s4.5-line-wrapping)), so the indentation level is not increased. Example:

<pre class="prettyprint lang-java">@Override
@Nullable
public String getNameIfPresent() { ... }
</pre>

**Exception:** A _single_ parameterless annotation _may_ instead appear together with the first line of the signature, for example:

<pre class="prettyprint lang-java">@Override public int hashCode() { ... }
</pre>

Annotations applying to a field also appear immediately after the documentation block, but in this case, _multiple_ annotations (possibly parameterized) may be listed on the same line; for example:

<pre class="prettyprint lang-java">@Partial @Mock DataLoader loader;
</pre>

There are no specific rules for formatting annotations on parameters, local variables, or types.

#### 4.8.6 Comments

This section addresses _implementation comments_. Javadoc is addressed separately in Section 7, [Javadoc](#s7-javadoc).

Any line break may be preceded by arbitrary whitespace followed by an implementation comment. Such a comment renders the line non-blank.

##### 4.8.6.1 Block comment style

Block comments are indented at the same level as the surrounding code. They may be in `/* ... */` style or `// ...` style. For multi-line `/* ... */` comments, subsequent lines must start with `*` aligned with the `*` on the previous line.

<pre class="prettyprint lang-java">/*
 * This is          // And so           /* Or you can
 * okay.            // is this.          * even do this. */
 */
</pre>

Comments are not enclosed in boxes drawn with asterisks or other characters.

**Tip:** When writing multi-line comments, use the `/* ... */` style if you want automatic code formatters to re-wrap the lines when necessary (paragraph-style). Most formatters don't re-wrap lines in `// ...` style comment blocks.

#### 4.8.7 Modifiers

Class and member modifiers, when present, appear in the order recommended by the Java Language Specification:

<pre>public protected private abstract default static final transient volatile synchronized native strictfp
</pre>

#### 4.8.8 Numeric Literals

`long`-valued integer literals use an uppercase `L` suffix, never lowercase (to avoid confusion with the digit `1`). For example, `3000000000L` rather than `3000000000l`.

## 5 Naming

### 5.1 Rules common to all identifiers

Identifiers use only ASCII letters and digits, and, in a small number of cases noted below, underscores. Thus each valid identifier name is matched by the regular expression `\w+` .

Special prefixes or suffixes are **not** used, for example, these names are prohibited: `name_`, `mName`, `s_name` and `kName`.

### 5.2 Rules by identifier type

#### 5.2.1 Package names

Package names are all lowercase, with consecutive words simply concatenated together (no underscores). For example, `com.example.deepspace`, not `com.example.deepSpace` or `com.example.deep_space`.

#### 5.2.2 Class names

Class names are written in [UpperCamelCase](#s5.3-camel-case).

Class names are typically nouns or noun phrases. For example, `Character` or `ImmutableList`. Interface names may also be nouns or noun phrases (for example, `List`), but may sometimes be adjectives or adjective phrases instead (for example, `Readable`).

There are no specific rules or even well-established conventions for naming annotation types.

_Test_ classes are named starting with the name of the class they are testing, and ending with `Test`. For example, `HashTest` or `HashIntegrationTest`.

#### 5.2.3 Method names

Method names are written in [lowerCamelCase](#s5.3-camel-case).

Method names are typically verbs or verb phrases. For example, `sendMessage` or `stop`.

Underscores may appear in JUnit _test_ method names to separate logical components of the name, with _each_ component written in [lowerCamelCase](#s5.3-camel-case). One typical pattern is `_<methodUnderTest>___<state>_`, for example `pop_emptyStack`. There is no One Correct Way to name test methods.

#### 5.2.4 Constant names

Constant names use `CONSTANT_CASE`: all uppercase letters, with each word separated from the next by a single underscore. But what _is_ a constant, exactly?

Constants are static final fields whose contents are deeply immutable and whose methods have no detectable side effects. This includes primitives, Strings, immutable types, and immutable collections of immutable types. If any of the instance's observable state can change, it is not a constant. Merely _intending_ to never mutate the object is not enough. Examples:

<pre class="prettyprint lang-java">// Constants
static final int NUMBER = 5;
static final ImmutableList<String> NAMES = ImmutableList.of("Ed", "Ann");
static final ImmutableMap<String, Integer> AGES = ImmutableMap.of("Ed", 35, "Ann", 32);
static final Joiner COMMA_JOINER = Joiner.on(','); // because Joiner is immutable
static final SomeMutableType[] EMPTY_ARRAY = {};
enum SomeEnum { ENUM_CONSTANT }

// Not constants
static String nonFinal = "non-final";
final String nonStatic = "non-static";
static final Set<String> mutableCollection = new HashSet<String>();
static final ImmutableSet<SomeMutableType> mutableElements = ImmutableSet.of(mutable);
static final ImmutableMap<String, SomeMutableType> mutableValues =
    ImmutableMap.of("Ed", mutableInstance, "Ann", mutableInstance2);
static final Logger logger = Logger.getLogger(MyClass.getName());
static final String[] nonEmptyArray = {"these", "can", "change"};
</pre>

These names are typically nouns or noun phrases.

#### 5.2.5 Non-constant field names

Non-constant field names (static or otherwise) are written in [lowerCamelCase](#s5.3-camel-case).

These names are typically nouns or noun phrases. For example, `computedValues` or `index`.

#### 5.2.6 Parameter names

Parameter names are written in [lowerCamelCase](#s5.3-camel-case).

One-character parameter names in public methods should be avoided.

#### 5.2.7 Local variable names

Local variable names are written in [lowerCamelCase](#s5.3-camel-case).

Even when final and immutable, local variables are not considered to be constants, and should not be styled as constants.

#### 5.2.8 Type variable names

Each type variable is named in one of two styles:

*   A single capital letter, optionally followed by a single numeral (such as `E`, `T`, `X`, `T2`)
*   A name in the form used for classes (see Section 5.2.2, [Class names](#s5.2.2-class-names)), followed by the capital letter `T` (examples: `RequestT`, `FooBarT`).

### 5.3 Camel case: defined

Sometimes there is more than one reasonable way to convert an English phrase into camel case, such as when acronyms or unusual constructs like "IPv6" or "iOS" are present. To improve predictability, the following (nearly) deterministic scheme should be used.

Beginning with the prose form of the name:

1.  Convert the phrase to plain ASCII and remove any apostrophes. For example, "Müller's algorithm" might become "Muellers algorithm".
2.  Divide this result into words, splitting on spaces and any remaining punctuation (typically hyphens).
    *   _Recommended:_ if any word already has a conventional camel-case appearance in common usage, split this into its constituent parts (e.g., "AdWords" becomes "ad words"). Note that a word such as "iOS" is not really in camel case _per se_; it defies _any_ convention, so this recommendation does not apply.
3.  Now lowercase _everything_ (including acronyms), then uppercase only the first character of:
    *   ... each word, to yield _upper camel case_, or
    *   ... each word except the first, to yield _lower camel case_
4.  Finally, join all the words into a single identifier.

Note that the casing of the original words is almost entirely disregarded. Examples:

<table>

<tbody>

<tr>

<th>Prose form</th>

<th>Correct</th>

<th>Incorrect</th>

</tr>

<tr>

<td>"XML HTTP request"</td>

<td>`XmlHttpRequest`</td>

<td>`XMLHTTPRequest`</td>

</tr>

<tr>

<td>"new customer ID"</td>

<td>`newCustomerId`</td>

<td>`newCustomerID`</td>

</tr>

<tr>

<td>"inner stopwatch"</td>

<td>`innerStopwatch`</td>

<td>`innerStopWatch`</td>

</tr>

<tr>

<td>"supports IPv6 on iOS?"</td>

<td>`supportsIpv6OnIos`</td>

<td>`supportsIPv6OnIOS`</td>

</tr>

<tr>

<td>"YouTube importer"</td>

<td>`YouTubeImporter`  
`YoutubeImporter`*</td>

<td></td>

</tr>

</tbody>

</table>

*Acceptable, but not recommended.

**Note:** Some words are ambiguously hyphenated in the English language: for example "nonempty" and "non-empty" are both correct, so the method names `checkNonempty` and `checkNonEmpty` are likewise both correct.

## 6 Programming Practices

### 6.1 `@Override`: always used

A method is marked with the `@Override` annotation whenever it is legal. This includes a class method overriding a superclass method, a class method implementing an interface method, and an interface method respecifying a superinterface method.

**Exception:** `@Override` may be omitted when the parent method is `@Deprecated`.

### 6.2 Caught exceptions: not ignored

Except as noted below, it is very rarely correct to do nothing in response to a caught exception. (Typical responses are to log it, or if it is considered "impossible", rethrow it as an `AssertionError`.)

When it truly is appropriate to take no action whatsoever in a catch block, the reason this is justified is explained in a comment.

<pre class="prettyprint lang-java">try {
  int i = Integer.parseInt(response);
  return handleNumericResponse(i);
} catch (NumberFormatException ok) {
  // it's not numeric; that's fine, just continue
}
return handleTextResponse(response);
</pre>

**Exception:** In tests, a caught exception may be ignored without comment _if_ its name is or begins with `expected`. The following is a very common idiom for ensuring that the code under test _does_ throw an exception of the expected type, so a comment is unnecessary here.

<pre class="prettyprint lang-java">try {
  emptyStack.pop();
  fail();
} catch (NoSuchElementException expected) {
}
</pre>

### 6.3 Static members: qualified using class

When a reference to a static class member must be qualified, it is qualified with that class's name, not with a reference or expression of that class's type.

<pre class="prettyprint lang-java">Foo aFoo = ...;
Foo.aStaticMethod(); // good
<span class="badcode">aFoo.aStaticMethod();</span> // bad
<span class="badcode">somethingThatYieldsAFoo().aStaticMethod();</span> // very bad
</pre>

### 6.4 Finalizers: not used

It is **extremely rare** to override `Object.finalize`.

**Tip:** Don't do it. If you absolutely must, first read and understand [_Effective Java_ Item 7,](http://books.google.com/books?isbn=8131726592) "Avoid Finalizers," very carefully, and _then_ don't do it.

### 6.5 Static members: never modify after initialization

Bad:

<pre class="prettyprint lang-java">private static final List<String[]> DATA = new ArrayList<>();

@BeforeClass
private void generateData() throws Exception {
    setData();
}
</pre>

Good:

<pre class="prettyprint lang-java">private static final List<String[]> DATA = Collections.unmodifiableList(prepareData());
</pre>