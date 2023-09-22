grammar Test;
prule: 'hallo ' name;
name: CHAR+;
CHAR: 'a'..'z' | 'A'..'Z' | ' ';