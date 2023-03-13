grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

WS : [ \t\n\r\f]+ -> skip ;

INT : [0-9]+ ;

BOOLEAN : 'true' | 'false' ;

VISIBILITY : 'public' | 'private' | 'protected' ;

TYPE: 'int' | 'boolean' | 'String';

ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;

program : importDeclaration* classDeclaration EOF;

importDeclaration : 'import' ID ( '.' ID )* ';' ;
classDeclaration : 'class' class=ID ( 'extends' extends=ID )? '{' varDeclaration* methodDeclaration* '}' ;

varDeclaration : varType=type ID ';';
methodDeclaration
    : visibility=VISIBILITY? static='static'? returnType=type methodName=ID '(' ( type ID ( ',' type ID )* )? ')' '{' varDeclaration* statement* 'return' expression ';' '}'
    ;

simpleType
    : TYPE
    | ID
    ;
arrayType: arrayType '[' ']' | simpleType '[' ']';

type: simpleType | arrayType;

statement
    : '{' statement* '}'
    | 'if' '(' expression ')' statement 'else' statement
    | 'while' '(' expression ')' statement
    | expression ';'
    | ID '=' expression ';'
    | ID '[' expression ']' '=' expression ';'
    ;

expression
    :expression op=('*' | '/') expression #BinaryOp
    |expression op=('+'|'-') expression #BinaryOp
    |expression op='&&' expression #BinaryOp
    |expression op='||' expression #BinaryOp
    |expression op=('<' |'>' |'<=' | '>=') expression #BinaryOp
    |expression op=('==' | '!=')  expression #BinaryOp
    | expression '[' expression ']' #ArrayIndexing
    | expression '.' ID '(' ( expression ( ',' expression )* )? ')' #MethodCalling
    // See how arrays work in java
    | 'new' type '[' expression ']' #NewArray
    | 'new' ID '(' ')' #NewObject
    | '!' expression  #Negation
    | '(' expression ')'#Paren
    | value=INT  #Integer
    | value=BOOLEAN #Boolean
    | value=ID #Identifier
    | value='this' #This
    ;
