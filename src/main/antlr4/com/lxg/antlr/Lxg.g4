grammar Lxg;

prog: stmt* EOF;

stmt
    : 'print' expr ';'
    | 'let' ID '=' expr ';'
    | ID '=' expr ';'
    | 'if' '(' expr ')' block ('else' block)?
    | block
    ;

block: '{' stmt* '}';

expr: equality;

equality: comparison (('==' | '!=') comparison)*;

comparison: addition (( '<' | '>' | '<=' | '>=' ) addition)*;

addition: multiplication (( '+' | '-' ) multiplication)*;

multiplication: unary (( '*' | '/' ) unary)*;

unary: ( '+' | '-' | '!' ) unary | primary;

primary
    : INT
    | STRING
    | TRUE
    | FALSE
    | ID
    | '(' expr ')'
    ;

TRUE: 'true';
FALSE: 'false';
ID: [a-zA-Z_][a-zA-Z_0-9]*;
INT: [0-9]+;
STRING: '"' ( ~["\\] | '\\' . )* '"';
WS: [ \t\r\n]+ -> skip;
COMMENT: '//' ~[\r\n]* -> skip; 