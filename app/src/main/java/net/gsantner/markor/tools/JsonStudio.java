package net.gsantner.markor.tools;

/** Strict (RFC 8259) JSON validator / formatter / minifier. Pure Java, unit tested. */
public final class JsonStudio {
    private JsonStudio() {
    }

    public static final class Result {
        public boolean ok;
        public String output = "";
        public String error = "";
        public int line, column;
        public int keys, values, depth;
    }

    private static final int MAX_DEPTH = 256;

    /** indent: number of spaces, -1 = tab, 0 = minify. */
    public static Result process(final String text, final int indent) {
        final Result r = new Result();
        final P p = new P(text == null ? "" : text, indent, r);
        try {
            p.ws();
            if (p.pos >= p.s.length()) {
                throw p.err("empty");
            }
            p.value(0);
            p.ws();
            if (p.pos < p.s.length()) {
                throw p.err("trailing");
            }
            r.ok = true;
            r.output = p.out.toString();
        } catch (final JsonErr e) {
            r.ok = false;
            r.error = e.code;
            r.line = e.line;
            r.column = e.col;
            r.output = "";
        }
        return r;
    }

    public static Result format(final String text, final int indent) {
        return process(text, indent);
    }

    public static Result minify(final String text) {
        return process(text, 0);
    }

    private static final class JsonErr extends Exception {
        final String code;
        final int line, col;

        JsonErr(final String code, final int line, final int col) {
            super(code, null, false, false);
            this.code = code;
            this.line = line;
            this.col = col;
        }
    }

    private static final class P {
        final String s;
        final int indent;
        final Result r;
        final StringBuilder out = new StringBuilder();
        int pos;

        P(final String s, final int indent, final Result r) {
            this.s = s;
            this.indent = indent;
            this.r = r;
        }

        JsonErr err(final String code) {
            int line = 1, col = 1;
            for (int i = 0; i < pos && i < s.length(); i++) {
                if (s.charAt(i) == '\n') {
                    line++;
                    col = 1;
                } else {
                    col++;
                }
            }
            return new JsonErr(code, line, col);
        }

        void ws() {
            while (pos < s.length()) {
                final char c = s.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    pos++;
                } else {
                    break;
                }
            }
        }

        void nl(final int depth) {
            if (indent == 0) {
                return;
            }
            out.append('\n');
            for (int i = 0; i < depth; i++) {
                if (indent < 0) {
                    out.append('\t');
                } else {
                    for (int k = 0; k < indent; k++) {
                        out.append(' ');
                    }
                }
            }
        }

        void value(final int depth) throws JsonErr {
            if (depth > MAX_DEPTH) {
                throw err("depth");
            }
            if (depth + 1 > r.depth) {
                r.depth = depth + 1;
            }
            if (pos >= s.length()) {
                throw err("unexpected_end");
            }
            final char c = s.charAt(pos);
            if (c == '{') {
                object(depth);
            } else if (c == '[') {
                array(depth);
            } else if (c == '"') {
                out.append(string());
                r.values++;
            } else if (c == '-' || (c >= '0' && c <= '9')) {
                out.append(number());
                r.values++;
            } else if (s.startsWith("true", pos)) {
                lit("true");
            } else if (s.startsWith("false", pos)) {
                lit("false");
            } else if (s.startsWith("null", pos)) {
                lit("null");
            } else {
                throw err("unexpected");
            }
        }

        void lit(final String w) {
            out.append(w);
            pos += w.length();
            r.values++;
        }

        void object(final int depth) throws JsonErr {
            pos++;
            out.append('{');
            ws();
            if (pos < s.length() && s.charAt(pos) == '}') {
                pos++;
                out.append('}');
                r.values++;
                return;
            }
            boolean first = true;
            while (true) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                nl(depth + 1);
                ws();
                if (pos >= s.length()) {
                    throw err("unexpected_end");
                }
                if (s.charAt(pos) != '"') {
                    throw err("expected_key");
                }
                out.append(string());
                r.keys++;
                ws();
                if (pos >= s.length() || s.charAt(pos) != ':') {
                    throw err("expected_colon");
                }
                pos++;
                out.append(indent == 0 ? ":" : ": ");
                ws();
                value(depth + 1);
                ws();
                if (pos >= s.length()) {
                    throw err("unexpected_end");
                }
                final char c = s.charAt(pos);
                if (c == ',') {
                    pos++;
                } else if (c == '}') {
                    pos++;
                    nl(depth);
                    out.append('}');
                    r.values++;
                    return;
                } else {
                    throw err("expected_comma");
                }
            }
        }

        void array(final int depth) throws JsonErr {
            pos++;
            out.append('[');
            ws();
            if (pos < s.length() && s.charAt(pos) == ']') {
                pos++;
                out.append(']');
                r.values++;
                return;
            }
            boolean first = true;
            while (true) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                nl(depth + 1);
                ws();
                value(depth + 1);
                ws();
                if (pos >= s.length()) {
                    throw err("unexpected_end");
                }
                final char c = s.charAt(pos);
                if (c == ',') {
                    pos++;
                } else if (c == ']') {
                    pos++;
                    nl(depth);
                    out.append(']');
                    r.values++;
                    return;
                } else {
                    throw err("expected_comma");
                }
            }
        }

        String string() throws JsonErr {
            final int start = pos;
            pos++;
            while (true) {
                if (pos >= s.length()) {
                    throw err("unterminated_string");
                }
                final char c = s.charAt(pos);
                if (c == '"') {
                    pos++;
                    return s.substring(start, pos);
                }
                if (c < 0x20) {
                    throw err("control_char");
                }
                if (c == '\\') {
                    pos++;
                    if (pos >= s.length()) {
                        throw err("unterminated_string");
                    }
                    final char e = s.charAt(pos);
                    if (e == 'u') {
                        for (int i = 1; i <= 4; i++) {
                            if (pos + i >= s.length() || Character.digit(s.charAt(pos + i), 16) < 0) {
                                pos += i;
                                throw err("bad_escape");
                            }
                        }
                        pos += 4;
                    } else if ("\"\\/bfnrt".indexOf(e) < 0) {
                        throw err("bad_escape");
                    }
                }
                pos++;
            }
        }

        String number() throws JsonErr {
            final int start = pos;
            if (s.charAt(pos) == '-') {
                pos++;
            }
            if (pos >= s.length()) {
                throw err("bad_number");
            }
            if (s.charAt(pos) == '0') {
                pos++;
            } else if (s.charAt(pos) >= '1' && s.charAt(pos) <= '9') {
                digits();
            } else {
                throw err("bad_number");
            }
            if (pos < s.length() && s.charAt(pos) == '.') {
                pos++;
                if (digits() == 0) {
                    throw err("bad_number");
                }
            }
            if (pos < s.length() && (s.charAt(pos) == 'e' || s.charAt(pos) == 'E')) {
                pos++;
                if (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) {
                    pos++;
                }
                if (digits() == 0) {
                    throw err("bad_number");
                }
            }
            return s.substring(start, pos);
        }

        int digits() {
            int n = 0;
            while (pos < s.length() && s.charAt(pos) >= '0' && s.charAt(pos) <= '9') {
                pos++;
                n++;
            }
            return n;
        }
    }
}
