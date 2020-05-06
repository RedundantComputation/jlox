package craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static craftinginterpreters.lox.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    // Picked up a division operator instead.
                    addToken(SLASH);
                }
                break;
            // TODO: Consider adding support for multi-line comments (CHALLENGE section 4)

            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;

            case '"': string(); break;

            /* TODO: This shotguns the user with a blast of error messages, can you
                    find a way to generate a single error for nicer UX? (section 4.5.1.)
             */
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        // See if the identifier is a reserved word.
        String text = source.substring(start, current);

        TokenType type = keywords.get(text);
        if (type == null)
            type = IDENTIFIER;

        addToken(type);
    }

    private void number() {
        while (isDigit(peek()))
            advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek()))
                advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // Handles string tokens
    // TODO: Consider unescaping escape sequences? (section 4.6.1)
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line ++;
            advance();
        }

        // Unterminated string.
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotation marks.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING,value);
    }

    // Conditional advance; only consumes current character if expected is value, we uhh expect
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current ++;
        return true;
    }

    // Advance but does not consume character (does lookahead)
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    // Lookahead helper for second character in input stream
    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /* Defined our own isDigit helper to avoid the Java standard library method
        which hilariously enough allows for Devanagari digits
    */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Helper to see if we've reached end of source file
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Consumes the next character in the source file and returns it
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    // Grabs the text of the current lexeme and creates a new token for it
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // Overloaded method for adding tokens with literal values
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
