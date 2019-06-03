/*
    Parser.java

    This class implements a recursive-descent parser,
    and is used as the 'brain' of the calculator.

    Algorithm adapted from public domain code here:

    https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form

    Precedence follows tiers, evaluating by climbing the tree
    bottom to top, (e.g. highest tier to lowest tier)
        1) Addition, Subtraction
        2) Multiplication, Division, Modulo
        3) Exponentiation, nth roots
        4) Unary functions, parenthesis/groups
 */
package dev.StylishNerds.GraphingCalculator;

import java.util.HashMap;
import java.util.function.DoubleUnaryOperator;

public class Parser {

    // instance variables
    private final HashMap<String, DoubleUnaryOperator> map;
    private HashMap<String, Double> vars;
    private int pos;    // keep track of our position in the string
    private int val;    // keep track of the last char we consumed
    private String input;

    /**
     * Constructor for our expression object, requires
     * a hashmap to use for looking up operations
     */
    public Parser() {
        this.map = new HashMap<>();
        initFuncMap();
        reset();
    }

    /**
     * Populate our function map to lookup
     * actual math functions, as DoubleUnaryOperations,
     * based on input string/character.
     */
    private void initFuncMap() {
        map.put("sin", Math::sin);
        map.put("cos", Math::cos);
        map.put("tan", Math::tan);
        map.put("asin", Math::asin);
        map.put("acos", Math::acos);
        map.put("atan", Math::atan);
        map.put("sqrt", Math::sqrt);
        map.put("√", Math::sqrt);
        map.put("log", Math::log);
        map.put("exp", Math::exp);
        map.put("sec", (val) -> (1.0 / Math.cos(val)));
        map.put("csc", (val) -> (1.0 / Math.sin(val)));
        map.put("cot", (val) -> (1.0 / Math.tan(val)));
    }

    /**
     * advance our parser to look at the
     * next character in the expression
     */
    private void next() {
        val = (++pos < input.length() ? input.charAt(pos) : -1);
    }

    /**
     * consume the current character we're evaluating;
     * this advances the parser by calling next()
     * @param c the character to test/consume
     * @return  true if we consumed the character, otherwise false
     */
    private boolean consume(char c) {
        if (val == c) {
            next();
            return true;
        }
        return false;
    }

    /**
     * main parse method, starts the process
     * of building our recursive expression tree
     *
     * @return  the final, compiled Expression/tree
     */
    private Expression parse() {
        next(); //consume the next character
        Expression x = parseTier1();
        if (pos < input.length()) {
            throw new RuntimeException("unexpected char: " + (char) val);
        }
        return x;
    }

    /**
     * parseTier1 -> handles lowest precedence operators;
     * e.g. addition/subtraction
     *
     * @return  the compiled child expression
     */
    private Expression parseTier1() {
        Expression x = parseTier2();
        while (true) {
            if (consume('+')) {
                Expression left = x, right = parseTier2();
                x = () -> left.eval() + right.eval();
            } else if (consume('-')) {
                Expression left = x, right = parseTier2();
                x = () -> left.eval() - right.eval();
            } else {
                return x;
            }
        }
    }

    /**
     * parseTier2 -> handles next tier precedence;
     * e.g. multiplication/division/modular division
     *
     * @return  the compiled child expression
     */
    private Expression parseTier2() {
        Expression x = parseTier3();
        while (true) {
            if (consume('*')) {
                Expression left = x, right = parseTier3();
                x = () -> left.eval() * right.eval();
            } else if (consume('/')) {
                Expression left = x, right = parseTier3();
                x = () -> left.eval() / right.eval();
            }else if (consume('%')) {
                Expression left = x, right = parseTier3();
                x = () -> left.eval() % right.eval();
            } else {
                return x;
            }
        }
    }

    /**
     * parseTier3 -> handles exponentiation, including
     * nth-roots (fractional exponents); next to highest
     * precedence before identity and unary functions;
     *
     * @return  the compiled child expression
     */
    private Expression parseTier3() {
        Expression x = parseTier4();
        while (true) {
            // handle exponentiation & nth roots/fractional exponents
            if (consume('^')) {
                Expression left = x, right = parseTier4();
                x = () -> Math.pow(left.eval(), right.eval());
            } else if (consume('@')) {
                Expression left = x, right = parseTier4();
                x = () -> Math.pow(left.eval(), (1.0 / right.eval()));
            } else {
                return x;
            }
        }
    }

    /**
     * parseTier4 -> handles the highest tier operator
     * precedence; unary functions, parens, etc.
     *
     * @return  the compiled child expression
     */
    private Expression parseTier4() {
        int start = this.pos;
        Expression x;   // declare the Expression we're going to return
        if (consume('+')) {
            x = parseTier4();
            return x;
        } else if (consume('-')) {
            Expression right = parseTier4();
            x = () -> (-1.0 * right.eval());
            return x;
        }

        if (consume('(')) {
            x = parseTier1();     // branch our tree until we hit the ')'
            consume(')');
            return x;
        } else if (isNumber()) {
            while(isNumber()) {
                next();     // advance our parser to the first non-digit or '.'
            }
            double d = Double.parseDouble(input.substring(start, this.pos));
            x = () -> d;
            return x;
        } else if (isAlpha()) {     // handle unary functions, and variables
            while (isAlpha()) {
                next();     // advance our parser to the first non-alpha
            }
            String fn = input.substring(start, this.pos); // get the name of the function
            x = parseTier4();    // get the value the function will operate on
            if (map.containsKey(fn)) {
                Expression arg = x;
                DoubleUnaryOperator func = map.get(fn);
                x = () -> func.applyAsDouble(arg.eval());
            } else {
                x = () -> vars.get(fn);
            }
            return x;
        } else {
            System.out.println("Unexpected operation: " + val + ", " + (char) val);
            return null;
        }
    }

    /**
     * isNumber: check to see if the character we're
     * currently evaluating is between 0-9, or '.'
     * @return true if number or period, otherwise false
     */
    private boolean isNumber() {
        return Character.isDigit(val) || val == '.';
    }

    /**
     * isAlpha: check to see if the character we're
     * currently evaluating is between a-z
     * @return  true if a letter, otherwise false
     */
    private boolean isAlpha() {
        return Character.isAlphabetic(val) &&
	        !(val == '(' || val == ')');
    }

    /**
     * reset the parser to prepare for next expression
     */
    private void reset() {
        this.pos = -1;  // set the starting position for our loop/parser
        this.val = -1;
        this.input = "";
    }

    /**
     * format a given expression, as a string, to remove
     * any 'decorative' (read: 'pretty') labels/symbols
     * and replace them with proper math symbols, or special
     * flags/symbols used elsewhere in the parser
     *
     * @param in    the expression to clean/format, as string
     * @return      the properly formatted expression
     */
    private String formatInput(String in) {
        return in.replace(" ", "")    // strip spaces
                .replace("ⁿ√x", "@")    // use '@' to denote 'nth' roots
                .replace("√", "sqrt")   // handle square roots
                .replace("×", "*")      // convert 'pretty' * symbols
                .replace("÷", "/");     // convert 'pretty' / symbols
    }

    /**
     * Overloaded eval method to handle variables;
     * this allows the parser to be used for graphing
     * mode as well as normal/scientific.
     * @param exp   the input string to parse
     * @param vars  the map containing variables & values
     * @return      the result, as compiled Expression tree
     */
    public Expression eval(String exp, HashMap<String, Double> vars) {
        this.vars = vars;
        return this.eval(exp);
    }

    /**
     * Wrapper function to start the process of
     * evaluating our expression; Follows
     * basic PE(MD)(AS) operator precedence
     * @return  the result, as a compiled
     *          'Expression' Object/Tree
     */
    public Expression eval(String exp) {
        try {
            this.input = formatInput(exp);
            return this.parse();
        } finally {
            reset();    // reset our parser
        }
    }
}
