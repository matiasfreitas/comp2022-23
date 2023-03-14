grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

WS : [ \t\n\r\f]+ -> skip ;

LINE_COMMENT: '//' ~[\n\r]* ->skip;
BLOCK_COMMENT: '/*' .*? '*/' ->skip;
INT : [0-9]+ ;

BOOLEAN : 'true' | 'false' ;

// Add support for characters escaping?
CHAR: '\'' ~['\\] '\'';
STRING: '"' ~["\\]* '"';

VISIBILITY : 'public' | 'private' | 'protected' ;

TYPE: 'int' | 'boolean' | 'String' | 'char' | 'double';

ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;

// null? other data types?
// better way of doing arguments
// one or more classes?
// variable declaration with visibility specifiers inside class?

program : importDeclaration* classDeclaration EOF;

// Submodules not working as a list
importDeclaration : 'import' moduleName+=ID ( '.' moduleName+=ID )* ';' ;
classDeclaration : visibility=VISIBILITY? 'class' className=ID ( 'extends' extendsName=ID )? '{' classVarDeclaration* methodDeclaration* '}' ;

methodDeclaration
    : visibility=VISIBILITY? isStatic='static'? type methodName=ID '(' ( type ID ( ',' type ID )* )? ')' '{' statement*'}'
    ;

// Fazer distinção do builtin type e custom types?
simpleType
    : typeName=TYPE #BuiltinType
    | typeName=ID   #ObjectType
    ;
arrayType: arrayType '[' ']' | simpleType '[' ']';

type: simpleType | arrayType;

// assignment	= += -= *= /= %= &= ^= |= <<= >>= >>>= ???

varDeclaration :  type varName=ID ( '=' expression)? ';' ;

classVarDeclaration: visibility=VISIBILITY? varDeclaration | varDeclaration;

statement
    : '{' statement* '}' #ScopedBlock
    | 'if' '(' expression ')' statement 'else' statement  #IfStatement
    | 'while' '(' expression ')' statement #WhileLoop
    | expression ';' #SingleStatement
    | varDeclaration #VarDeclarationStatement
    | varName=ID '=' expression ';' #Assignment
    | varName=ID '[' expression ']' '=' expression ';' #ArrayAssignment
    | 'return' expression ';' #ReturnStatement
    ;
// instance of
expression
    : expression op=('++' | '--') #PostFix
    | op=('++'|'--'|'+' |'-' |'!') #Unary
    | expression op=('*' | '/') expression #BinaryOp
    | expression op=('+'|'-') expression #BinaryOp
    | expression op=('<<' | '>>' | '>>>') expression #BinaryOp
    | expression op=('<' |'>' |'<=' | '>=') expression #BinaryOp
    | expression op=('==' | '!=')  expression #BinaryOp
    | expression op='&' expression #BinaryOp
    | expression op='^' expression #BinaryOp
    | expression op='|' expression #BinaryOp
    | expression op='&&' expression #BinaryOp
    | expression op='||' expression #BinaryOp
    | expression '?' expression ':' expression #TernaryOp
    | expression '[' expression ']' #ArrayIndexing
    | expression '.' methodName=ID '(' ( expression ( ',' expression )* )? ')' #MethodCalling
    | expression '.' attributeName=ID #AttributeAccessing
    // See how arrays work in java
    | 'new' type '[' expression ']' #NewArray
    | 'new' typeName=ID '(' ')' #NewObject
    | '!' expression  #Negation
    | '(' expression ')'#Paren
    | value=INT  #Integer
    | value=BOOLEAN #Boolean
    | value=CHAR #CHAR
    | value=STRING #STRING
    | value=ID #Identifier
    | value='this' #This
    ;
