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

// Submodules not working as a list
importDeclaration : 'import' moduleName=ID ( '.' subModules=ID )* ';' ;
classDeclaration : 'class' className=ID ( 'extends' extendsName=ID )? '{' statement* methodDeclaration* '}' ;

methodDeclaration
    : visibility=VISIBILITY? isStatic='static'? type methodName=ID '(' ( type ID ( ',' type ID )* )? ')' '{' statement*'}'
    ;

simpleType
    : typeName=TYPE
    | typeName=ID
    ;
arrayType: arrayType '[' ']' | simpleType '[' ']';

type: simpleType | arrayType;

statement
    : '{' statement* '}' #ScopedBloc
    | 'if' '(' expression ')' statement 'else' statement  #IfStatement
    | 'while' '(' expression ')' statement #WhileLoop
    | expression ';' #SingleStatement
    | type varName=ID ( '=' expression)? ';' #Declaration
    | varName=ID '=' expression ';' #Assignment
    | varName=ID '[' expression ']' '=' expression ';' #ArrayAssignment
    | 'return' expression ';' #ReturnStatement
    ;

expression
    :expression op=('*' | '/') expression #BinaryOp
    |expression op=('+'|'-') expression #BinaryOp
    |expression op='&&' expression #BinaryOp
    |expression op='||' expression #BinaryOp
    |expression op=('<' |'>' |'<=' | '>=') expression #BinaryOp
    |expression op=('==' | '!=')  expression #BinaryOp
    | expression '[' expression ']' #ArrayIndexing
    | expression '.' methodName=ID '(' ( expression ( ',' expression )* )? ')' #MethodCalling
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
