# Local testing of FluentBit

## Installation

```bash
brew install fluent-bit
```

## Execute

```bash
fluent-bit -c fluent-bit.conf
```

Note: You need to abort this with Ctrl C. There's the option exit_on_eof, however this will lead to empty results when using multiline parsers.
