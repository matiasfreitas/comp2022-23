grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

WS : [ \t\n\r\f]+ -> skip ;

LINE_COMMENT: '//' ~[\n\r]* ->skip;
BLOCK_COMMENT: '/*' .*? '*/' ->skip;
INT : [1-9][0-9]* | '0' ;

BOOLEAN : 'true' | 'false' ;

// Add support for characters escaping?
CHAR: '\'' ~['\\] '\'';
STRING: '"' ~["\\]* '"';

VISIBILITY : 'public' | 'private' | 'protected' ;

TYPE: 'int' | 'boolean' | 'String' | 'char';

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
    : visibility=VISIBILITY? isStatic='static'? type methodName=ID '(' methodArguments?')' methodBody
    ;

methodBody: '{' (varDeclaration ';')* statement*'}';
// Fazer distinção do builtin type e custom types?
simpleType
    : typeName=TYPE #BuiltInType
    | typeName=ID   #ObjectType
    ;
arrayType: arrayType '[' ']' | simpleType '[' ']';

type: simpleType | arrayType;

// assignment	= += -= *= /= %= &= ^= |= <<= >>= >>>= ???

varTypeSpecification : type varName=ID ;
// Isto também é redundante
varDeclaration :  varTypeSpecification ;

methodArguments : varTypeSpecification (',' varTypeSpecification)* ;

// TODO: se é opcional não preciso desta regra redundante
classVarDeclaration: visibility=VISIBILITY? varDeclaration ';' | varDeclaration ';' ;

// TODO: Need to correct this  scopedBlock should disapear
statement
    : '{' statement* '}' #ScopedBlock
    // For the statements i just visit() and then add lablels while incrementing the labels names
    | 'if' '(' expression ')' statement 'else' statement  #IfStatement
    | 'while' '(' expression ')' statement #WhileLoop
    // TODO: Check in semantics what type of expressions can be statements
    | expression ';' #SingleStatement
    | varName=ID '=' expression ';' #Assignment
    | varName=ID '[' expression ']' '=' expression ';' #ArrayAssignment
    // TODO: return type and assumed type interaction! -> next todo
    | 'return' expression ';' #ReturnStatement
    ;
// instance of
expression
    : '(' expression ')'#Paren
    | expression '.' methodName=ID '(' ( expression ( ',' expression )* )? ')' #MethodCalling
    | expression '.' attributeName=ID #AttributeAccessing
    | expression '[' expression ']' #ArrayIndexing
    | op='!' expression #Unary
    | expression op=('*' | '/') expression #BinaryOp
    | expression op=('+'|'-') expression #BinaryOp
    | expression op='<'  expression #BinaryOp
    | expression op='&&' expression #BinaryOp
    | 'new' type '[' expression ']' #NewArray
    | 'new' typeName=ID '(' ')' #NewObject
    // TODO: what should i do with chars?
    // Should have a subtype called literal instead of int,boolean char,string....
    | value=INT  #Int
    | value=BOOLEAN #Boolean
    | value=CHAR #Char
    | value=STRING #String
    | value=ID #Identifier
    | value='this' #This
    ;
