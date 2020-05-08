package craftinginterpreters.lox;

import java.util.List;

import static craftinginterpreters.lox.TokenType.*;

class Parser {
    // Sentinel class used to unwind the parser
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    /* TODO: Unify redundant code using helper that takes a list of token types and an
             operand method handle (section 6.2.1)
    */
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right  = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    // checks to see if current token is any of the given types
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    // Checks to see if next token is of expected type. If not, throws error
    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    // returns true if current token is of given type (doesn't consume token, only looks at it)
    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    // consumes current token and returns it
    private Token advance() {
        if (!isAtEnd())
            current ++;
        return previous();
    }

    // checks if we've run out of tokens to parse
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    // returns the current token we have yet to consume
    private Token peek() {
        return tokens.get(current);
    }

    // returns most recently consumed token
    private Token previous() {
        return tokens.get(current - 1);
    }

    // detects parse errors and signals error message to be returned through lox
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        /* returns ParseError instead of throwing ParseError so that caller can
            can decide if they want to unwind error or not
        */
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            // semi-colon found, synchronize by returning
            if (previous().type == SEMICOLON)
                return;

            // discard tokens until we hit a semi-colon and synchronize
            switch(peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}